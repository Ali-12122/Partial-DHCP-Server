import java.util.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Scanner;

public class Server {
    static void outputAllIPs(Map<String,String> IPs){
        for (Map.Entry mapElement : IPs.entrySet()) {
            String key= (String) mapElement.getKey();
            String value = (String)mapElement.getValue();
            System.out.println(key + " : " + value);
        }
    }
    static void outputReservedIPs(Map<String,String> IPs){
        for (Map.Entry mapElement : IPs.entrySet()){
            String key= (String) mapElement.getKey();
            String value = (String)mapElement.getValue();
            if(!value.equals("0")){
                System.out.println(key + " : " + value);
            }
        }
    }
    static String incrementIP(String IP){
       String[] IPArr = (IP.trim().split("\\."));
       int i = Integer.parseInt(IPArr[3]);
       ++i;
       IP= IPArr[0] +IPArr[1]+IPArr[2]+ i;
       return IP;
    }

    public static void main(String[] args) {
        Map<String,String>IPAddresses=new HashMap<>();
        IPAddresses.put("192.32.21.1","0");
        IPAddresses.put("192.32.21.2","0");
        IPAddresses.put("192.32.21.3","0");
        IPAddresses.put("192.32.21.4","0");

        String firstUnusedIP="192.32.21.1";
        String ServerIp="192.32.31.12";
        String RouterIp="192.32.31.11";
        String SubnetMask = "192.32.21.255";
        String DNSServer1="192.32.21.100";
        String DNSServer2="192.32.21.101";

        try {
            DatagramSocket serverSocket = new DatagramSocket(8080);
            System.out.println("Server is up !");
            byte[] requestBytes = new byte[4096];
            final byte[][] responseBytes = {new byte[4096]};
            Scanner scan = new Scanner(System.in);
            System.out.println("Server is ready to communicate");

                DatagramPacket clientPacket = new DatagramPacket(requestBytes, requestBytes.length);
                serverSocket.receive(clientPacket);

                String msg = new String(clientPacket.getData()).trim();
                String[] msgArr = (msg.trim()).split(" ");
                IPAddresses.put(firstUnusedIP,"Offered to: "+msgArr[2]);

                msg = "DHCPOFFER: Your IP Address can be "+ firstUnusedIP +" Server\n" +
                        "IP address is " + ServerIp + ", Router IP address is " + RouterIp + ", Subnet mask " + SubnetMask + ", IP\n" +
                        "address lease time is 5 seconds, DNS Servers are"+DNSServer1+", "+DNSServer2+".";

                responseBytes[0] = msg.getBytes();
                InetAddress clientIP = clientPacket.getAddress();
                int clientPort = clientPacket.getPort();
                DatagramPacket serverPacket = new DatagramPacket(responseBytes[0], responseBytes[0].length, clientIP, clientPort);

                serverSocket.send(serverPacket);
                System.out.println("Server: " + msg);
                requestBytes = new byte[4096];
                responseBytes[0] = new byte[4096];


                serverSocket.receive(clientPacket);
                String msg2 = new String(clientPacket.getData()).trim();
                String[] msg2Arr = (msg2.trim()).split("");
                IPAddresses.put(firstUnusedIP,"Reserved for: "+msgArr[2]);

                String msg3 = "DHCPOFFER: Your IP Address is "+ firstUnusedIP +" Server\n" +
                        "IP address is " + ServerIp + ", Router IP address is " + RouterIp + ", Subnet mask " + SubnetMask + ", IP\n" +
                        "address lease time is 5 seconds, DNS Servers are"+DNSServer1+", "+DNSServer2+".";
                responseBytes[0] = msg3.getBytes();
                serverSocket.send(serverPacket);
                System.out.println("Server: " + msg3);


                responseBytes[0] = new byte[4096];
                requestBytes = new byte[4096];

            String finalFirstUnusedIP = firstUnusedIP;
            new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                String msg4="Your IP address has expired, delete it and request another";
                                responseBytes[0] = msg4.getBytes();

                                    serverSocket.send(serverPacket);
                                    System.out.println("Server: " + msg4);
                                    IPAddresses.put("192.32.21.1"," ");
                                    serverSocket.receive(clientPacket);
                                    String msg6 = new String(clientPacket.getData()).trim();
                                    System.out.println("Client: " + msg6);
                                    if (msg6.equals("Re-requesting IP due to expiry")){
                                        IPAddresses.put(finalFirstUnusedIP,"Reserved for: "+msgArr[2]);
                                        String msg8 = "DHCPOFFER: Your IP Address is "+ finalFirstUnusedIP +" Server\n" +
                                                "IP address is " + ServerIp + ", Router IP address is " + RouterIp + ", Subnet mask " + SubnetMask + ", IP\n" +
                                                "address lease time is 5 seconds, DNS Servers are"+DNSServer1+", "+DNSServer2+".";
                                        responseBytes[0] = msg8.getBytes();
                                        serverSocket.send(serverPacket);
                                        System.out.println("Server: " + msg8);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            }
                        },
                        5000
                );
            firstUnusedIP= incrementIP(firstUnusedIP);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
