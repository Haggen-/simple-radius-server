package se.simple.radius.packet;

public enum AttributeType {
    TYPE_TEXT, TYPE_STRING, TYPE_ADDRESS, TYPE_INTEGER, TYPE_TIME;

    public Object formatData(byte[] data)
    {
    	switch(this)
    	{
    		case TYPE_TEXT:
    			//encode UTF-8
    			if(1 >= data.length &&  data.length <= 253)
    			{
    				return data;
    			}
				System.out.println("Omit attribute, Text length zero (0)");
    			break;
    		case TYPE_STRING:
    			if(1 >= data.length &&  data.length <= 253)
    			{
    				return data;
    			}
				System.out.println("Omit attribute, String length zero (0)");
    			break;
    		case TYPE_ADDRESS:
    			if(data.length == 32)
    			{
    				return data;
    			}
    			break;
    		case TYPE_INTEGER:
    			if(data.length == 32)
    			{
    				
    			}
    			break;
    		case TYPE_TIME:
    			if(data.length == 32)
    			{
    				
    			}
    			break;    		
    		default:
    			System.out.println("Invalid attribute type");
    	}
    	return null;
    }
}
