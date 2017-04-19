package com.sd.vr.education.network.http.downloader.load;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sd.vr.education.network.http.downloader.entity.LoaderInfo;

public class LoaderConfig {

    private final static String SUFFIX = ".cfg";

    private File cfgFile;

    public LoaderConfig(LoaderInfo info) {
        this.cfgFile = new File(info.path() + SUFFIX);
        final File parent = this.cfgFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
    }

    public File file() {
        return cfgFile;
    }

    public boolean record(ConfigDesc desc) {
        try {
            write(desc);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ConfigDesc revert() {
        try {
            return read();
        } catch (IOException e) {
            return null;
        }
    }

    private void write(ConfigDesc desc) throws IOException {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(cfgFile));
            dos.writeInt(desc.blockCount);
            dos.writeInt(desc.fileSize);
            dos.writeInt(desc.loadedLength);
            for (int i = 0; i < desc.blockCount; i++) {
                dos.writeInt(desc.starts[i]);
                dos.writeInt(desc.ends[i]);
            }
        } finally {
            if (dos != null) {
                dos.close();
            }
        }
    }

    private ConfigDesc read() throws IOException {
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(cfgFile));
            int blockCount = dis.readInt();
            int fileSize = dis.readInt();
            int loadedLength = dis.readInt();
            int[] starts = new int[blockCount];
            int[] ends = new int[blockCount];

            for (int i = 0; i < blockCount; i++) {
                starts[i] = dis.readInt();
                ends[i] = dis.readInt();
            }

            return new ConfigDesc(blockCount, fileSize, loadedLength, starts, ends);
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }

    public static class ConfigDesc {
        private int blockCount;
        private int fileSize;
        private int loadedLength;
        private int[] starts;
        private int[] ends;

        ConfigDesc() {
        }

        ConfigDesc(int blockCount, int fileSize, int loadedLength, int[] starts, int[] ends) {
            this.blockCount = blockCount;
            this.fileSize = fileSize;
            this.loadedLength = loadedLength;
            this.starts = starts;
            this.ends = ends;
        }

        public int blockCount() {
            return blockCount;
        }

        public int fileSize() {
            return fileSize;
        }

        public int loadedLength() {
            return loadedLength;
        }

        public int[] starts() {
            return starts;
        }

        public int[] ends() {
            return ends;
        }

        public synchronized void update(int blockId, int offset) {
            starts[blockId] += offset;
            loadedLength += offset;
        }

        public boolean isBlockOver(int blockId) {
            return ends != null && starts != null && starts[blockId] >= ends[blockId];
        }

        public boolean isLoadedOver() {
            Log.e("isLoadedOver","loadedLength="+loadedLength+"------------"+"fileSize"+fileSize);
            return loadedLength == fileSize;
        }
    }
}
