package se.simple.radius;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class RadiusPacket {
    final static int CODE_FIELDLENGTH = 1;
    final static int IDENTIFIER_FIELDLENGTH = 1;
    final static int LENGTH_FIELDLENGTH = 2;
    final static int AUTH_FIELDLENGTH = 16;

    RadiusPacketCode packetCode;
    byte[] packetData;

    private int packetIdentifier;
    private short packetLength;


    ArrayList<RadiusPacketAttribute> attributes = new ArrayList<RadiusPacketAttribute>();

    public RadiusPacket(int code, int packetIdentifier, byte[] data) throws IllegalArgumentException {
        this.packetCode = RadiusPacketCode.intToCode(code);
        this.packetIdentifier = packetIdentifier;
        this.packetLength = (short)(CODE_FIELDLENGTH + IDENTIFIER_FIELDLENGTH + LENGTH_FIELDLENGTH + AUTH_FIELDLENGTH);
        this.packetData = data;
    }

    public RadiusPacket(RadiusPacketCode code, int packetIdentifier, byte[] data) {
        this.packetCode = code;
        this.packetIdentifier = packetIdentifier;
        this.packetLength = (short)(CODE_FIELDLENGTH + IDENTIFIER_FIELDLENGTH + LENGTH_FIELDLENGTH + AUTH_FIELDLENGTH);
        this.packetData = data;
    }

    public void addAttribute(int code, int length, byte[] data) throws IllegalArgumentException {
        this.packetLength = (short)(this.packetLength + length + RadiusPacketAttribute.HEADER_SIZE);
        this.attributes.add(new RadiusPacketAttribute(code, data));
    }

    public RadiusPacketAttribute findFirstAttribute(RadiusPacketAttributeCode code) {
        for(RadiusPacketAttribute attr : this.attributes) {
            if(attr.attributeCode.equals(code))
                return attr;
        }
        return null;
    }

    public static RadiusPacket parse(DatagramPacket packet) throws IllegalArgumentException, AssertionError {
        RadiusPacket rad;
        try {
            ByteBuffer bb = ByteBuffer.wrap(packet.getData());
            int packetCode = bb.get();
            int packetIdentifier = bb.get();
            int packetLength = bb.getShort();

            if(packetLength != packet.getLength())
                throw new AssertionError("Invalid package length. Discarding package.");
            byte[] packetData = new byte[AUTH_FIELDLENGTH];
            bb.get(packetData, 0, AUTH_FIELDLENGTH);

            rad = new RadiusPacket(packetCode, packetIdentifier, packetData);

            while(bb.position() < packetLength) {
                int attrCode = bb.get();
                int attrLength = bb.get();
                int attrLengthWithoutHeader = attrLength-RadiusPacketAttribute.HEADER_SIZE;

                byte[] attrData = new byte[attrLengthWithoutHeader];
                bb.get(attrData, 0, attrLengthWithoutHeader);
                rad.addAttribute(attrCode, attrLength, attrData);
            }

        } catch (AssertionError ex) {
            System.out.println(ex.getMessage());
            rad = null;
        }

        return rad;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(this.packetCode.code);
        bos.write(this.packetIdentifier);

        ByteBuffer buffer = ByteBuffer.allocate(LENGTH_FIELDLENGTH);
        buffer.putShort(this.packetLength);
        buffer.flip();
        bos.write(buffer.array(), 0, buffer.array().length);
        bos.write(this.packetData, 0, this.packetData.length);

        for(RadiusPacketAttribute attribute : this.attributes) {
            bos.write(attribute.toByteArray(), 0, attribute.toByteArray().length);
        }

        return bos.toByteArray();
    }

    public static RadiusPacket createResponsePacket(RadiusPacket receivedPackage, RadiusPacketCode responseCode, byte[] sharedSecret) throws NoSuchAlgorithmException {
        return new RadiusPacket(responseCode, receivedPackage.packetIdentifier, createResponseAuthenticator(responseCode, receivedPackage.packetIdentifier, receivedPackage.packetData, sharedSecret));
    }

    private static byte[] createResponseAuthenticator(RadiusPacketCode code, int identifier, byte[] packageData, byte[] sharedSecret) throws NoSuchAlgorithmException {
        RadiusPacket rad = new RadiusPacket(code, identifier, packageData);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(rad.toByteArray());
        md.update(sharedSecret);

        return md.digest();
    }
}
