package com.syou.chatroom;

import com.syou.chatroom.bean.ServerInfo;
import com.syou.chatroom.core.Connector;
import com.syou.chatroom.core.Packet;
import com.syou.chatroom.core.ReceivePacket;
import com.syou.chatroom.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public class TCPClient extends Connector {
    private final File cachePath;

    public TCPClient(SocketChannel socketChannel, File cachePath) throws IOException {
        this.cachePath = cachePath;
        setup(socketChannel);
    }

    public void exit() {
        CloseUtils.close(this);
    }


    @Override
    protected File createNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }

    @Override
    protected void onReceivedNewPacket(ReceivePacket packet) {
        super.onReceivedNewPacket(packet);
        if (packet.type() == Packet.TYPE_MEMORY_STRING) {
            String string = (String) packet.entity();
            System.out.println(key.toString() + ":" + string);
        }
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("Connect closed, cannot read data");
    }

    public static TCPClient startWith(ServerInfo info, File cachePath) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();

        //connect server
        socketChannel.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()));

        System.out.println("start to connect server~");
        System.out.println("client info: " + socketChannel.getLocalAddress().toString());
        System.out.println("server info: " + socketChannel.getRemoteAddress().toString());

        try {
            // get client
            return new TCPClient(socketChannel, cachePath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exit in exception.");
            CloseUtils.close(socketChannel);
        }
        return null;
    }
}
