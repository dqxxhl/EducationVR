package com.sd.vr.education.network.http.downloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sd.vr.education.network.http.downloader.entity.LoaderInfo;
import com.sd.vr.education.network.http.downloader.load.LoaderTask;

import android.util.Log;

/**
 * Created by sk on 15-8-1. <br/>
 * 下载任务执行器. <br/>
 */
public class LoaderExecutor {

    /**
     * 最大线程数量.
     */
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
        public void onError(String url, LoaderError err) {
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

    /**
     * 根据下载信息进行下载任务. <br/>
     * 1. 新任务将直接下载 <br/>
     * 2. 正在下载的任务将不执行，直接返回当前task <br/>
     * 3. 下载完成的任务将重新下载 <br/>
     * 4. 下载失败的任务（若配置文件存在的情况下）则继续上次断点下载 <br/>
     * 5. 下载取消的任务（若配置文件存在的情况下）则继续上次断点下载 <br/>
     * 6. 若配置文件不存在,将重新下载. <br/>
     * 7. 默认每条url对应一个任务，不存在多个相同url对应多个任务
     *
     * @param info
     * @param listener
     * @return
     */
    public static LoaderTask load(LoaderInfo info, LoaderListener listener) {
        if (info == null) {
            return null;
        }

        return load(buildTask(info), listener);
    }

    /**
     * 创建LoaderTask.
     * 
     * @param info
     * @return
     */
    public static LoaderTask buildTask(LoaderInfo info) {
        return new LoaderTask(info);
    }

    /**
     * 根据LoaderTask下载.
     * 
     * @param task
     * @param l
     */
    public static LoaderTask load(LoaderTask task, LoaderListener l) {
        if (task == null) {
            return null;
        }
        final String url = task.getInfo().url();

        return mTasks.containsKey(url) ? mTasks.get(url) : execute(task, l);
    }

    /**
     * 停止当前下载任务.
     * 
     * @param task
     */
    public static void cancel(LoaderTask task) {
        if (task == null) {
            return;
        }
        task.stop();
    }

    /**
     * 删除当前下载任务，
     * 
     * @param task
     */
    public static void delete(LoaderTask task) {
        if (task == null) {
            return;
        }

        task.release();
        mTasks.remove(task.getInfo().url());
        mTaskListeners.remove(task.getInfo().url());
    }
}
