package com.sd.vr.education.network.http.downloader.blockload;

/**
 * 块下载监控
 * Created by hl09287 on 2017/4/14.
 */
public interface BlockTaskListener {
    void update(int blockId, int pos);
}
