package net.jradius.client.auth.tls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of the TLS 1.0 record layer.
 */
class RecordStream
{
    private TlsProtocolHandler handler;
    private InputStream is;
    private OutputStream os;
    private CombinedHash hash;
    private TlsCipher readCipher = null;
    private TlsCipher writeCipher = null;

    RecordStream(TlsProtocolHandler handler, InputStream is, OutputStream os)
    {
        this.handler = handler;
        this.is = is;
        this.os = os;
        this.hash = new CombinedHash();
        this.readCipher = new TlsNullCipher();
        this.writeCipher = this.readCipher;
    }

    public RecordStream(TlsProtocolHandler handler)
    {
        this.handler = handler;
        this.hash = new CombinedHash();
        this.readCipher = new TlsNullCipher();
        this.writeCipher = this.readCipher;
	}

	void clientCipherSpecDecided(TlsCipher tlsCipher)
    {
        this.writeCipher = tlsCipher;
    }

    void serverClientSpecReceived()
    {
        this.readCipher = this.writeCipher;
    }

    public void setInputStream(ByteArrayInputStream stream)
    {
    	this.is = stream;
    }

    public void setOutputStream(ByteArrayOutputStream stream)
    {
    	this.os = stream;
    }

    public void readData() throws IOException
    {
        short type = TlsUtils.readUint8(is);
        TlsUtils.checkVersion(is, handler);
        int size = TlsUtils.readUint16(is);
        byte[] buf = decodeAndVerify(type, is, size);
        handler.processData(type, buf, 0, buf.length);
    }

    protected byte[] decodeAndVerify(short type, InputStream is, int len) throws IOException
    {
        byte[] buf = new byte[len];
        TlsUtils.readFully(buf, is);
        return readCipher.decodeCiphertext(type, buf, 0, buf.length);
    }

    protected void writeMessage(short type, byte[] message, int offset, int len) throws IOException
    {
        if (type == 22) // TlsProtocolHandler.RL_HANDSHAKE
        {
            updateHandshakeData(message, offset, len);
        }
        byte[] ciphertext = writeCipher.encodePlaintext(type, message, offset, len);
        byte[] writeMessage = new byte[ciphertext.length + 5];
        TlsUtils.writeUint8(type, writeMessage, 0);
        TlsUtils.writeUint8((short)3, writeMessage, 1);
        TlsUtils.writeUint8((short)1, writeMessage, 2);
        TlsUtils.writeUint16(ciphertext.length, writeMessage, 3);
        System.arraycopy(ciphertext, 0, writeMessage, 5, ciphertext.length);
        os.write(writeMessage);
        os.flush();
    }

    public boolean hasMore() throws IOException
    {
    	return (is.available() > 0);
    }

    void updateHandshakeData(byte[] message, int offset, int len)
    {
        hash.update(message, offset, len);
    }

    byte[] getCurrentHash()
    {
        return doFinal(new CombinedHash(hash));
    }

    protected void close() throws IOException
    {
        IOException e = null;
        try
        {
            is.close();
        }
        catch (IOException ex)
        {
            e = ex;
        }
        try
        {
            os.close();
        }
        catch (IOException ex)
        {
            e = ex;
        }
        if (e != null)
        {
            throw e;
        }
    }

    protected void flush() throws IOException
    {
        os.flush();
    }

    private static byte[] doFinal(CombinedHash ch)
    {
        byte[] bs = new byte[ch.getDigestSize()];
        ch.doFinal(bs, 0);
        return bs;
    }
}
