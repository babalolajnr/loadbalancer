package com.loadbalancer;

public class Node {
    private final String ip;
    private final int port;

    public Node(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

}
