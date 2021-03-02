package com.syou.chatroom.impl.async;

import com.syou.chatroom.core.IoArgs;
import com.syou.chatroom.core.SendDispacher;
import com.syou.chatroom.core.SendPacket;
import com.syou.chatroom.core.Sender;
import com.syou.chatroom.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSendDispatcher implements SendDispacher, IoArgs.IoArgsEventProcessor, AsyncPacketReader.PacketProvider {
    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isSending = new AtomicBoolean();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final AsyncPacketReader reader = new AsyncPacketReader(this);
    private final Object queueLock = new Object();

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
        sender.setSendListener(this);
    }

    @Override
    public void send(SendPacket packet) {
        synchronized (queueLock) {
            queue.offer(packet);
            if (isSending.compareAndSet(false, true)) {
                if (reader.requestTakePacket()) {
                    requestSend();
                }
            }
        }
    }

    @Override
    public void cancel(SendPacket packet) {
        boolean ret;
        synchronized (queueLock) {
            ret = queue.remove(packet);
        }
        if (ret) {
            packet.cancel();
            return;
        }

        reader.cancel(packet);
    }

    @Override
    public SendPacket takePacket() {
        SendPacket packet;
        synchronized (queueLock) {
            packet = queue.poll();
            if (packet == null) {
                // 队列为空，取消发送
                isSending.set(false);
                return null;
            }
        }

        if (packet.isCanceled()) {
            // packet is canceled, do not send
            return takePacket();
        }
        return packet;
    }

    @Override
    public void completedPacket(SendPacket packet, boolean isSucceed) {
        CloseUtils.close(packet);
    }

    /**
     * request network and send
     */
    private void requestSend() {
        try {
            sender.postSendAsync();
        } catch (IOException e) {
            closeAndNotify();
        }

    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }


    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            // reader close
            reader.close();
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        return reader.fillData();
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        if (args != null) {
            e.printStackTrace();
        } else {
            // TODO
        }
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        if (reader.requestTakePacket()) {
            requestSend();
        }
    }
}
