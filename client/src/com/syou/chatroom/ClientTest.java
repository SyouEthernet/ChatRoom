package com.syou.chatroom;

import com.syou.chatroom.bean.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientTest {
    private static boolean done = false;

    public static void main(String[] args) throws IOException {
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        if (info == null) {
            return;
        }

        // current connection size
        int size = 0;
        final List<TCPClient> tcpClients = new ArrayList<>();
        for (int i = 0; i< 10; i++) {
            try {
                TCPClient tcpClient = TCPClient.startWith(info);
                if (tcpClient == null) {
                    System.out.println("connect error");
                }
                tcpClients.add(tcpClient);
                System.out.println("connect success.");
            } catch (Exception e) {
                System.out.println("connect error");
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.in.read();
        
        Runnable runnable = () -> {
            while(!done) {
                for (TCPClient tcpClient :
                        tcpClients) {
                    tcpClient.send("Hello.");
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        
        Thread thread = new Thread(runnable);
        thread.start();

        System.in.read();

        done = true;

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (TCPClient tcpClient :
                tcpClients) {
            tcpClient.exit();
        }
    }
}
