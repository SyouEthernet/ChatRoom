package com.syou.chatroom.core;

/**
 * receive packet
 */
public abstract class ReceivePacket extends Packet{
    public abstract void save(byte[] bytes, int count);
}
