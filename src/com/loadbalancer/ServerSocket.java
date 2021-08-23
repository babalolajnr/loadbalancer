package com.loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.*;
import java.util.concurrent.*;


public class ServerSocket {

    private AsynchronousServerSocketChannel server;


    private final ThreadPoolExecutor executors = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);


    private List<AsynchronousSocketChannel> connectedClients = new ArrayList<>();
    private Queue<AsynchronousSocketChannel> clientQueue = new LinkedList<>();


    public ServerSocket(String ip, int port) {
        run(ip, port);
    }


    public void run(String ip, int port) {
        try {
            server = AsynchronousServerSocketChannel.open();

            server.bind(new InetSocketAddress(ip, port));

            server.accept(new ConnectionData(server), new ConnectionHandler());

            System.out.println("Listening for connections on port " + port);


        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }


    class ConnectionData {
        AsynchronousServerSocketChannel server;

        public ConnectionData(AsynchronousServerSocketChannel server) {
            this.server = server;
        }
    }

    class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, ConnectionData> {
        @Override
        public void completed(AsynchronousSocketChannel client, ConnectionData connectionData) {
            try {
                connectionData.server.accept(new ConnectionData(server), this);
                SocketAddress clientAddress = client.getRemoteAddress();
                notifyConnect(client);

                System.out.println("Accepted a connection from " + clientAddress);

            } catch (IOException e) {
                System.out.println("Could not process client connection" + e);
            }
        }

        @Override
        public void failed(Throwable exc, ConnectionData attachment) {
            System.out.println("Could not connect successfully to client " + exc);
        }
    }

    private void setupFirstTwoBytesReading(AsynchronousSocketChannel client, NodeNetworkController nodeNetworkController) {
        ReadData lenData = new ReadData();
        lenData.client = client;
        lenData.buffer = ByteBuffer.allocate(2);
        client.read(lenData.buffer, lenData, new FirstTwoBytesReader(nodeNetworkController));
    }


    class ReadData {
        AsynchronousSocketChannel client;
        ByteBuffer buffer;
    }

    class FirstTwoBytesReader implements CompletionHandler<Integer, ReadData> {
        private NodeNetworkController nodeNetworkController;

        public FirstTwoBytesReader(NodeNetworkController nodeNetworkController) {
            this.nodeNetworkController = nodeNetworkController;
        }

        @Override
        public void failed(Throwable exc, ReadData firstTwoBytesData) {
            notifyDisconnect(firstTwoBytesData.client);
            System.out.println("could not read first two bytes" + exc);
        }

        @Override
        public void completed(Integer result, ReadData readLenData) {
            if (result == -1) {
                notifyDisconnect(readLenData.client);
                return;
            }
            readLenData.buffer.flip();
            ReadData actualData = new ReadData();
            actualData.client = readLenData.client;
            short len = readLenData.buffer.getShort();
            actualData.buffer = ByteBuffer.allocate(len);
            System.out.println("Length bytes (2) read. expecting {} bytes" + len);
            actualData.client.read(actualData.buffer, actualData, new RemainingBytesHandler(nodeNetworkController));
        }
    }


    class RemainingBytesHandler implements CompletionHandler<Integer, ReadData> {
        private NodeNetworkController nodeNetworkController;

        public RemainingBytesHandler(NodeNetworkController nodeNetworkController) {
            this.nodeNetworkController = nodeNetworkController;
        }

        @Override
        public void failed(Throwable exc, ReadData remainingBytesData) {
            notifyDisconnect(remainingBytesData.client);
        }

        @Override
        public void completed(Integer result, ReadData remainingBytesData) {
            if (result == -1) {
                notifyDisconnect(remainingBytesData.client);
                return;
            }

            setupFirstTwoBytesReading(remainingBytesData.client, nodeNetworkController);
            remainingBytesData.buffer.flip();
            System.out.println("Remaining " + new String(remainingBytesData.buffer.array()));

            byte[] fullResponse = handleRequest(remainingBytesData.buffer.array(), remainingBytesData, nodeNetworkController);
            String log = String.format("writing to client  " + new String(fullResponse));
            System.out.println(log);

            ReadData actualData = new ReadData();
            actualData.client = remainingBytesData.client;

            ByteBuffer responseBuffer = ByteBuffer.wrap(fullResponse);
            synchronized (actualData.client) {
                actualData.client.write(responseBuffer); //send response back to client socket waiting
            }
        }
    }

    private byte[] handleRequest(byte[] request, ReadData readData, NodeNetworkController nodeNetworkController) {
        String response;
        byte[] fullResponse;

        try {
            MessageServer sender = new MessageServer(nodeNetworkController);
            response = sender.processMessage(new String(request));

            fullResponse = prependLenBytes(response.getBytes()); //add length to message
            return fullResponse;

        } catch (Exception e) {
            System.out.println("There was an error processing message " + e);
            return new byte[0];
        }


    }

    private static byte[] prependLenBytes(byte[] data) {
        short len = (short) data.length;
        byte[] newBytes = new byte[len + 2];
        newBytes[0] = (byte) (len / 256);
        newBytes[1] = (byte) (len & 255);
        System.arraycopy(data, 0, newBytes, 2, len);
        return newBytes;
    }


    private void notifyConnect(AsynchronousSocketChannel socketChannel) throws IOException {
        if (socketChannel == null) {
            throw new IOException("Socket channel is null");
        }
        connectedClients.add(socketChannel);
//        clientQueue.add(socketChannel);
    }

    private void notifyDisconnect(AsynchronousSocketChannel socketChannel) {
        if (socketChannel == null) {
            System.out.println("Socket channel is null");
        }

        int connectionSize = connectedClients.size();
        for (int i = 0; i < connectionSize; i++) {
            if (connectedClients.get(i).equals(socketChannel)) {
                connectedClients.remove(socketChannel);
                System.out.println("Removed socket channel");
                break;
            }
        }
    }


}
