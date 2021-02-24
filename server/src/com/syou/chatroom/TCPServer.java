package com.syou.chatroom;

import com.syou.chatroom.handler.ClientHandler;
import com.syou.chatroom.utils.CloseUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements ClientHandler.ClientHandlerCallback {
    private final int port;
    private final File cachePath;
    private ClientListener mListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingTreadPoolExecutor;
    private Selector selector;
    ServerSocketChannel server;

    public TCPServer(int port, File cachePath) {
        this.port = port;
        this.cachePath = cachePath;
        forwardingTreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            selector = Selector.open();
            ServerSocketChannel server = ServerSocketChannel.open();
            // set non-blocking
            server.configureBlocking(false);
            // bind port
            server.socket().bind(new InetSocketAddress(port));
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server info: " + server.getLocalAddress().toString());
            this.server = server;

            // start client listen
            ClientListener clientListener = new ClientListener();
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

        CloseUtils.close(server);
        CloseUtils.close(selector);

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
        forwardingTreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (ClientHandler client : clientHandlerList) {
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

        @Override
        public void run() {
            super.run();

            Selector selector = TCPServer.this.selector;
            System.out.println("server ready!");

            while (!done) {
                try {
                    if (selector.select() == 0) {
                        if (done) {
                            break;
                        }
                        continue;
                    }

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        if (done) {
                            break;
                        }
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            // non-blocking get a client
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            try {
                                // async clienthandler construct
                                ClientHandler clientHandler = new ClientHandler(socketChannel, TCPServer.this, cachePath);
                                synchronized (TCPServer.this) {
                                    clientHandlerList.add(clientHandler);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("client connect exception" + e.getMessage());
                            }
                        }
                    }
                } catch (IOException e) {
                    continue;
                }
            }

            System.out.println("server closed!");
        }

        public void exit() {
            done = true;
            selector.wakeup();
            System.out.println("ClientListener exited.");
        }
    }
}
