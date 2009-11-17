package net.jradius.client.auth.tls;

import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.signers.GenericSigner;

class TlsRSASigner
    extends GenericSigner
{
    TlsRSASigner()
    {
        super(new PKCS1Encoding(new RSABlindedEngine()), new CombinedHash());
    }
}
