package com.loadbalancer;

import java.util.List;

public class NodePool {
    private List<NodesManager> nodeList;

    public List<NodesManager> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<NodesManager> nodeList) {
        this.nodeList = nodeList;
    }

}
