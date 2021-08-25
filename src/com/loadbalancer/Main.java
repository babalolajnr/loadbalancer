package com.loadbalancer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static ConcurrentHashMap<Integer, ServerSocket> currentConnections = new ConcurrentHashMap<>();
    private final static ExecutorService executors = Executors.newFixedThreadPool(1000);

    public static void main(String[] args) throws InterruptedException {

        if (Objects.isNull(currentConnections)) {
            currentConnections = new ConcurrentHashMap<>();
        }
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter Load Balancer IP address");
        String ip = scanner.nextLine();

        System.out.println("Enter Load Balancer port");
        int port = Integer.parseInt(scanner.nextLine());

        if (!currentConnections.containsKey(port)) {
            System.out.println("Load Balancer started on " + ip + ":" + port);
            currentConnections.put(port, new ServerSocket(ip, port));
        }

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
        NodePool nodePool = new NodePool();

        Scanner nodeDetails = new Scanner(System.in);
        System.out.println("Enter number of nodes");
        int noOfNodes = Integer.parseInt(nodeDetails.nextLine());

        for (int i = 0; i < noOfNodes; i++) {
            System.out.println("Enter node name");
            String name = nodeDetails.nextLine();

            System.out.println("Enter node Ip address");
            String ip = nodeDetails.nextLine();

            System.out.println("Enter node port");
            int port = Integer.parseInt(nodeDetails.nextLine());

            Node node = new Node(ip, port);
            NodeNetworkController nodeNetworkController = new NodeNetworkController(node);
            executors.submit(nodeNetworkController::init);
            Thread.sleep(1000);

            NodesManager nodesManager = new NodesManager();
            nodesManager.setNode(node);
            nodesManager.setNodeNetworkController(nodeNetworkController);
            nodeList.add(nodesManager);

        }

        nodePool.setNodeList(nodeList);
        nodeHashMap.put(1,nodePool);
    }

    public static HashMap<Integer, NodePool>
            nodeHashMap = new HashMap<>();
}
