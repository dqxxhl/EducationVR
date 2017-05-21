package com.sd.vr.education.network.http.downloader;

/**
 * 文件下载监控回调事件
 * Created by hl09287 on 2017/4/14.
 */
public abstract class LoaderListener {

    /**
     * 当下载进度更新时回调.
     */
    abstract public void onLoad(String url, int size, int totalSize);

    /**
     * 当任务完成后回调.
     */
    public void onCompleted(String url, String path) {
    }

    /**
     * 出错时回调.
     */
    public void onError(String url, ErrorCode err) {

    }

    /**
     * 取消时会掉
     * @param url
     */
    public void onCancelled(String url) {

    }
}
