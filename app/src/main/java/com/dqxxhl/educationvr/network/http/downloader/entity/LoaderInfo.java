package com.dqxxhl.educationvr.network.http.downloader.entity;

import java.io.File;

public class LoaderInfo {
    /**
     * 下载所需的url链接
     */
    private String url;

    /**
     * 下载文件名
     */
    private String name;

    /**
     * 下载的目录
     */
    private String dir;

    /**
     * 下载块数量
     */
    private int splitter;

    private LoaderInfo(Builder builder) {
        this.url = builder.url;
        this.name = builder.name;
        this.dir = builder.dir;
        this.splitter = builder.splitter;
    }

    public String url() {
        return this.url;
    }

    public String name() {
        return this.name;
    }

    public String dir() {
        return this.dir;
    }

    public String path() {
        return new File(dir, name).getAbsolutePath();
    }

    public int splitter() {
        return this.splitter;
    }

    public static class Builder {
        private String url;
        private String name;
        private String dir;
        private int splitter;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder dir(String dir) {
            this.dir = dir;
            return this;
        }

        public Builder splitter(int splitter) {
            this.splitter = splitter;
            return this;
        }

        public LoaderInfo build() {
            if (url == null || "".equals(url)) {
                return null;
            }

            if (name == null || "".equals(name)) {
                name = url.substring(url.lastIndexOf("/") + 1, url.length());
            }

            splitter = splitter <= 0 ? 1 : splitter;

            return new LoaderInfo(this);
        }
    }

    @Override
    public String toString() {
        return String.format("[%s,%s] - %d - %s", name, dir, splitter, url);
    }
}
