package com.syou.chatroom.box;

import com.syou.chatroom.core.SendPacket;

import java.io.IOException;

public class StringSendPacket extends SendPacket {
    private final byte[] bytes;

    public StringSendPacket(String str) {
        this.bytes = str.getBytes();
        this.length = bytes.length;
    }


    @Override
    public byte[] bytes() {
        return bytes;
    }

    @Override
    public void close() throws IOException {

    }
}
