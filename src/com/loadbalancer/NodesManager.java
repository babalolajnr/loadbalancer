package com.loadbalancer;

public class NodesManager {
    private Node node;
    private NodeNetworkController nodeNetworkController;

    public NodeNetworkController getNodeNetworkController() {
        return nodeNetworkController;
    }

    public void setNodeNetworkController(NodeNetworkController nodeNetworkController) {
        this.nodeNetworkController = nodeNetworkController;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}

