package com.loadbalancer;

public interface NetworkEventListener {
    void notifyConnect();

    void notifyDisconnect();
}
