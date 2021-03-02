package com.syou.chatroom.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class IoArgs {
    private int limit = 256;
    private ByteBuffer buffer = ByteBuffer.allocate(256);

    /**
     * read from byte
     * @param bytes
     * @param offset
     * @param count
     * @return
     */
    public int readFrom(byte[] bytes, int offset, int count) {
        int size = Math.min(count, buffer.capacity());
        if (size<=0) {
            return 0;
        }
        buffer.put(bytes, offset, size);
        return size;
    }

    /**
     * read data frome bytes
     */
    public int readFrom(ReadableByteChannel channel) throws IOException {
        int bytesProduced = 0;
        int len;
        do {
            len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        } while (buffer.hasRemaining() && len != 0);
        return bytesProduced;
    }

    /**
     * write data to bytes
     */
    public int writeTo(WritableByteChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        return bytesProduced;
    }

    /**
     * read from channel
     *
     * @param channel
     * @return
     * @throws IOException
     */
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        int bytesProduced = 0;
        int len;
        do {
            len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        } while (buffer.hasRemaining() && len != 0);
        finishWriting();
        return bytesProduced;
    }

    /**
     * write to bytes
     * @param bytes
     * @return
     * @throws IOException
     */
    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        return size;
    }

    /**
     * write to channel
     *
     * @param channel
     * @return
     * @throws IOException
     */
    public int writeTo(SocketChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        return bytesProduced;
    }

    /**
     * start to write data to ioArgs
     */
    public void startWriting() {
        buffer.clear();
        // set limit length
        buffer.limit(limit);
    }

    public void finishWriting() {
        buffer.flip();
    }

    /**
     * set limit
     *
     * @param limit
     */
    public void limit(int limit) {
        this.limit = Math.min(limit, buffer.capacity());
    }

    public int readLength() {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public boolean remained() {
        return buffer.remaining() > 0;
    }

    public int fillEmpty(int size) {
        int fillSize = Math.min(size, buffer.remaining());
        buffer.position(buffer.position() + fillSize);
        return fillSize;
    }

    public int setEmpty(int size) {
        int emptySize = Math.min(size, buffer.remaining());
        buffer.position(buffer.position() + emptySize);
        return emptySize;
    }

    public interface IoArgsEventProcessor {
        IoArgs provideIoArgs();

        void onConsumeFailed(IoArgs args, Exception e);

        void onConsumeCompleted(IoArgs args);
    }
}
