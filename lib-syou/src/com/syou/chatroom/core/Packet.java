package com.syou.chatroom.core;

import java.io.Closeable;

/**
 * public data class
 * provide type and length
 */
public abstract class Packet implements Closeable {
    protected  byte type;
    protected int length;

    public byte type() {
        return type;
    }

    public int length() {
        return length;
    }
}
