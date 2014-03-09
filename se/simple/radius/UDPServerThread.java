package se.simple.radius;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

public class UDPServerThread implements Runnable {

    DatagramSocket socket;
    DatagramPacket receivedPacket;
    final String sharedSecret;
    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public  UDPServerThread(DatagramSocket socket, DatagramPacket packet, String sharedSecret) {
        this.socket = socket;
        this.receivedPacket = packet;
        this.sharedSecret = sharedSecret;
    }

    private boolean userExists(String username) {
        return UDPServer.userHT.get(username) != null;
    }

    private boolean isPasswordValid(String userPassword, String receivedPassword) {
        userPassword = new String(RadiusPacketAttribute.addPaddingTo16Bits(userPassword.getBytes()), UTF8_CHARSET);
        return userPassword.equals(receivedPassword);
    }

    @Override
    public void run() {
        try {
            RadiusPacket receivedRadiusPacket = RadiusPacket.parse(this.receivedPacket);
            switch(receivedRadiusPacket.packetCode) {
                case ACCESS_REQUEST: // For now, we only handle access requests.
                    RadiusPacketCode responseCode = RadiusPacketCode.ACCESS_REJECT;
                    RadiusPacketAttribute usernameAttr = receivedRadiusPacket.findFirstAttribute(RadiusPacketAttributeCode.USERNAME);
                    RadiusPacketAttribute passwordAttr = receivedRadiusPacket.findFirstAttribute(RadiusPacketAttributeCode.PASSWORD);

                    if(usernameAttr != null && passwordAttr != null) {
                        String receivedUsername = new String(usernameAttr.attributeData, UTF8_CHARSET);
                        String receivedPassword = RadiusPacketAttribute.decodePassword(receivedRadiusPacket.packetData, passwordAttr.attributeData, sharedSecret.getBytes());
                        if(userExists(receivedUsername) && isPasswordValid(UDPServer.userHT.get(receivedUsername), receivedPassword)) {
                            responseCode = RadiusPacketCode.ACCESS_ACCEPT;
                        }
                    }
                    if(responseCode.equals(RadiusPacketCode.ACCESS_REJECT)) {
                        System.out.println("Invalid attributes for ACCESS REQUEST. Sending ACCESS REJECT.");
                    }

                    byte[] responseData = RadiusPacket.createResponsePacket(receivedRadiusPacket, responseCode, this.sharedSecret.getBytes()).toByteArray();
                    socket.send(new DatagramPacket(responseData, responseData.length, this.receivedPacket.getAddress(), this.receivedPacket.getPort()));
                break;
                default:
                    System.out.println("Package code received isn't handled by this server, please switch to a better RADIUS Server to remedy this. Ignoring package.");
            }
        } catch (IllegalArgumentException | AssertionError | IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("MD5 Encryption failed, algorithm not found. This server requires a version of Java that has access to the MD5 Encryption Algorithm. ");
            ex.printStackTrace();
        }
    }
}
