package com.syou.chatroom.core;

import java.io.Closeable;

/**
 * trans some ioargs to packet
 */
public interface ReceiveDispatcher extends Closeable {
    void start();

    void stop();

    interface ReceivePacketCallback{
        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
