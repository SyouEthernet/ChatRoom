package com.syou.chatroom.frames;

import com.syou.chatroom.core.Frame;
import com.syou.chatroom.core.IoArgs;

import java.io.IOException;

public abstract class AbsReceiveFrame extends Frame {
    volatile  int bodyRemaining;

    public AbsReceiveFrame(byte[] header) {
        super(header);
        bodyRemaining = getBodyLength();
    }

    @Override
    public boolean handle(IoArgs args) throws IOException {
        if (bodyRemaining == 0) {
            return true;
        }

        bodyRemaining -= consumeBody(args);

        return bodyRemaining == 0;
    }

    @Override
    public Frame nextFrame() {
        return null;
    }

    @Override
    public int getConsumableLength() {
        return bodyRemaining;
    }

    protected abstract int consumeBody(IoArgs args) throws IOException;
}
