package com.syou.chatroom.box;

import com.syou.chatroom.core.SendPacket;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class StringSendPacket extends ByteSendPacket {
    public StringSendPacket(String msg) {
        super(msg.getBytes());
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }
}
