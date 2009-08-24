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
package net.jradius.radsec;

import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;

import net.jradius.util.KeyStoreUtil;

import org.springframework.beans.factory.InitializingBean;

public class SimpleKeyManager implements X509KeyManager, InitializingBean
{
	private String keyFile;
	private String keyFileType;
	private String keyFilePassword;
	private X509KeyManager keyManager;

	public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
		String alias = keyManager.chooseClientAlias(arg0, arg1, arg2);
		System.err.println("Client Alias: "+alias);
		return alias;
	}

	public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
		String alias = keyManager.chooseServerAlias(arg0, arg1, arg2);
		System.err.println("Server Alias: "+alias);
		return alias;
	}

	public X509Certificate[] getCertificateChain(String arg0) {
		X509Certificate[] certs = keyManager.getCertificateChain(arg0);
		for (X509Certificate cert : certs) System.err.println(arg0+" cert: "+cert.getSubjectDN());
		return certs;
	}

	public String[] getClientAliases(String arg0, Principal[] arg1) {
		String alias[] = keyManager.getClientAliases(arg0, arg1);
		for (String a : alias) System.err.println("Server Alias: "+a);
		return alias;
	}

	public PrivateKey getPrivateKey(String arg0) {
		return keyManager.getPrivateKey(arg0);
	}

	public String[] getServerAliases(String arg0, Principal[] arg1) {
		String alias[] = keyManager.getServerAliases(arg0, arg1);
		for (String a : alias) System.err.println("Server Alias: "+a);
		return alias;
	}

	public void afterPropertiesSet() throws Exception {
		KeyManager keyManagers[] = KeyStoreUtil.loadKeyManager(keyFileType, new FileInputStream(new File(keyFile)), keyFilePassword);
		if (keyManagers == null || keyManagers.length == 0) throw new RuntimeException("could not initialize RadSec keystore");
		keyManager = (X509KeyManager) keyManagers[0];
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	public void setKeyFileType(String keyFileType) {
		this.keyFileType = keyFileType;
	}

	public void setKeyFilePassword(String keyFilePassword) {
		this.keyFilePassword = keyFilePassword;
	}
	
}
