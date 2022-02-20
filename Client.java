import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Scanner;


public class Client {
    static String randomGenerateMACAddress(){

        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);

        StringBuilder sb = new StringBuilder(18);
        for(byte b : macAddr){
            if(sb.length()>0){
                sb.append(":");
            }
            sb.append(String.format("%02x",b));
        }
        return sb.toString();
    }
    static void doesHaveIP(String IP){
        if(!IP.equals(" ")){
            System.out.println("Client IP address is: "+ IP);
        }
        else
        {
            System.out.println("Client does not have an IP address yet.");
        }
    }
    public static void main(String[] args) {
        String MAC=randomGenerateMACAddress();
        String IPFromDHCPServer=" ";
        String DHCPServerIP=" ";
        String ClientIP=" ";


        try {
            DatagramSocket clientSocket = new DatagramSocket();
            System.out.println("Client is active ...");

            InetAddress serverIP = InetAddress.getByName("localhost");
            int serverPort = 8080;
            byte[] response = new byte[4096];
            byte[] request = new byte[4096];
            Scanner scan = new Scanner(System.in);
            System.out.println("You are ready to communicate");


                String msg =" DHCPDISCOVER: source MAC address is " + MAC + ", destination MAC address is 192.32.21.255  ";

                request = msg.getBytes();
                DatagramPacket clientPacket = new DatagramPacket(request, request.length, serverIP, serverPort);
                clientSocket.send(clientPacket);
                System.out.println("Client: " + msg);


                DatagramPacket serverPacket = new DatagramPacket(response, response.length);
                clientSocket.receive(serverPacket);
                msg = new String(serverPacket.getData()).trim();


                String[] msgArr = (msg.trim()).split(" ");
                IPFromDHCPServer = msgArr[6];
                DHCPServerIP = msgArr[11];

                request = new byte[4096];
                response = new byte[4096];

                String msg2="“DHCPREQUEST: I request IP address offered " + IPFromDHCPServer + ", DHCP server IP " + DHCPServerIP + ".";
                request = msg2.getBytes();
                clientSocket.send(clientPacket);
                System.out.println("Client: " + msg2);

                clientSocket.receive(serverPacket);
                ClientIP = IPFromDHCPServer;
                String msg8= new String(serverPacket.getData()).trim();


                request = new byte[4096];
                response = new byte[4096];

                clientSocket.receive(serverPacket);
                String msg3= new String(serverPacket.getData()).trim();
                System.out.println("Server: " + msg3);

                if(msg3.equals("Your IP address has expired, delete it and request another")){
                    ClientIP=" ";
                    String msg5 = "Re-requesting IP due to expiry";
                    request = msg5.getBytes();
                    clientSocket.send(clientPacket);
                    System.out.println("Client: " + msg5);
                    clientSocket.receive(serverPacket);
                    String msg10 = new String(serverPacket.getData()).trim();
                    ClientIP = IPFromDHCPServer;

                }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

