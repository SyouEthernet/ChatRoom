package com.syou.chatroom.frames;

import com.syou.chatroom.core.Frame;
import com.syou.chatroom.core.IoArgs;

import java.io.IOException;

public class CancelSendFrame extends AbsSendFrame{
    public CancelSendFrame(short identifier) {
        super(0, TYPE_COMMAND_SEND_CANCEL, FLAG_NONE, identifier);
    }

    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        return 0;
    }

    @Override
    public Frame nextFrame() {
        return null;
    }
}
