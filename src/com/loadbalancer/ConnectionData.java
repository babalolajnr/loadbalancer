package com.loadbalancer;

import java.nio.channels.AsynchronousServerSocketChannel;

public class ConnectionData {
    AsynchronousServerSocketChannel asyncServer;

    public ConnectionData(AsynchronousServerSocketChannel asyncServer) {
        this.asyncServer = asyncServer;
    }
}
