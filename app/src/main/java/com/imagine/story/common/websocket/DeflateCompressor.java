package com.imagine.story.common.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * DEFLATE (<a href="http://tools.ietf.org/html/rfc1951">RFC 1951</a>) compressor implementation
 * 压缩
 */
class DeflateCompressor {
    public static byte[] compress(byte[] input) throws IOException {
        // Destination where compressed data will be stored.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Create a compressor.
        Deflater deflater = createDeflater();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater);

        // Compress the data.
        //
        // Some other implementations such as Jetty and Tyrus use
        // Deflater.deflate(byte[], int, int, int) with Deflate.SYNC_FLUSH,
        // but this implementation does not do it intentionally because the
        // method and the constant value are not available before Java 7.
        dos.write(input, 0, input.length);
        dos.close();

        // Release the resources held by the compressor.
        deflater.end();

        // Retrieve the compressed data.
        return baos.toByteArray();
    }


    private static Deflater createDeflater() {
        // The second argument (nowrap) is true to get only DEFLATE
        // blocks without the ZLIB header and checksum fields.
        return new Deflater(Deflater.DEFAULT_COMPRESSION, true);
    }
}

