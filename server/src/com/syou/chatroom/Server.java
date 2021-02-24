package com.syou.chatroom;

import com.syou.chatroom.constants.TCPConstants;
import com.syou.chatroom.core.IoContext;
import com.syou.chatroom.impl.IoSelectorProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
    public static void main(String[] args) throws IOException {
        File cachePath = Foo.getCacheDir("server");
        IoContext.setup().ioPorvider(new IoSelectorProvider()).start();

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER, cachePath);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("start TCP Server failed!");
        }

        UDPProvider.start(TCPConstants.PORT_SERVER);


        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            if (str.equalsIgnoreCase("00bye00")) {
                break;
            }
            tcpServer.broadcast(str);
        } while (true);

        UDPProvider.stop();
        tcpServer.stop();
        IoContext.close();
    }
}
