package com.sd.vr.education.network.http.downloader.blockload;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;

public class Accessor {

    private RandomAccessFile itemFile;

    private FileChannel itemFileChannel;

    public Accessor() throws IOException {
        this("", 0);
    }

    public Accessor(String path, long pos) throws IOException {
        itemFile = new RandomAccessFile(path, "rw");
        itemFileChannel = itemFile.getChannel();
        itemFileChannel.position(pos);
    }

    public synchronized boolean write(byte[] buff, int start, int length) throws IOException {
        try {
            itemFileChannel.write(ByteBuffer.wrap(buff, start, length));
        } catch (ClosedChannelException e) {
            return false;
        }
        return true;
    }

    public void close() throws IOException {
        if (itemFileChannel != null) {
            itemFileChannel.close();
        }

        if (itemFile != null) {
            itemFile.close();
        }
    }
}
