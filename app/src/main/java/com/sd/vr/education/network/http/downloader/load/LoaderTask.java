package com.sd.vr.education.network.http.downloader.load;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.sd.vr.education.network.http.downloader.LoaderError;
import com.sd.vr.education.network.http.downloader.LoaderListener;
import com.sd.vr.education.network.http.downloader.entity.LoaderInfo;
import com.sd.vr.education.network.http.downloader.utils.LoaderUtils;

import android.util.Log;

/**
 * Created by sk on 15-8-1.
 */
public class LoaderTask implements Runnable, BlockTaskListener {

    public static final int STATUS_INITIALIZE = 0x00;
    public static final int STATUS_READY = 0x10;
    public static final int STATUS_FINISHED = 0x20;
    public static final int STATUS_ERROR = 0x30;
    public static final int STATUS_CANCELED = 0x40;

    private NetFileInfo mNetFileInfo;
    private int mTaskNum;
    private int mTaskStatus;

    private int[] mBlockTaskStatus;
    private FutureTask<Integer>[] mTasks;
    private BlockTask[] mBlockTasks;

    private ExecutorService mExecutor;

    private LoaderInfo mInfo;
    private LoaderConfig mConfig;
    private LoaderConfig.ConfigDesc mConfigDesc;
    private LoaderListener mLoaderListener;

    private final byte[] mLock = new byte[0];

    public LoaderTask(LoaderInfo info) {
        mInfo = info;
        mConfig = new LoaderConfig(info);
        mTaskStatus = STATUS_INITIALIZE;

        printf("Task status : %d", mTaskStatus);
    }

    /**
     * 获取配置文件信息
     * 
     * @return 当前下载的配置
     */
    public LoaderConfig getConfig() {
        return mConfig;
    }

    /**
     * 设置下载监听器.
     * 
     * @param l
     */
    public void setLoaderListener(LoaderListener l) {
        mLoaderListener = l;
    }

    /**
     * 停止下载任务
     */
    public void stop() {
        for (int i = 0; i < mTaskNum; i++) {
            if (mTasks[i] != null) {
                mTasks[i].cancel(true);
            }
        }
    }

    /**
     * 释放当前任务，（停止任务的基础上，删除配置文件)
     */
    public void release() {
        stop();
        mConfig.file().delete();
    }

    /**
     * 获取当前任务的状态 .
     * 
     * @return
     */
    public int getStatus() {
        return mTaskStatus;
    }

    /**
     * 获取下载信息.
     * 
     * @return
     */
    public LoaderInfo getInfo() {
        return mInfo;
    }

    @Override
    public void run() {

        try {

            // 防止loadTask重复调用.
            if (isExecuted()) {
                printf("This task has been start!");
                return;
            }

            mTaskStatus = STATUS_READY;
            printf("Task status : %d", mTaskStatus);

            initNetFileInfo();

            printf("%s", mNetFileInfo);

            if (mNetFileInfo.size == -2) {
                mTaskStatus = STATUS_ERROR;
                mLoaderListener.onError(mInfo.url(), new LoaderError(LoaderError.CODE_RESPONSE_CODE_ERROR,
                        new RuntimeException("The response code is not HTTP_OK or HTTP_PARTIAL !")));
                return;
            }

            if (mNetFileInfo.size == -1) {
                // setBlockStatus(STATUS_ERROR);
                mTaskStatus = STATUS_ERROR;
                mLoaderListener.onError(mInfo.url(), new LoaderError(LoaderError.CODE_CONNECT_ERROR,
                        new RuntimeException("Got the error when connect to the server !")));
                return;
            }

            // 是否支持断点，不支持则设置线程数只可为一
            mTaskNum = mNetFileInfo.isSupportPartial ? mInfo.splitter() : 1;

            mExecutor = Executors.newFixedThreadPool(mTaskNum);

            mBlockTaskStatus = new int[mTaskNum];
            mTasks = new FutureTask[mTaskNum];
            mBlockTasks = new BlockTask[mTaskNum];

            // 新建配置信息 or 恢复配置信息
            build_config_desc();

            // 分发线程任务
            dispatch_block_task();

            // 循环回调下载进度
            loop_load_progress();

            // 处理结果
            handle_ret(obtain_loader_state());

        } catch (IOException e) {
            mLoaderListener.onError(mInfo.url(), new LoaderError(LoaderError.CODE_LOAD_ERROR, e));
        } catch (InterruptedException e) {
            mLoaderListener.onError(mInfo.url(), new LoaderError(LoaderError.CODE_LOAD_ERROR, e));
        }
    }

    /**
     * <p>
     * 获取下载任务配置信息
     * </p>
     * 1. 优先获取本地配置信息 <br/>
     * 2. 本地无配置信息，则走新建流程
     */
    private void build_config_desc() {
        LoaderConfig.ConfigDesc revert;

        // 如下情况会走新建流程:
        // 1. 当前不支持断点
        // 2. 文件不存在
        // 3. 文件信息错误或者不存在
        // 4. 文件大小不匹配
        // 5. 文件block数不匹配
        if (!mNetFileInfo.isSupportPartial || !mConfig.file().exists() || (revert = mConfig.revert()) == null
                || revert.fileSize() != mNetFileInfo.size || revert.blockCount() != mTaskNum) {

            mConfigDesc = build_block_desc(mNetFileInfo.size);
            return;

        }

        mConfigDesc = revert;
    }

    /**
     * 重新分配任务，并且创建配置描述.
     *
     * @param fileSize
     * @return
     */
    private LoaderConfig.ConfigDesc build_block_desc(int fileSize) {
        final int splitter = mTaskNum;

        final int[] starts = new int[splitter];
        final int[] ends = new int[splitter];

        final int blockSize = (fileSize % splitter) == 0 ? fileSize / splitter : fileSize / splitter + 1;

        for (int i = 0; i < splitter; i++) {
            starts[i] = blockSize * i;
            ends[i] = blockSize * (i + 1) - 1;
        }

        return new LoaderConfig.ConfigDesc(splitter, fileSize, 0, starts, ends);
    }

    /**
     * <p>
     * 获取文件网络文件信息.
     * </p>
     * >0 - 文件大小 <br/>
     * -1 - rspCode 返回问题 <br/>
     * -2 - url,io 错误 <br/>
     * 
     * @return
     */
    private void initNetFileInfo() {
        if (mNetFileInfo == null) {
            mNetFileInfo = new NetFileInfo();
        }

        try {
            URL url = new URL(mInfo.url());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Range", "bytes=" + 0 + "-");
            LoaderUtils.setHeader(conn);
            final int stateCode = conn.getResponseCode();

            mNetFileInfo.size = (stateCode == HttpURLConnection.HTTP_OK)
                    || (stateCode == HttpURLConnection.HTTP_PARTIAL) ? conn.getContentLength() : -2;

            mNetFileInfo.isSupportPartial = stateCode == HttpURLConnection.HTTP_PARTIAL;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            mNetFileInfo.size = -1;
        } catch (IOException e) {
            e.printStackTrace();
            mNetFileInfo.size = -1;
        }
    }

    /**
     * <p>
     * 获取当前下载任务的状态.
     * </p>
     * <i>默认</i>所有块任务完成才算完成.
     * 
     * @return
     */
    private int obtain_loader_state() {
        for (int i = 0; i < mTaskNum; i++) {
            switch (mBlockTaskStatus[i]) {
            case STATUS_ERROR:
            case STATUS_CANCELED:
            case STATUS_READY:
            case STATUS_INITIALIZE:
                return mBlockTaskStatus[i];
            }
        }
        return STATUS_FINISHED;
    }

    /**
     * 检查当前Task的是否已经启动过.
     * 
     * @return
     */
    private boolean isExecuted() {
        return mTaskStatus == STATUS_READY;
    }

    /**
     * 分发每个线程的任务.
     */
    private void dispatch_block_task() throws IOException {
        for (int i = 0; i < mTaskNum; i++) {
            if (!mConfigDesc.isBlockOver(i) && !mConfigDesc.isLoadedOver()) {
                mBlockTasks[i] = new BlockTask(mInfo, mConfigDesc.starts()[i], mConfigDesc.ends()[i], i, this);
                mTasks[i] = new FutureTask<Integer>(mBlockTasks[i]);
                mBlockTaskStatus[i] = STATUS_READY;
                mExecutor.submit(mTasks[i]);
            } else {
                mBlockTaskStatus[i] = STATUS_FINISHED;
            }
        }

        mExecutor.shutdown();
    }

    /**
     * 检查每个线程的状态并且唤醒失败线程.
     *
     * @return
     */
    private boolean check_awake_block_task() {
        boolean notFinish = false;

        for (int i = 0; i < mTaskNum; i++) {

            if (mTasks[i] == null) {
                continue;
            }

            if (mTasks[i].isDone()) {
                if (mTasks[i].isCancelled()) {
                    // 被取消.
                    mBlockTaskStatus[i] = STATUS_CANCELED;
                } else {
                    // 任务完成
                    try {
                        mBlockTaskStatus[i] = mTasks[i].get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                notFinish = true;
            }
        }

        return notFinish;
    }

    /**
     * 轮询当前下载进度.
     * 
     * @throws InterruptedException
     */
    private void loop_load_progress() throws InterruptedException {
        while (check_awake_block_task()) {
            Thread.sleep(500);

            // call back
            mLoaderListener.onLoad(mInfo.url(), mConfigDesc.loadedLength(), mConfigDesc.fileSize());
        }
    }

    private void handle_ret(int flag) {
        switch (flag) {
        case STATUS_FINISHED:
            mConfig.file().delete();
            // 若完成
            if (mConfigDesc.isLoadedOver()) {
                mTaskStatus = STATUS_FINISHED;
                printf("Task status : %d", mTaskStatus);
                mLoaderListener.onCompleted(mInfo.url(), mInfo.path());
            } else {
                // 未知错误.
                mTaskStatus = STATUS_ERROR;
                printf("Task status : %d", mTaskStatus);
                mLoaderListener.onError(mInfo.url(), new LoaderError(LoaderError.CODE_UNKNOWN_ERROR,
                        new RuntimeException("unknown err.")));
            }
            break;
        case STATUS_ERROR:
            mTaskStatus = STATUS_ERROR;
            printf("Task status : %d", mTaskStatus);
            mLoaderListener.onError(mInfo.url(), new LoaderError(LoaderError.CODE_LOAD_ERROR, new RuntimeException(
                    "load err.")));
            break;
        case STATUS_CANCELED:
            mTaskStatus = STATUS_CANCELED;
            printf("Task status : %d", mTaskStatus);
            mLoaderListener.onCancelled(mInfo.url());
            break;
        }
    }

    @Override
    public void update(int blockId, int pos) {
        synchronized (mLock) {
            mConfigDesc.update(blockId, pos);

            // 若支持断点，则需记录文件
            if (mNetFileInfo.isSupportPartial) {
                mConfig.record(mConfigDesc);
            }
        }
    }

    private void printf(String format, Object... args) {
        Log.e(getClass().getSimpleName(), String.format(format, args));
    }

    private static class NetFileInfo {
        /**
         * 服务端提供的名字位于 'Content-Disposition' 中 'fileId' 节点
         */
        private String name;

        /**
         * 服务端提供的大小位于 'Content-Length'
         */
        private int size;

        /**
         * 服务端是否支持断点，若不支持断点则不支持多线程
         */
        private boolean isSupportPartial;

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();

            sb.append("{ fileId:").append(name).append(" , fileSize:").append(size).append(" , isSupportPartial:")
                    .append(isSupportPartial).append(" }");

            return sb.toString();
        }
    }
}
