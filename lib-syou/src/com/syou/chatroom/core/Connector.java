package com.syou.chatroom.core;

import com.syou.chatroom.box.ByteReceivePacket;
import com.syou.chatroom.box.FileReceivePacket;
import com.syou.chatroom.box.StringReceivePacket;
import com.syou.chatroom.box.StringSendPacket;
import com.syou.chatroom.impl.SocketChannelAdapter;
import com.syou.chatroom.impl.async.AsyncReceiveDispatcher;
import com.syou.chatroom.impl.async.AsyncSendDispatcher;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public abstract class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {
    protected UUID key = UUID.randomUUID();
    private SocketChannel channel;
    private Sender sender;
    private Receiver receiver;
    private SendDispacher sendDispacher;
    private ReceiveDispatcher receiveDispatcher;

    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;

        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, context.getIoProvider(), this);

        this.sender = adapter;
        this.receiver = adapter;

        sendDispacher = new AsyncSendDispatcher(sender);
        receiveDispatcher = new AsyncReceiveDispatcher(receiver, receivePacketCallback);

        //start receive
        receiveDispatcher.start();
    }

    public void send(String msg) {
        SendPacket packet = new StringSendPacket(msg);
        sendDispacher.send(packet);
    }

    public void send(SendPacket packet) {
        sendDispacher.send(packet);
    }

    protected abstract File createNewReceiveFile();

    @Override
    public void close() throws IOException {
        receiveDispatcher.close();
        sendDispacher.close();
        sender.close();
        receiver.close();
        channel.close();
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    protected  void onReceivedNewPacket(ReceivePacket packet) {
        System.out.println(key.toString() + ":[New Packet]-Type:"
                + packet.type() + ", length:" + packet.length());
    }

    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiveDispatcher.ReceivePacketCallback() {
        @Override
        public ReceivePacket<?, ?> onArrivedNewPacket(byte type, long length) {
            switch (type) {
                case Packet.TYPE_MEMORY_BYTES:
                    return new ByteReceivePacket(length);
                case Packet.TYPE_MEMORY_STRING:
                    return new StringReceivePacket(length);
                case Packet.TYPE_STREAM_FILE:
                    return new FileReceivePacket(length, createNewReceiveFile());
                case Packet.TYPE_STREAM_DIRECT:
                    return new ByteReceivePacket(length);
                default:
                    throw new UnsupportedOperationException("Unsupported packet type:" + type);
            }
        }

        @Override
        public void onReceivePacketCompleted(ReceivePacket packet) {
            onReceivedNewPacket(packet);
        }
    };
}
