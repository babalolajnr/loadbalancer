package com.loadbalancer;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter Load Balancer IP address");
        String ip = scanner.nextLine();

        System.out.println("Enter Load Balancer port");
        int port = scanner.nextInt();
        scanner.close();
        registerNode();


    }

    private static void registerNode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of nodes");
        int noOfNodes = scanner.nextInt();

        for (int i = 0; i < noOfNodes; i++) {
            System.out.println("Enter node name");
            String name = scanner.nextLine();

            System.out.println("Enter node Ip address");
            String ip = scanner.nextLine();

            System.out.println("Enter node port");
            int port = scanner.nextInt();




//            Node node = new Node();
        }
    }
}
