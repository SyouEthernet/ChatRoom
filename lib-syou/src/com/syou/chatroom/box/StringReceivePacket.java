package com.syou.chatroom.box;

import com.syou.chatroom.core.ReceivePacket;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

public class StringReceivePacket extends AbsByteArrayReceivePacket<String> {
    public StringReceivePacket(long len) {
        super(len);
    }

    @Override
    protected String buidEntity(ByteArrayOutputStream stream) {
        return new String(stream.toByteArray());
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }
}
