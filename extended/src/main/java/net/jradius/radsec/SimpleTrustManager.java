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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.jradius.util.KeyStoreUtil;

import org.springframework.beans.factory.InitializingBean;

public class SimpleTrustManager implements X509TrustManager, InitializingBean
{
	private Boolean trustAll = Boolean.FALSE;
	
	private String certFile;
	private String certFileType;
	private String certFilePassword;

	private X509TrustManager trustManager;

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { 
    	for (X509Certificate c : chain)
    		System.err.println("Checking Client: "+c.getSubjectDN());
		trustManager.checkClientTrusted(chain, authType);
    }
    
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    	for (X509Certificate c : chain)
    		System.err.println("Checking Server: "+c.getSubjectDN());
		trustManager.checkServerTrusted(chain, authType);
    }

    public X509Certificate[] getAcceptedIssuers() { 
    	if (trustAll.booleanValue()) return new X509Certificate[0];
    	X509Certificate[] chain = trustManager.getAcceptedIssuers();
    	for (X509Certificate c : chain)
    		System.err.println("Accepted Issuer: "+c.getSubjectDN());
    	return chain; 
    }

    public void afterPropertiesSet() throws Exception {
    	TrustManager[] managers = KeyStoreUtil.loadTrustManager(certFileType, new FileInputStream(new File(certFile)), certFilePassword);
    	if (managers != null && managers.length != 0)
    		trustManager = (X509TrustManager) managers[0];
	}
	
    public void setCertFile(String keyFile) {
		this.certFile = keyFile;
	}
	
    public void setCertFileType(String keyFileType) {
		this.certFileType = keyFileType;
	}

	public void setCertFilePassword(String keyFilePassword) {
		this.certFilePassword = keyFilePassword;
	}

	public void setTrustAll(Boolean trustAll) {
		this.trustAll = trustAll;
	}
}
