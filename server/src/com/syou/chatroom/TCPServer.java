package com.syou.chatroom;

import com.syou.chatroom.handler.ClientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandler.ClientHandlerCallback {
    private final int port;
    private ClientListener mListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingTreadPoolExecutor;

    public TCPServer(int port) {
        this.port = port;
        forwardingTreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            ClientListener clientListener = new ClientListener(port);
            mListener = clientListener;
            clientListener.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (mListener != null) {
            mListener.exit();
        }

        synchronized (TCPServer.this) {
            for (ClientHandler clientHandler :
                    clientHandlerList) {
                clientHandler.exit();
            }

            clientHandlerList.clear();
        }

        forwardingTreadPoolExecutor.shutdownNow();
    }

    public synchronized void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public synchronized void onNewMessageArrived(ClientHandler clientHandler, String str) {
        System.out.println("Received-" + clientHandler.getClientInfo() + ":" + str);
        forwardingTreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (ClientHandler client: clientHandlerList) {
                    if (client.equals(clientHandler)) {
                        continue;
                    }
                    client.send(str);
                }
            }
        });
    }

    private class ClientListener extends Thread {
        private boolean done = false;
        private ServerSocket serverSocket;

        public ClientListener(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            System.out.println("Server info: " + serverSocket.getInetAddress() + "\tport:" + serverSocket.getLocalPort());
        }

        @Override
        public void run() {
            super.run();

            System.out.println("server ready!");

            Socket client;
            while (!done) {
                try {
                    client = serverSocket.accept();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    // async clienthandler construct
                    ClientHandler clientHandler = new ClientHandler(client, TCPServer.this);
                    // start to read and print
                    clientHandler.readToPrint();
                    synchronized (TCPServer.this) {
                        clientHandlerList.add(clientHandler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("client connect exception" + e.getMessage());
                }
            }

            System.out.println("server closed!");
        }

        void close() {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverSocket = null;
            }
        }

        public void exit() {
            done = true;
            close();
            System.out.println("ClientListener exited.");
        }
    }
}
