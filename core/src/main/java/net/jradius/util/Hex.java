package net.jradius.util;

public class Hex 
{
	static String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

	public static byte[] hexStringToByteArray(String hex)
	{
	    int len = hex.length();
	    byte[] bin = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	    	bin[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
	    			+ Character.digit(hex.charAt(i+1), 16));
	    }
	    return bin;
	}
	
	public static String byteArrayToHexString(byte in[]) 
	{
	    byte ch = 0x00;
	    int i = 0; 

	    if (in == null || in.length <= 0)
	        return null;

	    StringBuffer sb = new StringBuffer(in.length * 2);

	    while (i < in.length) 
	    {
	    	ch = (byte) (in[i] & 0xF0);
	        ch = (byte) (ch >>> 4);
	        ch = (byte) (ch & 0x0F);    
	        sb.append(pseudo[(int)ch]); 
	        ch = (byte) (in[i] & 0x0F); 
	        sb.append(pseudo[(int)ch]); 
	        i++;
	    }

	    return sb.toString();
	}    
}
