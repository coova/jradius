package net.jradius.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

public class KeyStoreUtil 
{
	public static KeyManager[] loadKeyManager(String type, InputStream in, String password) throws Exception
	{
		final char[] pwd = (password == null || password.isEmpty()) ? null : password.toCharArray();

		if (type.equalsIgnoreCase("pem"))
		{
			PEMReader pemReader = new PEMReader(new InputStreamReader(in), new PasswordFinder() {
				public char[] getPassword() {
					return pwd;
				}
			});
			
			Object obj, keyObj=null, certObj=null, keyPair=null;

			while ((obj = pemReader.readObject()) != null)
			{
				if (obj instanceof X509Certificate) certObj = obj;
				else if (obj instanceof PrivateKey) keyObj = obj;
				else if (obj instanceof KeyPair) keyPair = obj;
			}
					
			if ((keyObj != null || keyPair != null) && certObj != null)
			{
				final PrivateKey key = keyPair != null ? ((KeyPair)keyPair).getPrivate() : (PrivateKey) keyObj;
				final X509Certificate cert = (X509Certificate) certObj;
				return new KeyManager[] { new X509KeyManager()
			    {
					public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
						return "a";
					}

					public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
						return "a";
					}

					public X509Certificate[] getCertificateChain(String alias) {
						return new X509Certificate[] { cert };
					}

					public String[] getClientAliases(String keyType, Principal[] issuers) {
						return new String[] {"a"};
					}

					public PrivateKey getPrivateKey(String alias) {
						return key;
					}

					public String[] getServerAliases(String keyType, Principal[] issuers) {
						return new String[] {"a"};
					}
			    }};
			}
			else
			{
				throw new RuntimeException("Could not load PEM source");
			}
		}


		KeyStore ksKeys = KeyStore.getInstance(type, "BC");
        ksKeys.load(in, pwd);

        Enumeration<String> aliases = ksKeys.aliases();
		while (aliases.hasMoreElements()) {
			String alias = (String) aliases.nextElement();
			System.err.println("KeyStore Alias: "+alias);
		}

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ksKeys, pwd);
        
        return kmf.getKeyManagers();
	}

	public static TrustManager[] loadTrustManager(String type, InputStream in, String password) throws Exception
	{
		final char[] pwd = (password == null || password.isEmpty()) ? null : password.toCharArray();

		if (type.equalsIgnoreCase("pem"))
		{
			PEMReader pemReader = new PEMReader(new InputStreamReader(in), new PasswordFinder() {
				public char[] getPassword() {
					return pwd;
				}
			});
			Object obj;
			while ((obj = pemReader.readObject()) != null)
			{
				if (obj instanceof X509Certificate)
				{
					final X509Certificate cert = (X509Certificate) obj;
					return new TrustManager[] { new X509TrustManager()
				    {
				        public void checkClientTrusted(X509Certificate[] chain, String authType) { }
				        public void checkServerTrusted(X509Certificate[] chain, String authType) { }
				        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[] { cert }; }
				    }};
				}
			}
		}

		KeyStore caKeys = KeyStore.getInstance(type, "BC");
        caKeys.load(in, pwd);

        Enumeration<String> aliases = caKeys.aliases();
		while (aliases.hasMoreElements()) {
			String alias = (String) aliases.nextElement();
			System.err.println("KeyStore Alias: "+alias);
		}

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(caKeys);
        
        return tmf.getTrustManagers();
	}
	
	public static TrustManager[] trustAllManager()
	{
		return new TrustManager[] { new X509TrustManager()
	    {
	        public void checkClientTrusted(X509Certificate[] chain, String authType) { }
	        public void checkServerTrusted(X509Certificate[] chain, String authType) { }
	        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
	    }};
	}
}