package com.syou.chatroom;

import com.syou.chatroom.bean.ServerInfo;
import com.syou.chatroom.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient {
    private final Socket socket;
    private final ReadHandler readHandler;
    private final PrintStream printStream;

    public TCPClient(Socket socket, ReadHandler readHandler) throws IOException {
        this.socket = socket;
        this.readHandler = readHandler;
        this.printStream = new PrintStream(socket.getOutputStream());
    }

    public void exit(){
        readHandler.exit();
        CloseUtils.close(printStream);
        CloseUtils.close(socket);
    }

    public void send(String msg){
        printStream.println(msg);
    }

    public static TCPClient startWith(ServerInfo info) throws IOException {
        Socket socket = new Socket();
        // set read timeout
        socket.setSoTimeout(3000);

        //connect server
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        System.out.println("start to connect server~");
        System.out.println("client info: " + socket.getLocalAddress() + "\tport:" + socket.getLocalPort());
        System.out.println("server info: " + socket.getInetAddress() + "\tport:" + socket.getPort());

        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();

            // get client
            return new TCPClient(socket, readHandler);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exit in exception.");
            CloseUtils.close(socket);
        }
        return null;
    }

    private static void write(Socket client) throws IOException {
        // read from keyboard
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // get socket outputStream and trans to printStream
        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        while (true) {
            // read from keyboard
            String str = input.readLine();
            // send to server
            socketPrintStream.println(str);
            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
        }

        // release resource
        socketPrintStream.close();
    }

    static class ReadHandler extends Thread {
        private boolean done = false;
        private final InputStream inputStream;

        public ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            super.run();
            try {
                // get inputStream for data receive
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    // get one line data
                    String str;
                    try {
                        str = socketInput.readLine();
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    if (str == null) {
                        System.out.println("connection closed,cannot read data!");
                        break;
                    }
                    System.out.println(str);
                } while (!done);
                socketInput.close();
            } catch (Exception e) {
                e.printStackTrace();
                if (!done) {
                    System.out.println("exception disconnect" + e.getMessage());
                }
            } finally {
                CloseUtils.close(inputStream);
            }
        }

        void exit() {
            done = true;
            CloseUtils.close(inputStream);
        }
    }
}
