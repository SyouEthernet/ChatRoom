package com.syou.chatroom.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IoArgs {
    private int limit = 5;
    private byte[] byteBuffer = new byte[5];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    /**
     * read data frome bytes
     *
     * @param bytes
     * @param offset
     * @return
     */
    public int readFrom(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.put(bytes, offset, size);
        return size;
    }

    /**
     * write data to bytes
     *
     * @param bytes
     * @param offset
     * @return
     */
    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        return size;
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
        this.limit = limit;
    }

    public void writeLength(int total) {
        buffer.putInt(total);
    }

    public int readLength() {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleteed(IoArgs args);
    }
}
