package se.simple.radius.packet.attribute;

public enum RadiusPacketAttributeCode {
    USERNAME(1, 3, null), 
    	PASSWORD(2, 18, 130), 
    		REPLY_MESSAGE(18, 3, null), 
    			NAS_IDENTIFIER(32, 3, null);

    public final int code;
    private final Integer minLength;
    private final Integer maxLength;

    private RadiusPacketAttributeCode(int i, Integer minLength, Integer maxLength) {
        this.code = i;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
    
    public static RadiusPacketAttributeCode intToCode(int i) {
        for(RadiusPacketAttributeCode packetCode : RadiusPacketAttributeCode.values()) {
            if(packetCode.code == i)
                return packetCode;
        }
        throw new IllegalArgumentException("Invalid attribute code for RadiusPacketAttribute");
    }
    
    public boolean isValidLength(int length)
	{
		return (checkMin(length) && checkMax(length));
	}
	
	private boolean checkMin(int length)
	{
		if(this.minLength == null)
			return true;
		return length >= this.minLength;
	}
	
	private boolean checkMax(int length)
	{
		if(this.maxLength == null)
			return true;
		return length <= this.maxLength;
	}
}
