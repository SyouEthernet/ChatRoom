package com.syou.chatroom.frames;

import com.syou.chatroom.core.Frame;
import com.syou.chatroom.core.IoArgs;
import com.syou.chatroom.core.SendPacket;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public class SendEntityFrame extends AbsSendPacketFrame{
    private final long unConsumeEntityLength;
    private final ReadableByteChannel channel;

    SendEntityFrame(short identifier, long entityLength, ReadableByteChannel channel, SendPacket packet) {
        super((int)Math.min(entityLength, Frame.MAX_CAPACITY), Frame.TYPE_PACKET_ENTITY, Frame.FLAG_NONE, identifier, packet);
        this.unConsumeEntityLength = entityLength - bodyRemaining;
        this.channel = channel;
    }

    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        if (packet == null) {
            // user canceled
            return args.fillEmpty(bodyRemaining);
        }
        return args.readFrom(channel);
    }

    @Override
    public Frame buildNextFrame() {
        if (unConsumeEntityLength == 0) {
            return null;
        }
        return new SendEntityFrame(getBodyIdentifier(), unConsumeEntityLength, channel, packet);
    }
}
