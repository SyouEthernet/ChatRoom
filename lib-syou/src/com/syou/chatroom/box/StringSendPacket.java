package com.syou.chatroom.box;

import com.syou.chatroom.core.SendPacket;

import java.io.ByteArrayInputStream;

public class StringSendPacket extends SendPacket<ByteArrayInputStream> {
    private final byte[] bytes;

    public StringSendPacket(String str) {
        this.bytes = str.getBytes();
        this.length = bytes.length;
    }


    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }
}
