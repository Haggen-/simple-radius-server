package se.simple.radius.packet;

public enum AttributeValue {
    VALUE_TEXT, VALUE_STRING, VALUE_ADDRESS, VALUE_INTEGER, VALUE_TIME;

    public Object formatValue(byte[] data)
    {
    	switch(this)
    	{
    		case VALUE_TEXT:
    			//encode UTF-8
    			if(1 >= data.length &&  data.length <= 253)
    			{
    				return data;
    			}
				System.out.println("Omit attribute, Text length zero (0)");
    			break;
    		case VALUE_STRING:
    			if(1 >= data.length &&  data.length <= 253)
    			{
    				return data;
    			}
				System.out.println("Omit attribute, String length zero (0)");
    			break;
    		case VALUE_ADDRESS:
    			if(data.length == 32)
    			{
    				return data;
    			}
    			break;
    		case VALUE_INTEGER:
    			if(data.length == 32)
    			{
    				
    			}
    			break;
    		case VALUE_TIME:
    			if(data.length == 32)
    			{
    				
    			}
    			break;    		
    		default:
    			System.out.println("Invalid attribute VALUE");
    	}
    	return null;
    }
}
