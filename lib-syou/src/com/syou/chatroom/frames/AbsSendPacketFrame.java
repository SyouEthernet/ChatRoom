package com.syou.chatroom.frames;

import com.syou.chatroom.core.Frame;
import com.syou.chatroom.core.IoArgs;
import com.syou.chatroom.core.SendPacket;

import java.io.IOException;

public abstract class AbsSendPacketFrame extends AbsSendFrame {
    protected volatile SendPacket<?> packet;

    public AbsSendPacketFrame(int length, byte type, byte flag, short identifier, SendPacket packet) {
        super(length, type, flag, identifier);
        this.packet = packet;
    }


    public synchronized SendPacket getPacket() {
        return packet;
    }

    @Override
    public synchronized boolean handle(IoArgs args) throws IOException {
        if (packet == null && !isSending()) {
            return true;
        }
        return super.handle(args);
    }

    @Override
    public final synchronized Frame nextFrame() {
        return packet == null ? null : buildNextFrame();
    }

    // true, not send any data
    public final synchronized boolean abort() {
        boolean isSendging = isSending();
        if (isSendging) {
            fillDirtyDataOnAbort();
        }
        packet = null;
        return !isSendging;
    }

    protected void fillDirtyDataOnAbort() {

    }

    protected abstract Frame buildNextFrame();
}
