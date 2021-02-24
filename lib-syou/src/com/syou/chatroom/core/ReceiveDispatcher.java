package com.syou.chatroom.core;

import java.io.Closeable;

/**
 * trans some ioargs to packet
 */
public interface ReceiveDispatcher extends Closeable {
    void start();

    void stop();

    interface ReceivePacketCallback{
        ReceivePacket<?, ?> onArrivedNewPacket(byte type, long length);
        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
