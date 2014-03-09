package se.simple.radius;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPServer {
    private DatagramSocket socket;
    private boolean isRunning;
    final ExecutorService service;
    public final String sharedSecret;
    public static volatile HashMap<String, String> userHT;

    private UDPServer(int portNumber, String sharedSecret) throws SocketException {
        this.isRunning = true;
        this.service = Executors.newFixedThreadPool(6);
        this.socket = new DatagramSocket(portNumber);
        this.sharedSecret = sharedSecret;

        UDPServer.userHT = new HashMap<String, String>(); // Using a simple Hashtable instead of an actual database.
        UDPServer.userHT.put("frans1", "fran123!");
        UDPServer.userHT.put("frans2", "fran123!");
    }

    public static void main(String [] args) throws SocketException, NoSuchAlgorithmException {
        if(args.length < 2) {
            System.out.println("Invalid number of arguments to start server. Needs <port> <shared secret>");
            return;
        }
        try {
            UDPServer server = new UDPServer(Integer.parseInt(args[0]), args[1]);

            while(server.isRunning) {
                byte[] receiveData = new byte[4096];

                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                server.socket.receive(packet);

                Runnable worker = new UDPServerThread(server.socket, packet, server.sharedSecret);
                server.service.execute(worker);
            }
        } catch (SocketException ex) {
            System.out.println("Socket could not be bound to port " + args[0] + " please try again with another port.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Package could not be received.");
            ex.printStackTrace();
        }
    }
}
