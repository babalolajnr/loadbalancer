package com.loadbalancer;

public class Node {
    private String ip;
    private String name;
    private int port;

    public Node(String ip, String name, int port) {
        this.ip = ip;
        this.name = name;
        this.port = port;
    }

    public String getNodeName() {
        return this.name;
    }

    public void setNodeName(String nodeName) {
        this.name = nodeName;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
