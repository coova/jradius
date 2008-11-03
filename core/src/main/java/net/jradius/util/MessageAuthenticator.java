package net.jradius.util;

import java.util.Arrays;

import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.RadiusAttribute;

public class MessageAuthenticator 
{
    private static final RadiusFormat format = RadiusFormat.getInstance();

    public static void generateRequestMessageAuthenticator(RadiusPacket request, String sharedSecret)
    {
        byte[] hash = new byte[16];
        request.overwriteAttribute(AttributeFactory.newAttribute(AttributeDictionary.MESSAGE_AUTHENTICATOR, hash));
        System.arraycopy(MD5.hmac_md5(format.packPacket(request, sharedSecret), sharedSecret.getBytes()), 0, hash, 0, 16);
	}
    
    public static void generateResponseMessageAuthenticator(RadiusPacket request, RadiusPacket reply, String sharedSecret)
    {
        byte[] hash = new byte[16];
        byte[] requestAuth = request.getAuthenticator();
        byte[] replyAuth = reply.getAuthenticator();
        reply.setAuthenticator(requestAuth);
        reply.overwriteAttribute(AttributeFactory.newAttribute(AttributeDictionary.MESSAGE_AUTHENTICATOR, hash));
        System.arraycopy(MD5.hmac_md5(format.packPacket(reply, sharedSecret), sharedSecret.getBytes()), 0, hash, 0, 16);
        reply.setAuthenticator(replyAuth);
	}
    
    public static Boolean verifyRequest(RadiusPacket request, String sharedSecret)
    {
        byte[] hash = new byte[16];
        
        RadiusAttribute attr = request.findAttribute(AttributeDictionary.MESSAGE_AUTHENTICATOR);
        if (attr == null) return null;
        
        byte[] pval = attr.getValue().getBytes();
        attr.setValue(hash);
        
        System.arraycopy(MD5.hmac_md5(format.packPacket(request, sharedSecret), sharedSecret.getBytes()), 0, hash, 0, 16);

        return new Boolean(Arrays.equals(pval, hash));
    }

    public static Boolean verifyReply(RadiusPacket request, RadiusPacket reply, String sharedSecret)
    {
        byte[] requestAuth = request.getAuthenticator();
        byte[] replyAuth = reply.getAuthenticator();
        byte[] hash = new byte[16];
        
        RadiusAttribute attr = reply.findAttribute(AttributeDictionary.MESSAGE_AUTHENTICATOR);
        if (attr == null) return null;
        
        byte[] pval = attr.getValue().getBytes();
        attr.setValue(hash);
        
        reply.setAuthenticator(requestAuth);

        System.arraycopy(MD5.hmac_md5(format.packPacket(reply, sharedSecret), sharedSecret.getBytes()), 0, hash, 0, 16);

        reply.setAuthenticator(replyAuth);
        
        return new Boolean(Arrays.equals(pval, hash));
    }
}
