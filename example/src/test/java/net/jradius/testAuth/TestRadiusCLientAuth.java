package net.jradius.testAuth;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

import org.junit.Ignore;
import org.junit.Test;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.EAPTTLSAuthenticator;
import net.jradius.client.auth.PEAPAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.dictionary.Attr_CleartextPassword;
import net.jradius.dictionary.Attr_Password;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.exception.RadiusException;
import net.jradius.exception.UnknownAttributeException;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeFactory;

/**
 * @author jmk <jm+jradius@kubek.fr>
 *
 */
public class TestRadiusCLientAuth {
    
    @Test
    @Ignore("This test require a radius server rinning on localhost")
    public void testPap() throws IOException, UnknownAttributeException, NoSuchAlgorithmException, RadiusException{
         InetAddress localhost = Inet4Address.getLocalHost();
        String secret = "testing123";
        RadiusClient rc = new RadiusClient(localhost,secret,1812,1813,2000);
        AccessRequest accessRequest = new AccessRequest();
        RadiusAuthenticator auth = RadiusClient.getAuthProtocol("pap");
        accessRequest.addAttribute(new Attr_UserName("bob"));
        auth.setPassword(new Attr_CleartextPassword("hello"));
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");

        int retries =0;
        RadiusResponse response = rc.authenticate(accessRequest, auth, 0);
        assertTrue(response instanceof AccessAccept);
    }
    
    @Test
    @Ignore("This test require a radius server rinning on localhost")
    public void testChap() throws IOException, UnknownAttributeException, NoSuchAlgorithmException, RadiusException{
         InetAddress localhost = Inet4Address.getLocalHost();
        String secret = "testing123";
        RadiusClient rc = new RadiusClient(localhost,secret,1812,1813,2000);
        AccessRequest accessRequest = new AccessRequest();
        RadiusAuthenticator auth = RadiusClient.getAuthProtocol("chap");
        accessRequest.addAttribute(new Attr_UserName("bob"));
        auth.setPassword(new Attr_CleartextPassword("hello"));
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");

        int retries =0;
        RadiusResponse response = rc.authenticate(accessRequest, auth, 0);
        assertTrue(response instanceof AccessAccept);
    }
    
    @Test
    @Ignore("This test require a radius server rinning on localhost")
    public void testMsChap() throws IOException, UnknownAttributeException, NoSuchAlgorithmException, RadiusException{
         InetAddress localhost = Inet4Address.getLocalHost();
        String secret = "testing123";
        RadiusClient rc = new RadiusClient(localhost,secret,1812,1813,2000);
        AccessRequest accessRequest = new AccessRequest();
        RadiusAuthenticator auth = RadiusClient.getAuthProtocol("eap-md5");
        accessRequest.addAttribute(new Attr_UserName("bob"));
        auth.setPassword(new Attr_CleartextPassword("hello"));
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");

        int retries =0;
        RadiusResponse response = rc.authenticate(accessRequest, auth, 0);
        assertTrue(response instanceof AccessAccept);
    }
    
    @Test
    @Ignore("This test require a radius server rinning on localhost")
    public void testEapTTLS() throws IOException, UnknownAttributeException, NoSuchAlgorithmException, RadiusException{
         InetAddress localhost = Inet4Address.getLocalHost();
        String secret = "testing123";
        RadiusClient rc = new RadiusClient(localhost,secret,1812,1813,2000);
        AccessRequest accessRequest = new AccessRequest();
        RadiusAuthenticator auth = RadiusClient.getAuthProtocol("eap-ttls");
        assertNotNull(auth);
        accessRequest.addAttribute(new Attr_UserName("bob"));
        accessRequest.addAttribute(new Attr_UserPassword("hello"));
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        int retries =0;
        RadiusResponse response = rc.authenticate(accessRequest, auth, 0);
        assertTrue(response instanceof AccessAccept);
    }
    
    @Test
    @Ignore("This test require a radius server rinning on localhost")
    public void testPeap() throws IOException, UnknownAttributeException, NoSuchAlgorithmException, RadiusException{
         InetAddress localhost = Inet4Address.getLocalHost();
        String secret = "testing123";
        RadiusClient rc = new RadiusClient(localhost,secret,1812,1813,2000);
        AccessRequest accessRequest = new AccessRequest();
        accessRequest.addAttribute(new Attr_UserName("bob2"));
        //accessRequest.addAttribute(new Attr_UserPassword("test"));
        RadiusAuthenticator auth = RadiusClient.getAuthProtocol("peap");
        assertNotNull(auth);
        ((PEAPAuthenticator)auth).setPassword(new Attr_Password("test"));
        ((PEAPAuthenticator)auth).setTrustAll(true);
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        int retries =0;
        RadiusResponse response = rc.authenticate(accessRequest, auth, 0);
        assertTrue(response.getClass().getName(),response instanceof AccessAccept);
    }
}
