package com.loadbalancer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static ConcurrentHashMap<Integer, ServerSocket> currentConnections = new ConcurrentHashMap<>();
    private final static ExecutorService executors = Executors.newFixedThreadPool(1000);

    public static void main(String[] args) throws InterruptedException {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter Load Balancer IP address");
        String ip = scanner.nextLine();

        System.out.println("Enter Load Balancer port");
        int port = scanner.nextInt();

        //Let server start fully before running next line registering nodes
        try {
            Thread.sleep(1000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        registerNode();

    }

    private static void registerNode() throws InterruptedException {
        List<NodesManager> nodeList = new ArrayList<>();
//        NodePool nodePool = new NodePool();

        Scanner nodeDetails = new Scanner(System.in);
        System.out.println("Enter number of nodes");
        int noOfNodes = nodeDetails.nextInt();

        for (int i = 0; i < noOfNodes; i++) {
            System.out.println("Enter node name");
            String name = nodeDetails.nextLine();

            System.out.println("Enter node Ip address");
            String ip = nodeDetails.nextLine();

            System.out.println("Enter node port");
            int port = nodeDetails.nextInt();

            Node node = new Node(ip, name, port);
            NodeNetworkController nodeNetworkController = new NodeNetworkController(node);
            executors.submit(nodeNetworkController::init);
            Thread.sleep(1000);

            NodesManager nodesManager = new NodesManager();
            nodesManager.setNode(node);
            nodesManager.setNodeNetworkController(nodeNetworkController);
            nodeList.add(nodesManager);

        }
    }

    public static HashMap<Integer, NodePool>
            nodeHashMap = new HashMap<>();
}
