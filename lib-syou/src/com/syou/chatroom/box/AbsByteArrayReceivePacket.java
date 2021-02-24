package com.syou.chatroom.box;

import com.syou.chatroom.core.Packet;
import com.syou.chatroom.core.ReceivePacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;

public abstract class AbsByteArrayReceivePacket<Entity> extends ReceivePacket<ByteArrayOutputStream, Entity> {
    public AbsByteArrayReceivePacket(long len) {
        super(len);
    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }
}
