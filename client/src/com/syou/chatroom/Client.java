package com.syou.chatroom;

import com.syou.chatroom.bean.ServerInfo;

import java.io.*;

public class Client {
    public static void main(String[] args) {
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        if (info != null) {
            TCPClient tcpClient = null;
            try{
                tcpClient = TCPClient.startWith(info);
                if (tcpClient == null) {
                    return;
                }
                write(tcpClient);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (tcpClient != null) {
                    tcpClient.exit();
                }
            }
        }
    }

    private static void write(TCPClient tcpClient) throws IOException {
        // read from keyboard
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));


        while (true) {
            // read from keyboard
            String str = input.readLine();
            // send to server
            tcpClient.send(str);
            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
        }
    }
}
