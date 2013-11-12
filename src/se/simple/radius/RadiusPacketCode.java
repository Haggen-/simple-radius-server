package se.simple.radius;

public enum RadiusPacketCode {
    ACCESS_REQUEST(1), ACCESS_ACCEPT(2), ACCESS_REJECT(3);
    public final int code;

    private RadiusPacketCode(int i) {
        this.code = i;
    }

    public static RadiusPacketCode intToCode(int i) {
        for(RadiusPacketCode code : RadiusPacketCode.values()) {
            if(code.code == i)
                return code;
        }
        throw new IllegalArgumentException("Invalid code for se.johanhagg.interview.nexus.radius.RadiusPacket");
    }
}