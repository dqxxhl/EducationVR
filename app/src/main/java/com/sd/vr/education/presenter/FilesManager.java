package com.sd.vr.education.presenter;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责文件的下载和管理
 * Created by hl09287 on 2017/4/14.
 */

public class FilesManager {

    public static final String TAG = FilesManager.class.getName();
    public static final String DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();

    public List<String> downLoadUrls = new ArrayList<>();
    private static FilesManager mFilesManager;

    private FilesManager(){
        Log.i(TAG, "启动文件管理");
    }


    public static synchronized FilesManager getInstance(){
        if (mFilesManager == null){
            mFilesManager = new FilesManager();
        }
        return mFilesManager;
    }

    /**
     * 下载文件
     * @param url
     */
    public void downLoad(String url){
        if (url == null || url.equals("")){
            return;
        }
        if (downLoadUrls == null){
            downLoadUrls = new ArrayList<>();
        }
        downLoadUrls.add(url);
    }

    /**
     * 批量下载文件
     * @param urls
     */
    public void downLoad(List<String> urls){
        if (urls == null || urls.size() == 0){
            return;
        }
        for (String url: urls) {
            downLoad(url);
        }
    }

    /**
     * 删除过期视频
     * @param fileId
     */
    public void deleteFile(String fileId){
        if (fileId == null || fileId.equals("")){
            return;
        }

        //TO-DO具体的删除逻辑
        File file = new File(DIRECTORY + fileId + ".mp4");
        if (file.delete()){
            Log.i(TAG, "delete:"+fileId);
        }
    }

    /**
     * 批量删除过期视频
     * @param fileIds
     */
    public void deleteFiles(List<String> fileIds){
        if (fileIds == null || fileIds.size() == 0){
            return;
        }

        for (String fileId: fileIds) {
            deleteFile(fileId);
        }
    }


}
