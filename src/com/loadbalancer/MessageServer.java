package com.loadbalancer;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MessageServer implements Consumer<byte[]> {

    private NodeNetworkController nodeNetworkController;
    private ResponseController responseController;

    public MessageServer(NodeNetworkController nodeNetworkController) {
        this.nodeNetworkController = nodeNetworkController;
    }

    private static final ConcurrentHashMap<String, ResponseMatcher> responseMatcherMap = new ConcurrentHashMap<>();

    public MessageServer() {
    }

    public String processMessage(String request) throws Exception {
        nodeNetworkController.setResponseConsumer(this);

        ResponseMatcher responseMatcher = this.getResponseMatcher(request);
        if (responseMatcher == null) {
            throw new Exception("Could not get response matcher for request");
        }

        if (responseMatcherMap.containsKey(responseMatcher)) {
            throw new Exception("Could not get unique matcher key for request");
        }

        responseMatcherMap.put(responseMatcher.getKey(), responseMatcher);
        byte[] fullMessage = prependLenBytes(request.getBytes());

        try {
            nodeNetworkController.write(fullMessage);
        } catch (IOException e) {
            responseMatcherMap.remove(responseMatcher);
            throw e;
        }

        try {
            synchronized (responseMatcher) {
                int timeout = 30000;
                responseMatcher.wait(timeout);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("There was an error waiting for response from upstream", e);
        } finally {
            responseMatcherMap.remove(responseMatcher.getKey());
        }

        if (responseMatcher.getResponse() == null) {
            throw new IOException("The message timed out");
        }

        return responseMatcher.getResponse();
    }


    private ResponseController getResponseMatcher(String msg) {
        try {
            responseController = new ResponseController(msg);
            return responseController;
        } catch (Exception e) {
            System.out.println("There was an error getting response matcher" + e);
        }
        return null;
    }

    private static byte[] prependLenBytes(byte[] data) {
        short len = (short) data.length;
        byte[] newBytes = new byte[len + 2];
        newBytes[0] = (byte) (len / 256);
        newBytes[1] = (byte) (len & 255);
        System.arraycopy(data, 0, newBytes, 2, len);
        return newBytes;
    }

    /**
     * Accepts a byte array and converts to an ISOMsg used to notify waiting responseMatcher threads of returned responses
     *
     * @param bytes the data to be processed as a response
     */
    @Override
    public void accept(byte[] bytes) {
        String responseMatchKey = responseController.getKey(new String(bytes));
        if (responseMatchKey != null) {
            ResponseMatcher responseMatcher = responseMatcherMap.get(responseMatchKey);
            if (responseMatcher != null) {
                responseMatcher.setResponse(new String(bytes));
                synchronized (responseMatcher) {
                    responseMatcher.notify();
                }
                return;
            }
        }
        System.out.println("Could not get response matcher for response" + new java.lang.String(bytes));
    }


}

