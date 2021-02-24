package com.syou.chatroom.handler;

import com.syou.chatroom.Foo;
import com.syou.chatroom.core.Connector;
import com.syou.chatroom.core.Packet;
import com.syou.chatroom.core.ReceivePacket;
import com.syou.chatroom.utils.CloseUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler extends Connector {
    private final File cachePath;
    private final ClientHandlerCallback clientHandlerCallback;
    private final String clientInfo;

    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback, File cachePath) throws IOException {
        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = "A["+socketChannel.getRemoteAddress().toString()+"]P["+socketChannel.socket().getPort()+"]";
        this.cachePath = cachePath;

        System.out.println("new socketChannel connected:" + clientInfo);

        setup(socketChannel);
    }

    public void exit() {
        CloseUtils.close(this);
        System.out.println("client finished:" + clientInfo);
    }

    @Override
    protected File createNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitBySelf();;
    }

    @Override
    protected void onReceivedNewPacket(ReceivePacket packet) {
        super.onReceivedNewPacket(packet);
        if (packet.type() == Packet.TYPE_MEMORY_STRING) {
            String string = (String) packet.entity();
            System.out.println(key.toString() + ":" + string);
            clientHandlerCallback.onNewMessageArrived(this, string);
        }
    }

    private void exitBySelf() {
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    public interface ClientHandlerCallback {
        void onSelfClosed(ClientHandler handler);
        void onNewMessageArrived(ClientHandler clientHandler, String str);
    }
}
