package com.syou.chatroom.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * receive packet
 */
public abstract class ReceivePacket<Stream extends OutputStream, Entity> extends Packet<Stream>{
    // received packet final entity
    private Entity entity;
    public ReceivePacket(long len) {
        this.length = len;
    }

    public Entity entity() {
        return entity;
    }

    protected abstract Entity buidEntity(Stream stream);

    protected final void closeStream(Stream stream) throws IOException {
        super.closeStream(stream);
        entity = buidEntity(stream);
    }

}
