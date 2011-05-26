package net.jradius.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.jradius.packet.RadiusFormat;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeDictionary;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.RadiusAttribute;

public class MessageAuthenticator 
{
    private static final RadiusFormat format = RadiusFormat.getInstance();

    public static void generateRequestMessageAuthenticator(RadiusPacket request, String sharedSecret) throws IOException, InvalidKeyException, NoSuchAlgorithmException
    {
        byte[] hash = new byte[16];
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        request.overwriteAttribute(AttributeFactory.newAttribute(AttributeDictionary.MESSAGE_AUTHENTICATOR, hash, request.isRecyclable()));
        format.packPacket(request, sharedSecret, buffer, true);
        System.arraycopy(MD5.hmac_md5(buffer.array(), 0, buffer.position(), sharedSecret.getBytes()), 0, hash, 0, 16);
	}
    
    public static void generateResponseMessageAuthenticator(RadiusPacket request, RadiusPacket reply, String sharedSecret) throws IOException, InvalidKeyException, NoSuchAlgorithmException
    {
        byte[] hash = new byte[16];
        byte[] requestAuth = request.getAuthenticator();
        byte[] replyAuth = reply.getAuthenticator();
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        reply.setAuthenticator(requestAuth);
        reply.overwriteAttribute(AttributeFactory.newAttribute(AttributeDictionary.MESSAGE_AUTHENTICATOR, hash, reply.isRecyclable()));
        format.packPacket(reply, sharedSecret, buffer, true);
        System.arraycopy(MD5.hmac_md5(buffer.array(), 0, buffer.position(), sharedSecret.getBytes()), 0, hash, 0, 16);
        reply.setAuthenticator(replyAuth);
	}
    
    public static Boolean verifyRequest(RadiusPacket request, String sharedSecret) throws IOException, InvalidKeyException, NoSuchAlgorithmException
    {
        byte[] hash = new byte[16];
        ByteBuffer buffer = ByteBuffer.allocate(4096);

        RadiusAttribute attr = request.findAttribute(AttributeDictionary.MESSAGE_AUTHENTICATOR);
        if (attr == null) return null;
        
        byte[] pval = attr.getValue().getBytes();
        attr.setValue(hash);
        
        format.packPacket(request, sharedSecret, buffer, true);
        System.arraycopy(MD5.hmac_md5(buffer.array(), 0, buffer.position(), sharedSecret.getBytes()), 0, hash, 0, 16);

        attr.setValue(pval);
        
        return new Boolean(Arrays.equals(pval, hash));
    }

    public static Boolean verifyReply(byte[] requestAuth, RadiusPacket reply, String sharedSecret) throws IOException, InvalidKeyException, NoSuchAlgorithmException
    {
        byte[] replyAuth = reply.getAuthenticator();
        byte[] hash = new byte[16];

        ByteBuffer buffer = ByteBuffer.allocate(4096);

        RadiusAttribute attr = reply.findAttribute(AttributeDictionary.MESSAGE_AUTHENTICATOR);
        if (attr == null) return null;
        
        byte[] pval = attr.getValue().getBytes();
        attr.setValue(hash);
        
        reply.setAuthenticator(requestAuth);

        format.packPacket(reply, sharedSecret, buffer, true);
        System.arraycopy(MD5.hmac_md5(buffer.array(), 0, buffer.position(), sharedSecret.getBytes()), 0, hash, 0, 16);

        reply.setAuthenticator(replyAuth);
        
        return new Boolean(Arrays.equals(pval, hash));
    }
}
