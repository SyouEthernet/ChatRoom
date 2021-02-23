package com.syou.chatroom.core;

import java.io.Closeable;

/**
 * cache all data need to send
 * wrap data when sending
 */
public interface SendDispacher extends Closeable {
    void send(SendPacket packet);

    void cancel(SendPacket packet);
}
