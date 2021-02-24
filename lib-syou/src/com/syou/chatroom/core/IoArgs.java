package com.syou.chatroom.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutorService;

public class IoArgs {
    private int limit = 5;
    private ByteBuffer buffer = ByteBuffer.allocate(5);

    /**
     * read data frome bytes
     */
    public int readFrom(ReadableByteChannel channel) throws IOException{
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
        startWriting();
        buffer.putInt(total);
        finishWriting();
    }

    public int readLength() {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public interface IoArgsEventProcessor {
        IoArgs provideIoArgs();

        void onConsumeFailed(IoArgs args, Exception e);

        void onConsumeCompleted(IoArgs args);
    }
}
