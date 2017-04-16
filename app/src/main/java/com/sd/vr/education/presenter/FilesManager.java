package com.sd.vr.education.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.util.Log;

import com.sd.vr.education.entity.FileDownLoad;
import com.sd.vr.education.network.http.downloader.LoaderError;
import com.sd.vr.education.network.http.downloader.LoaderExecutor;
import com.sd.vr.education.network.http.downloader.LoaderListener;
import com.sd.vr.education.network.http.downloader.entity.LoaderInfo;
import com.sd.vr.education.utils.Utils;

/**
 * 负责文件的下载和管理
 * Created by hl09287 on 2017/4/14.
 */

public class FilesManager {

    public static final String TAG = FilesManager.class.getName();
    public static final String DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath()+"/com.sd.vr";
    public static final String PATCH_SUFFIX = ".mp4";

    public List<FileDownLoad> downLoadFiles = new ArrayList<>();
    private static FilesManager mFilesManager;
    private String fileDownLoading;

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
     * @param fileDownLoad
     */
    public void downLoad(FileDownLoad fileDownLoad){
        if (fileDownLoad == null || fileDownLoad.fileUrl.equals("") || fileDownLoad.fileName.equals("") || fileDownLoad.fileSize == 0){
            return;
        }
        if (downLoadFiles == null){
            downLoadFiles = new ArrayList<>();
        }
        downLoadFiles.add(fileDownLoad);
        startDownLoad();
    }

    /**
     * 批量下载文件
     * @param files
     */
    public void downLoad(List<FileDownLoad> files){
        if (files == null || files.size() == 0){
            return;
        }
        for (FileDownLoad file: files) {
            downLoad(file);
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
        File file = new File(DIRECTORY + fileId);
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

    /**
     * 启动文件下载
     */
    private void startDownLoad(){

        if (fileDownLoading != null){
            return;
        }

        if (downLoadFiles == null || downLoadFiles.size() == 0){
            return;
        }

        String fileName = downLoadFiles.get(0).fileName;
        String url = downLoadFiles.get(0).fileUrl;
        long size = downLoadFiles.get(0).fileSize;

        //校验本地是否存在这个文件
        File fileDir = new File(DIRECTORY);
        if (fileDir != null && fileDir.listFiles() != null && fileDir.listFiles().length > 0){
            for (File file : fileDir.listFiles()) {
                if (file.getAbsolutePath().endsWith(PATCH_SUFFIX)){
                    if (file.getName().equals(fileName) && Utils.getFileSize(file) == size){
                        return;
                    }
                }
            }
        }
        Log.e(TAG, url);


        LoaderInfo loaderInfo = (new LoaderInfo.Builder()).dir(DIRECTORY).name(fileName).url(url).splitter(1).build();
        LoaderExecutor.load(loaderInfo, new LoaderListener() {
            @Override
            public void onLoad(String url, int size, int totalSize) {
                Log.e(TAG, "开始,下载......");
                Log.e(TAG, "总大小："+totalSize+"下载大小:"+size+"下载进度："+((float)size/(float) totalSize));
            }

            @Override
            public void onCompleted(String url, String path) {
                super.onCompleted(url, path);
                Log.e(TAG, "完成,下载......");
                fileDownLoading = null;
                startDownLoad();
            }

            @Override
            public void onCancelled(String url) {
                super.onCancelled(url);
                Log.e(TAG, "取消,下载......"+url);
                fileDownLoading = null;
            }

            @Override
            public void onError(String url, LoaderError err) {
                super.onError(url, err);
                Log.e(TAG, "异常,下载......"+err.getMsg());
                fileDownLoading = null;
            }
        });
    }


}
