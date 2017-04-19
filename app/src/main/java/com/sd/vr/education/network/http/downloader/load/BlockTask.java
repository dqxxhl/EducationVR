package com.sd.vr.education.network.http.downloader.load;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import com.sd.vr.education.network.http.downloader.entity.LoaderInfo;
import com.sd.vr.education.network.http.downloader.utils.LoaderUtils;

import android.util.Log;

class BlockTask implements Callable<Integer> {
    private String url;
    private long startPos;
    private long endPos;
    private int blockId;

    private Accessor accessor;
    private BlockTaskListener mBlockTaskListener;

    public BlockTask(LoaderInfo info, long startPos, long endPos, int blockId, BlockTaskListener l) throws IOException {
        this.url = info.url();
        this.startPos = startPos;
        this.endPos = endPos;
        this.blockId = blockId;
        this.mBlockTaskListener = l;
        this.accessor = new Accessor(info.path(), startPos);
    }

    @Override
    public Integer call() throws Exception {
        if (startPos < endPos) {
            BufferedInputStream bis = null;
            try {
               Log.e("sdadas","dasdasd");
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(5 * 1000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
                Log.e("BlockTask", "Range:"+ "bytes=" + startPos + "-" + endPos);
                LoaderUtils.setHeader(conn);

                final int CODE = conn.getResponseCode();

                if (CODE == HttpURLConnection.HTTP_OK || CODE == HttpURLConnection.HTTP_PARTIAL) {

                    bis = new BufferedInputStream(conn.getInputStream());
                    byte[] buffer = new byte[1024 * 4];
                    int offset;

                    while ((offset = bis.read(buffer)) != -1) {
                        if (!accessor.write(buffer, 0, offset)) {
                            return LoaderTask.STATUS_CANCELED;
                        }
                        mBlockTaskListener.update(blockId, offset);
                    }
                    return LoaderTask.STATUS_FINISHED;
                }
            } catch (Exception e) {
                return LoaderTask.STATUS_ERROR;
            } finally {
                if (accessor != null) {
                    accessor.close();
                }

                if (bis != null) {
                    bis.close();
                }
            }
        }
        return LoaderTask.STATUS_FINISHED;
    }

    private void printf(String format, Object... args) {
        Log.e(getClass().getSimpleName(), String.format(format, args));
    }

}
