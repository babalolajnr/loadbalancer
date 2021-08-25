package com.loadbalancer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NodeNetworkController extends MessageServer {


    private final Node node;

    public NodeNetworkController(Node node) {
        this.node = node;

    }

    private Socket socket;
    private Consumer<byte[]> consumer;
    private final ExecutorService executors = Executors.newFixedThreadPool(50);
    private final Object disconnectLock = new Object();
    private final Object retryConnectLock = new Object();
    private final Object connectLock = new Object();
    private static final int RECONNECT_WAIT_TIME = 5000;

//    private NetworkEventListener listener;


    public void init() {
        executors.submit(() -> connect(node));
        executors.submit(this::read);
    }

    public void setResponseConsumer(Consumer<byte[]> consumer) {
        this.consumer = consumer;
    }

    private void connect(Node node) {
        while (true) {

            while (isConnected()) {
                waitForDisconnection();
            }

            try {

                this.socket = new Socket();
                this.socket.setTcpNoDelay(true);
                this.socket.setKeepAlive(true);
                this.socket.setSoLinger(true, 0);

                try {
                    System.out.println("Attempting connection to Node " + node.getIp() + ":" + node.getPort());
                    this.socket.connect(new InetSocketAddress(node.getIp(), node.getPort()));
                    System.out.println("connected to " + node.getIp() + ":" + node.getPort());
                } catch (IOException e) {
                    closeSocket();
                    System.out.printf("Could not connect to remote %s:%d%n", node.getIp(), node.getPort());
                    waitForRetryConnection();
                }

                if (isConnected()) {
                    notifyConnect();
                }

            } catch (Exception e) {
                System.out.println("There was an error during connection" + e);
            }
        }
    }

    private void read() {
        while (true) {
            try {

                if (!isConnected()) {
                    waitForConnection();
                }

                try {

                    byte[] lenBytes = readToByteArray(socket.getInputStream(), 2);

                    int len = decodeSignedLenBytes(lenBytes[0], lenBytes[1]);

                    byte[] readData = readToByteArray(socket.getInputStream(), len);
                    System.out.println("response from Node " + new String(readData));

                    executors.submit(() -> consumer.accept(readData));

                } catch (IOException e) {
                    closeSocket();
                    System.out.println("There was an error reading data from socket" + e);
                }
            } catch (Exception e) {
                System.out.println("There as an error in read process" + e);
            }
        }
    }

    private byte[] readToByteArray(InputStream is, int len) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(is);
        byte[] data = new byte[len];
        dataInputStream.readFully(data);

        return data;
    }

    private static int decodeSignedLenBytes(byte firstByte, byte secondByte) {
        return ((firstByte & 255) << 8) + (secondByte & 255);
    }

    private boolean isConnected() {
        if (this.socket == null) {
            return false;
        }

        return this.socket.isConnected();
    }

    private void waitForDisconnection() {
        synchronized (disconnectLock) {
            try {
                disconnectLock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void waitForConnection() {
        synchronized (connectLock) {
            try {
                connectLock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void notifyConnect() {
        synchronized (connectLock) {
            connectLock.notifyAll();
        }

        this.notifyConnect();
    }

    private void waitForRetryConnection() {
        synchronized (retryConnectLock) {
            try {
                retryConnectLock.wait(RECONNECT_WAIT_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private synchronized void closeSocket() {
        if (socket == null || !socket.isConnected()) {
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(("There was an exception closing the client") + e);
        } finally {
            socket = null;
            notifyDisconnect();
        }
    }

    private void notifyDisconnect() {
        synchronized (disconnectLock) {
            disconnectLock.notifyAll();
        }

        this.notifyDisconnect();
    }

    public void write(byte[] data) throws IOException {
        if (socket == null || !socket.isConnected()) {
            throw new IOException("Socket is not connected");
        }

        System.out.println("Sending to %s" + new String(data));
        socket.getOutputStream().write(data);
    }
}


