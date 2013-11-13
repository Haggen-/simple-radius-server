package se.simple.radius;

public enum RadiusPacketAttributeCode {
    USERNAME(1), PASSWORD(2), REPLY_MESSAGE(18);

    public final int code;

    private RadiusPacketAttributeCode(int i) {
        this.code = i;
    }

    public static RadiusPacketAttributeCode intToCode(int i) {
        for(RadiusPacketAttributeCode packetCode : RadiusPacketAttributeCode.values()) {
            if(packetCode.code == i)
                return packetCode;
        }
        throw new IllegalArgumentException("Invalid attribute code for RadiusPacketAttribute");
    }
}
