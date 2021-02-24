package com.syou.chatroom.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * public data class
 * provide type and length
 */
public abstract class Packet<Stream extends Closeable> implements Closeable {
    //BYTE
    public static final byte TYPE_MEMORY_BYTES = 1;
    //String
    public static final byte TYPE_MEMORY_STRING = 2;
    //File
    public static final byte TYPE_STREAM_FILE = 3;
    //stream direct
    public static final byte TYPE_STREAM_DIRECT = 4;

    protected long length;
    private Stream stream;

    public long length() {
        return length;
    }

    protected abstract Stream createStream();

    public abstract byte type();

    protected void closeStream(Stream stream) throws IOException {
        stream.close();
    }

    public final Stream open() {
        if (stream == null) {
            stream = createStream();
        }
        return stream;
    }

    @Override
    public final void close() throws IOException {
        if (stream != null) {
            closeStream(stream);
            stream = null;
        }
    }
}
