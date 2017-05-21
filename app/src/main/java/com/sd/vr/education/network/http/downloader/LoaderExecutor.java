package com.sd.vr.education.network.http.downloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sd.vr.education.network.http.downloader.blockload.LoaderTask;

import android.util.Log;

/**
 * 下载执行器
 * Created by hl09287 on 2017/4/14.
 */
public class LoaderExecutor {

    private static final int MAX_LOADER_THREADS_COUNT = 5;

    private final static ExecutorService mExecutor = Executors.newFixedThreadPool(MAX_LOADER_THREADS_COUNT);

    private final static Map<String, LoaderTask> mTasks = new ConcurrentHashMap<String, LoaderTask>();

    private final static Map<String, LoaderListener> mTaskListeners = new ConcurrentHashMap<String, LoaderListener>();

    private static LoaderListener globalListener = new LoaderListener() {
        @Override
        public void onLoad(String url, int size, int totalSize) {
            if (mTaskListeners.containsKey(url)) {
                mTaskListeners.get(url).onLoad(url, size, totalSize);
            }
        }

        @Override
        public void onCompleted(String url, String path) {
            mTasks.remove(url);
            if (mTaskListeners.containsKey(url)) {
                mTaskListeners.remove(url).onCompleted(url, path);
            }
        }

        @Override
        public void onError(String url, ErrorCode err) {
            mTasks.remove(url);
            if (mTaskListeners.containsKey(url)) {
                mTaskListeners.remove(url).onError(url, err);
            }
        }

        @Override
        public void onCancelled(String url) {
            mTasks.remove(url);
            if (mTaskListeners.containsKey(url)) {
                mTaskListeners.remove(url).onCancelled(url);
            }
        }
    };

    private static LoaderTask execute(LoaderTask task, LoaderListener listener) {
        Log.e("--- exec ---", "exec:" + task.getInfo().url());
        mTasks.put(task.getInfo().url(), task);
        mTaskListeners.put(task.getInfo().url(), listener);
        task.setLoaderListener(globalListener);
        mExecutor.execute(task);

        return task;
    }
    public static LoaderTask load(LoaderInfo info, LoaderListener listener) {
        if (info == null) {
            return null;
        }

        return load(buildTask(info), listener);
    }

    public static LoaderTask buildTask(LoaderInfo info) {
        return new LoaderTask(info);
    }

    public static LoaderTask load(LoaderTask task, LoaderListener l) {
        if (task == null) {
            return null;
        }
        final String url = task.getInfo().url();

        return mTasks.containsKey(url) ? mTasks.get(url) : execute(task, l);
    }

    public static void cancel(LoaderTask task) {
        if (task == null) {
            return;
        }
        task.stop();
    }
    public static void delete(LoaderTask task) {
        if (task == null) {
            return;
        }

        task.release();
        mTasks.remove(task.getInfo().url());
        mTaskListeners.remove(task.getInfo().url());
    }
}
