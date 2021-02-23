package com.syou.chatroom;

import com.syou.chatroom.bean.ServerInfo;
import com.syou.chatroom.core.Connector;
import com.syou.chatroom.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TCPClient extends Connector {

    public TCPClient(SocketChannel socketChannel) throws IOException {
       setup(socketChannel);
    }

    public void exit(){
        CloseUtils.close(this);
    }


    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        System.out.println("Connect closed, cannot read data");
    }

    public static TCPClient startWith(ServerInfo info) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();

        //connect server
        socketChannel.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()));

        System.out.println("start to connect server~");
        System.out.println("client info: " + socketChannel.getLocalAddress().toString());
        System.out.println("server info: " + socketChannel.getRemoteAddress().toString());

        try {
            // get client
            return new TCPClient(socketChannel);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exit in exception.");
            CloseUtils.close(socketChannel);
        }
        return null;
    }
}
