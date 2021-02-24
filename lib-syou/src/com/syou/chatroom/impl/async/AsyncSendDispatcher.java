package com.syou.chatroom.impl.async;

import com.syou.chatroom.core.IoArgs;
import com.syou.chatroom.core.SendDispacher;
import com.syou.chatroom.core.SendPacket;
import com.syou.chatroom.core.Sender;
import com.syou.chatroom.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSendDispatcher implements SendDispacher, IoArgs.IoArgsEventProcessor {
    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isSending = new AtomicBoolean();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);


    private IoArgs ioArgs = new IoArgs();
    private SendPacket<?> packetTemp;

    //
    private ReadableByteChannel packetChannel;
    private long total;
    private int position;

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
        sender.setSendListener(this);
    }

    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        if (isSending.compareAndSet(false, true)) {
            sendNextPacket();
        }
    }

    @Override
    public void cancel(SendPacket packet) {

    }

    private SendPacket takePacket() {
        SendPacket packet = queue.poll();
        if (packet != null && packet.isCanceled()) {
            // packet is canceled, do not send
            return takePacket();
        }
        return packet;
    }

    private void sendNextPacket() {
        SendPacket temp = packetTemp;
        if (temp != null) {
            CloseUtils.close(temp);
        }

        packetTemp = takePacket();
        SendPacket packet = packetTemp;
        if (packet == null) {
            isSending.set(false);
            return;
        }

        total = packet.length();
        position = 0;

        sendCurrentPacket();
    }

    private void sendCurrentPacket() {
        if (position >= total) {
            completePacket(position == total);
            sendNextPacket();
            return;
        }

        try {
            sender.postSendAsync();
        } catch (IOException e) {
            closeAndNotify();
        }

    }

    private void completePacket(boolean isSucceed) {
        SendPacket packet = this.packetTemp;
        if (packet == null) {
            return;
        }
        CloseUtils.close(packet);
        CloseUtils.close(packetChannel);

        packetTemp = null;
        packetChannel = null;
        total = 0;
        position = 0;
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }


    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            // exception close
            completePacket(false);
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        if (packetChannel == null) {
            packetChannel = Channels.newChannel(packetTemp.open());
            args.limit(4);
            args.writeLength((int) packetTemp.length());
        } else {
            args.limit((int) Math.min(args.capacity(), total - position));
            try {
                int count = args.readFrom(packetChannel);
                position += count;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        sendCurrentPacket();
    }
}
