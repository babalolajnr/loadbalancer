package com.loadbalancer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHelper implements NetworkEventListener {
    private MessageServer messageServer;


    private final ExecutorService executors = Executors.newFixedThreadPool(10);

    @Override
    public void notifyConnect() {
        System.out.println("Connection event notified");
        executors.submit(() -> {

        });
    }

    @Override
    public void notifyDisconnect() {

    }


    public String sendRequest(String req) {
        try {

            return messageServer.processMessage(req);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
