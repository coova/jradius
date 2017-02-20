package net.jradius.tls;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.NullDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.DSASigner;

class TlsDSSSigner implements TlsSigner
{
    public byte[] calculateRawSignature(AsymmetricKeyParameter privateKey, byte[] md5andsha1)
        throws CryptoException
    {
        // Note: Only use the SHA1 part of the hash
        Signer sig = new DSADigestSigner(new DSASigner(), new NullDigest());
        sig.init(true, privateKey);
        sig.update(md5andsha1, 16, 20);
        return sig.generateSignature();
    }

    public Signer createVerifyer(AsymmetricKeyParameter publicKey)
    {
        Signer s = new DSADigestSigner(new DSASigner(), new SHA1Digest());
        s.init(false, publicKey);
        return s;
    }
}
