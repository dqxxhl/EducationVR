package com.dqxxhl.educationvr.network.http.downloader;

/**
 * Created by sk on 15-7-31.
 */
public abstract class LoaderListener {

    /**
     * 当下载进度更新时回调.<br/>
     * 根据Task的 reportSpace 来控制onLoad的回调.
     * 
     * @param url
     * @param size
     * @param totalSize
     */
    abstract public void onLoad(String url, int size, int totalSize);

    /**
     * 当任务完成后回调.
     * 
     * @param url
     */
    public void onCompleted(String url, String path) {
    }

    /**
     * 出错时回调.
     * 
     * @param err
     */
    public void onError(String url, LoaderError err) {

    }

    public void onCancelled(String url) {

    }
}
