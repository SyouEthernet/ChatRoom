package com.syou.chatroom.impl.async;

import com.syou.chatroom.box.StringReceivePacket;
import com.syou.chatroom.core.IoArgs;
import com.syou.chatroom.core.ReceiveDispatcher;
import com.syou.chatroom.core.ReceivePacket;
import com.syou.chatroom.core.Receiver;
import com.syou.chatroom.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncReceiveDispatcher implements ReceiveDispatcher, IoArgs.IoArgsEventProcessor {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final Receiver receiver;
    private final ReceivePacketCallback callback;

    private IoArgs ioArgs = new IoArgs();
    private ReceivePacket<?> packetTemp;
    private WritableByteChannel packetChannel;
    private long total;
    private int position;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveListener(this);
        this.callback = callback;
    }

    @Override
    public void start() {
        registerReceive();
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            completePacket(false);
        }
    }

    private void registerReceive() {
        try {
            receiver.postReceiveAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    /**
     * complete data receiver operation
     */
    private void completePacket(boolean isSucceed) {
        ReceivePacket packet = this.packetTemp;
        CloseUtils.close(packet);
        packetTemp = null;

        WritableByteChannel channel = this.packetChannel;
        CloseUtils.close(channel);
        packetChannel = null;

        if (packet != null) {
            callback.onReceivePacketCompleted(packet);
        }
    }

    /**
     * parse data and assemble
     *
     * @param args
     */
    private void assemblePacket(IoArgs args) {
        if (packetTemp == null) {
            int length = args.readLength();
            packetTemp = new StringReceivePacket(length);
            packetChannel = Channels.newChannel(packetTemp.open());

            total = length;
            position = 0;
        }
        try {
            int count = args.writeTo(packetChannel);

            position += count;

            if (position == total) {
                completePacket(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            completePacket(false);
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        int receieSize;
        if (packetTemp == null) {
            receieSize = 4;
        } else {
            receieSize = (int) Math.min(total - position, args.capacity());
        }
        // set data size limit
        args.limit(receieSize);
        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        assemblePacket(args);
        // next data
        registerReceive();
    }
}
