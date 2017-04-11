package com.sd.vr.education.network.http.downloader.load;

/**
 * Created by sk on 15-10-10.
 */
public interface BlockTaskListener {
    void update(int blockId, int pos);
}
