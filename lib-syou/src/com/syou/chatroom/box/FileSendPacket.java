package com.syou.chatroom.box;

import com.syou.chatroom.core.SendPacket;

import java.io.*;

public class FileSendPacket extends SendPacket<FileInputStream> {
    private final File file;

    public FileSendPacket(File file) {
        this.file = file;
        this.length = file.length();
    }

    @Override
    protected FileInputStream createStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public byte type() {
        return TYPE_STREAM_FILE;
    }
}
