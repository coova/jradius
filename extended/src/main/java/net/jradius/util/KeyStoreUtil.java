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
import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.AlgorithmParameters;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.InputDecryptorProvider;

public class KeyStoreUtil 
{
	public static void loadBC()
	{
		try {
			if (java.security.Security.getProvider("BC") == null)
				java.security.Security.addProvider(new BouncyCastleProvider());
		} catch (Throwable e) { }
	}
	
	public static KeyManager[] loadKeyManager(String type, InputStream in, String password) throws Exception
	{
		loadBC();
		
		final char[] pwd = (password == null || password.length() == 0) ? null : password.toCharArray();

		if (type.equalsIgnoreCase("pem"))
		{
			Object obj;
                        PrivateKey key = null;
			X509Certificate cert = null;
			KeyPair keyPair = null; 	

			PEMParser pemParser = new PEMParser(new InputStreamReader(in));
			try {
				while ((obj = pemParser.readObject()) != null)
				{
					if(obj instanceof X509CertificateHolder) {
						cert = new JcaX509CertificateConverter()
							.setProvider("BC")
							.getCertificate((X509CertificateHolder)obj);
					} else if(obj instanceof PrivateKeyInfo) {
						key = BouncyCastleProvider.getPrivateKey((PrivateKeyInfo)obj);
					} else if(obj instanceof PKCS8EncryptedPrivateKeyInfo) {
						InputDecryptorProvider pkcs8Prov = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password.toCharArray());
						JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
						key = converter.getPrivateKey(((PKCS8EncryptedPrivateKeyInfo)obj).decryptPrivateKeyInfo(pkcs8Prov));	
					} else if(obj instanceof PEMKeyPair) {
						JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
						keyPair = converter.getKeyPair((PEMKeyPair)obj);
					} else if(obj instanceof PEMEncryptedKeyPair) {
						PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
                                                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
    						keyPair = converter.getKeyPair(((PEMEncryptedKeyPair)obj).decryptKeyPair(decProv));
					}
				}
			} finally {
				pemParser.close();
			}
					
			if ((key != null || keyPair != null) && cert != null)
			{
				// final PrivateKey key = keyPair != null ? ((KeyPair)keyPair).getPrivate() : (PrivateKey) keyObj;
				KeyStore ksKeys = KeyStore.getInstance("JKS");
				ksKeys.load(null, pwd == null ? "".toCharArray() : pwd);

				ksKeys.setCertificateEntry("", cert);
				ksKeys.setKeyEntry("", key, pwd == null ? "".toCharArray() : pwd, new Certificate[]{cert});
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(ksKeys, pwd == null ? "".toCharArray() : pwd);

				return kmf.getKeyManagers();
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

		PEMParser pemParser = new PEMParser(new InputStreamReader(in));

		Object obj;
		while ((obj = pemParser.readObject()) != null)
		{
			if(obj instanceof X509CertificateHolder) {
				return new JcaX509CertificateConverter()
					.setProvider("BC")
					.getCertificate((X509CertificateHolder)obj);
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
