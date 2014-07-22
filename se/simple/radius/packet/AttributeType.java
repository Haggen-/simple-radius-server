package se.simple.radius.packet;

import java.io.IOException;

public enum AttributeType {
    USERNAME(1, 3, null, AttributeValue.VALUE_STRING), 
    	PASSWORD(2, 18, 130, AttributeValue.VALUE_STRING), 
    		REPLY_MESSAGE(18, 3, null, AttributeValue.VALUE_TEXT), 
    			NAS_IDENTIFIER(32, 3, null, AttributeValue.VALUE_STRING);

    public final int type;
    public final AttributeValue value;
    private final Integer minLength;
    private final Integer maxLength;
    /**
     * set length range, NULL undefined.
     * @param i
     * @param minLength
     * @param maxLength
     */
    private AttributeType(int i, Integer minLength, Integer maxLength, AttributeValue value) {
        this.type = i;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.value = value;
    }
    
    public static AttributeType intToCode(int i) {
        for(AttributeType packetType : AttributeType.values()) {
            if(packetType.type == i)
                return packetType;
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
	
	public byte[] formatAttributeValue(String data) throws IOException
	{
		return this.value.formatValue(data);
	}
	
	public String getAttributeValue(byte[] data)
	{
		return this.value.getValue(data);
	}
}
