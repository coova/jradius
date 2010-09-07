/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (c) 2009 Coova Technologies, LLC <support@coova.com>
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package net.jradius.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

public class KeyStoreUtil 
{
	public static void loadBC()
	{
        if (java.security.Security.getProvider("BC") == null)
        	java.security.Security.addProvider(new BouncyCastleProvider());
	}
	
	public static KeyManager[] loadKeyManager(String type, InputStream in, String password) throws Exception
	{
		loadBC();
		
		final char[] pwd = (password == null || password.length() == 0) ? null : password.toCharArray();

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
				
				KeyStore ksKeys = KeyStore.getInstance("JKS");
				ksKeys.load(null, pwd == null ? "".toCharArray() : pwd);

				ksKeys.setCertificateEntry("", cert);
				ksKeys.setKeyEntry("", key, pwd == null ? "".toCharArray() : pwd, new Certificate[]{cert});
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(ksKeys, pwd == null ? "".toCharArray() : pwd);

				return kmf.getKeyManagers();
				
/*
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
    */
			}
			else
			{
				throw new RuntimeException("Could not load PEM source");
			}
		}

		KeyStore ksKeys = KeyStore.getInstance(type);
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

	public static X509Certificate loadCertificateFromPEM(InputStream in, final char[] pwd) throws Exception
	{
		loadBC();

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
				return (X509Certificate) obj;
			}
		}
		
		return null;
	}
	
	public static TrustManager[] loadTrustManager(String type, InputStream in, String password) throws Exception
	{
		loadBC();

		char[] pwd = (password == null || password.length() == 0) ? null : password.toCharArray();

		if (type.equalsIgnoreCase("pem"))
		{
			final X509Certificate cert = loadCertificateFromPEM(in, pwd);

			KeyStore ksKeys = KeyStore.getInstance("JKS");
			ksKeys.load(null, pwd == null ? "".toCharArray() : pwd);

			ksKeys.setCertificateEntry("", cert);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ksKeys);

			return tmf.getTrustManagers();
			
/*
			return new TrustManager[] { new X509TrustManager()
		    {
		        public void checkClientTrusted(X509Certificate[] chain, String authType) { }
		        public void checkServerTrusted(X509Certificate[] chain, String authType) { }
		        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[] { cert }; }
		    }};
*/
		}

		KeyStore caKeys = KeyStore.getInstance(type);
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
		loadBC();

		return new TrustManager[] { new X509TrustManager()
	    {
	        public void checkClientTrusted(X509Certificate[] chain, String authType) { }
	        public void checkServerTrusted(X509Certificate[] chain, String authType) { }
	        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
	    }};
	}
}