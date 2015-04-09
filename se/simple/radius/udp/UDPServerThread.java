package se.simple.radius.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import se.simple.radius.Packet;
import se.simple.radius.PacketCode;
import se.simple.radius.packet.Attribute;
import se.simple.radius.packet.AttributeType;

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
        userPassword = new String(Attribute.addPaddingTo16Bits(userPassword.getBytes()), UTF8_CHARSET);
        return userPassword.equals(receivedPassword);
    }

    @Override
    public void run() {
        try {
            Packet receivedRadiusPacket = Packet.parse(this.receivedPacket);
            switch(receivedRadiusPacket.packetCode) {
                case ACCESS_REQUEST: // For now, we only handle access requests.
                    PacketCode responseCode = PacketCode.ACCESS_REJECT;
                    Attribute usernameAttr = receivedRadiusPacket.findFirstAttribute(AttributeType.USERNAME);
                    Attribute passwordAttr = receivedRadiusPacket.findFirstAttribute(AttributeType.PASSWORD);

                    Attribute stateAttr = receivedRadiusPacket.findFirstAttribute(AttributeType.STATE);

                    if(UDPServer.challengeSent.get(Integer.valueOf(receivedRadiusPacket.packetIdentifier - 1)) != null) {
                        UDPServer.challengeSent.remove(receivedRadiusPacket.packetIdentifier - 1);
                        Respond(receivedRadiusPacket, PacketCode.ACCESS_ACCEPT);
                        return;
                    }

                    if(usernameAttr != null && passwordAttr != null) {
                    	if(usernameAttr.isValidLength() && passwordAttr.isValidLength())
                    	{
                    		String receivedUsername = new String(usernameAttr.attributeValue, UTF8_CHARSET);
                    		String receivedPassword = Attribute.decodePassword(receivedRadiusPacket.packetData, passwordAttr.attributeValue, sharedSecret.getBytes());
	                        if(userExists(receivedUsername) && isPasswordValid(UDPServer.userHT.get(receivedUsername), receivedPassword)) {
	                            responseCode = PacketCode.ACCESS_ACCEPT;
	                        }
                    	}
                    }

                    if(UDPServer.challengeSent.get(Integer.valueOf(receivedRadiusPacket.packetIdentifier - 1)) == null) {
                        RespondChallenge(receivedRadiusPacket);
                        UDPServer.challengeSent.put(receivedRadiusPacket.packetIdentifier, true);
                        return;
                    }

                    if(responseCode.equals(PacketCode.ACCESS_REJECT)) {
                        System.out.println("Invalid attributes for ACCESS REQUEST. Sending ACCESS REJECT.");
                    }
                    Respond(receivedRadiusPacket, responseCode);
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

    private void Respond(Packet receivedPackage, PacketCode responseCode) throws NoSuchAlgorithmException, IOException
    {
        byte[] responseData = Packet.createResponsePacket(receivedPackage, responseCode, this.sharedSecret.getBytes()).toByteArray();
        socket.send(new DatagramPacket(responseData, responseData.length, this.receivedPacket.getAddress(), this.receivedPacket.getPort()));
    }

    private void RespondChallenge(Packet receivedPackage) throws NoSuchAlgorithmException, IOException
    {
        byte[] challengeData = Packet.createChallengePacket(receivedPackage, this.sharedSecret.getBytes()).toByteArray();
        socket.send(new DatagramPacket(challengeData, challengeData.length, this.receivedPacket.getAddress(), this.receivedPacket.getPort()));
    }
}
