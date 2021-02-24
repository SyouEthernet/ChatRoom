package com.syou.chatroom.box;

import com.syou.chatroom.core.SendPacket;

import java.io.*;

public class FileSendPacket extends SendPacket<FileInputStream> {
    private final File file;
    private InputStream stream;

    public FileSendPacket(File file) {
        this.file = file;
        this.length = file.length();
    }

    @Override
    protected FileInputStream createStream() {
        return null;
    }
}
