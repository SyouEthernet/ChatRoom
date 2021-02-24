package com.syou.chatroom.box;

import java.io.ByteArrayOutputStream;

public class ByteReceivePacket extends AbsByteArrayReceivePacket<byte[]>{

    public ByteReceivePacket(long len) {
        super(len);
    }

    @Override
    protected byte[] buidEntity(ByteArrayOutputStream stream) {
        return stream.toByteArray();
    }


    @Override
    public byte type() {
        return TYPE_MEMORY_BYTES;
    }
}
