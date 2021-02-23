package com.syou.chatroom.core;

import java.io.Closeable;
import java.io.IOException;

public class IoContext{
    private static IoContext INSTANSE;
    private final IoProvider ioProvider;

    private IoContext(IoProvider ioProvider) {
        this.ioProvider = ioProvider;
    }

    public IoProvider getIoProvider() {
        return ioProvider;
    }

    public static IoContext get() {
        return INSTANSE;
    }

    public static StartedBoot setup() {
        return new StartedBoot();
    }

    public static void close() throws IOException {
        if (INSTANSE != null) {
            INSTANSE.callClose();
        }
    }

    public void callClose() throws IOException {
        ioProvider.close();
    }

    public static class StartedBoot{
        private IoProvider ioProvider;

        private StartedBoot() {

        }

        public StartedBoot ioPorvider(IoProvider ioProvider) {
            this.ioProvider = ioProvider;
            return this;
        }

        public IoContext start() {
            INSTANSE = new IoContext(ioProvider);
            return INSTANSE;
        }
    }
}
