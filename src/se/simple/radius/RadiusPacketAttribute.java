package se.simple.radius;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class RadiusPacketAttribute {
    RadiusPacketAttributeCode attributeCode;
    final static int HEADER_SIZE = 2;
    private int attributeLength;
    byte[] attributeData;
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    RadiusPacketAttribute(int code, byte[] data) throws IllegalArgumentException {
        this.attributeCode = RadiusPacketAttributeCode.intToCode(code);
        this.attributeLength = data.length + HEADER_SIZE;
        this.attributeData = data;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(this.attributeCode.code);
        bos.write(this.attributeLength);
        bos.write(this.attributeData, 0, this.attributeData.length);
        return bos.toByteArray();
    }


    public static byte[] xorByteArray(byte[] one, byte[] two) {
        byte[] res = new byte[one.length];
        for(int i = 0; i < one.length; i++) {
            res[i] = (byte) (one[i] ^ two[i%two.length]);
        }
        return res;
    }

    private static byte[] generateSharedSecretRAHash(byte[] ResponseAuthenticator, byte[] sharedSecret) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(sharedSecret);
        md5.update(ResponseAuthenticator);

        return md5.digest();
    }

    public static byte[] addPaddingTo16Bits(byte[] unPaddedPackage) {
        int newLength = ((unPaddedPackage.length/16) + 1)*16;
        byte[] returnBuffer = new byte[newLength];

        Arrays.fill(returnBuffer, (byte) 0);
        System.arraycopy(unPaddedPackage, 0, returnBuffer, 0, unPaddedPackage.length);

        return returnBuffer;
    }

    public static String decodePassword(byte[] requestAuthenticator, byte[] passwordData, byte[] sharedSecret) throws NoSuchAlgorithmException {
        final int SEGMENT_LENGTH = 16;

        if(passwordData.length == SEGMENT_LENGTH) {
            return  new String(xorByteArray(generateSharedSecretRAHash(requestAuthenticator, sharedSecret), passwordData), UTF8_CHARSET);
        }

        int len = passwordData.length-SEGMENT_LENGTH;

        byte[] remainingPasswordData = Arrays.copyOfRange(passwordData, 0, len);
        byte[] currentpasswordData = Arrays.copyOfRange(passwordData, len, passwordData.length);

        return decodePassword(requestAuthenticator, remainingPasswordData, sharedSecret) + new String(xorByteArray(generateSharedSecretRAHash(currentpasswordData, sharedSecret), passwordData), UTF8_CHARSET);
    }
}
