package com.syou.chatroom.impl.async;

import com.syou.chatroom.core.Frame;
import com.syou.chatroom.core.IoArgs;
import com.syou.chatroom.core.SendPacket;
import com.syou.chatroom.ds.BytePriorityNode;
import com.syou.chatroom.frames.AbsSendPacketFrame;
import com.syou.chatroom.frames.CancelSendFrame;
import com.syou.chatroom.frames.SendEntityFrame;
import com.syou.chatroom.frames.SendHeaderFrame;

import java.io.Closeable;
import java.io.IOException;

public class AsyncPacketReader implements Closeable {
    private final PacketProvider provider;
    private volatile IoArgs args = new IoArgs();

    private volatile BytePriorityNode<Frame> node;
    private volatile int nodeSize = 0;

    private short lastIdentifier = 0;

    public AsyncPacketReader(PacketProvider provider) {
        this.provider = provider;
    }

    boolean requestTakePacket() {
        synchronized (this) {
            if (nodeSize >= 1) {
                return true;
            }
        }
        SendPacket packet = provider.takePacket();
        if (packet != null) {
            short identifier = generateIdentifier();
            SendHeaderFrame frame = new SendHeaderFrame(identifier, packet);
            appendNewFrame(frame);
        }
        synchronized (this) {
            return nodeSize != 0;
        }
    }


    IoArgs fillData() {
        Frame currentFrame = getCurrentFrame();
        if (currentFrame == null) {
            return null;
        }

        try {
            if (currentFrame.handle(args)) {
                // consume this frame
                // construct next frame
                Frame nextFrame = currentFrame.nextFrame();
                if (nextFrame != null) {
                    appendNewFrame(nextFrame);
                } else if (currentFrame instanceof SendEntityFrame) {
                    // tail entity frame
                    // notify complete
                    provider.completedPacket(((SendEntityFrame) currentFrame).getPacket(), true);
                }
                popCurrentFrame();
            }
            return args;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void cancel(SendPacket packet) {
        if (nodeSize == 0) {
            return;
        }

        for (BytePriorityNode<Frame> x = node, before = null; x != null; before = x, x = x.next) {
            Frame frame = x.item;
            if (frame instanceof AbsSendPacketFrame) {
                AbsSendPacketFrame packetFrame = (AbsSendPacketFrame) frame;
                if (packetFrame.getPacket() == packet) {
                    boolean removable = packetFrame.abort();
                    if (removable) {
                        removeFrame(x, before);
                        if (packetFrame instanceof SendHeaderFrame) {
                            // header frame not send any data
                            break;
                        }
                    }

                    // notify data transe cancel
                    CancelSendFrame cancelSendFrame = new CancelSendFrame(packetFrame.getBodyIdentifier());
                    appendNewFrame(cancelSendFrame);

                    provider.completedPacket(packet, false);
                    break;
                }
            }
        }
    }


    @Override
    public synchronized void close() {
        while (node != null) {
            Frame frame = node.item;
            if (frame instanceof AbsSendPacketFrame) {
                SendPacket packet = ((AbsSendPacketFrame) frame).getPacket();
                provider.completedPacket(packet, false);
            }
            node = node.next;
        }

        nodeSize = 0;
        node = null;
    }

    private synchronized void appendNewFrame(Frame frame) {
        BytePriorityNode<Frame> newNode = new BytePriorityNode<>(frame);
        if (node != null) {
            node.appendWithPriority(newNode);
        } else {
            node = newNode;
        }
        nodeSize++;
    }

    private synchronized Frame getCurrentFrame() {
        if (node == null) {
            return null;
        }
        return node.item;
    }

    private synchronized void popCurrentFrame() {
        node = node.next;
        nodeSize--;
        if (node == null) {
            requestTakePacket();
        }
    }

    private synchronized void removeFrame(BytePriorityNode<Frame> removeNode, BytePriorityNode<Frame> before) {
        if (before == null) {
            node = removeNode.next;
        } else {
            before.next = removeNode.next;
        }
        nodeSize--;
        if (node == null) {
            requestTakePacket();
        }
    }


    private short generateIdentifier() {
        short identifier = ++lastIdentifier;
        if (identifier == 255) {
            lastIdentifier = 0;
        }
        return identifier;
    }

    interface PacketProvider {
        SendPacket takePacket();

        void completedPacket(SendPacket packet, boolean isSucceed);
    }
}
