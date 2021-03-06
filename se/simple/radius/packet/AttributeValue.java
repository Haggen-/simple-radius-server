package se.simple.radius.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public enum AttributeValue {
    VALUE_TEXT, VALUE_STRING, VALUE_ADDRESS, VALUE_INTEGER, VALUE_TIME;

    public String getValue(byte[] data)
    {
    	String value = null;
    	ByteBuffer bb;
    	switch(this)
    	{
    		case VALUE_TEXT:
    			if(data.length >= 1 &&  data.length <= 253)
    			{
    				value = new String(data, Charset.forName("UTF-8"));
    			}
				System.out.println("Omit attribute, Text length zero (0)");
    			break;
    		case VALUE_STRING:
    			if(data.length >= 1 &&  data.length <= 253)
    			{
    				value = new String(data);
    			}
				System.out.println("Omit attribute, String length zero (0)");
    			break;
    		case VALUE_ADDRESS:
    			StringBuilder sb = new StringBuilder();
    			for(byte tmp : data)
    			{
    				sb.append((tmp & 0xFF)+".");
    			}	
    			sb.deleteCharAt(sb.length()-1);
    			value = sb.toString();
    			break;
    		case VALUE_INTEGER:
    			bb = ByteBuffer.wrap(data);
    			
    			value = ""+bb.getInt();
    			
    			break;
    		case VALUE_TIME:
    			bb = ByteBuffer.wrap(data);
    			
    			value = ""+bb.getInt();
    			
    			break;    		
    		default:
    			System.out.println("Invalid attribute VALUE");
    	}
    	return value;
    }
    
    public byte[] formatValue(String data) throws IOException
    {
    	byte[] value = null;
    	ByteBuffer bb;
    	ByteArrayOutputStream bos;
    	switch(this)
    	{
    		case VALUE_TEXT:
    			if(data.length() >= 1 &&  data.length() <= 253)
    			{
    				value = data.getBytes(Charset.forName("UTF-8"));
    			}
				System.out.println("Omit attribute, Text length zero (0)");
    			break;
    		case VALUE_STRING:
    			if(data.length() >= 1 &&  data.length() <= 253)
    			{
    				value = data.getBytes();
    			}
				System.out.println("Omit attribute, String length zero (0)");
    			break;
    		case VALUE_ADDRESS:
    			String[] dataSplit = data.split("\\.");
    			bb = ByteBuffer.allocate(dataSplit.length);
    			for(String tmp : dataSplit)
    			{
    				bb.put((byte)Integer.parseInt(tmp));
    			}
    			
    			bos = new ByteArrayOutputStream();
    			bos.write(bb.array(), 0, bb.array().length);
    			bos.flush();
    			
    			value = bos.toByteArray();    			
    			break;
    		case VALUE_INTEGER:
    			bb = ByteBuffer.allocate(4);
    			bb.putInt(Integer.parseInt(data));
    			
    			bos = new ByteArrayOutputStream();
    			bos.write(bb.array(), 0, bb.array().length);
    			bos.flush();
    			
    			value = bos.toByteArray();
    			break;
    		case VALUE_TIME:
    			bb = ByteBuffer.allocate(4);
    			bb.putInt(Integer.parseInt(data));
    			
    			bos = new ByteArrayOutputStream();
    			bos.write(bb.array(), 0, bb.array().length);
    			bos.flush();
    			
    			value = bos.toByteArray();
    			break;    		
    		default:
    			System.out.println("Invalid attribute VALUE");
    	}
    	return value;
    }
}
