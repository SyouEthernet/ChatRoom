package com.syou.chatroom;

import com.syou.chatroom.constants.TCPConstants;
import com.syou.chatroom.core.IoContext;
import com.syou.chatroom.impl.IoSelectorProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
    public static void main(String[] args) throws IOException {
        IoContext.setup().ioPorvider(new IoSelectorProvider()).start();

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("start TCP Server failed!");
        }

        UDPProvider.start(TCPConstants.PORT_SERVER);


        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while (!"00bye00".equalsIgnoreCase(str));

        UDPProvider.stop();
        tcpServer.stop();
        IoContext.close();
    }
}
