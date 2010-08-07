package net.jradius.tls;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

interface TlsSigner
{
    byte[] calculateRawSignature(AsymmetricKeyParameter privateKey, byte[] md5andsha1)
        throws CryptoException;

    Signer createVerifyer(AsymmetricKeyParameter publicKey);
}
