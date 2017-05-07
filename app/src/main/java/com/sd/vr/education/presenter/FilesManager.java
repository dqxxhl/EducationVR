package com.sd.vr.education.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.os.Environment;
import android.util.Log;

import com.sd.vr.education.entity.FileDownLoad;
import com.sd.vr.education.entity.VideoItem;
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
    public static final String TEMP_SUFFIX = ".cfg";
    public static final String FILE_SUFFIX = "_";

    //下载状态定义
    public static final int STATUS_TO_DOWNLOAD = 0;//待下载
    public static final int STATUS_DOWNLOADING = 1;//下载中
    public static final int STATUS_ERROR_DOWNLOAD = 2;//下载异常
    public static final int STATUS_COMPLETE_DOWNLOAD = 3;//下载完成



    public HashMap<FileDownLoad,Integer> downLoadFiles = new HashMap<>();
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
     * @param fileDownLoad
     */
    public void downLoad(FileDownLoad fileDownLoad){
        if (fileDownLoad == null || fileDownLoad.fileUrl.equals("") || fileDownLoad.fileId.equals("") || fileDownLoad.fileSize == 0){
            return;
        }
        if (downLoadFiles == null){
            downLoadFiles = new HashMap<>();
        }
        //如果已存在次下载项不再下载
        if (downLoadFiles.size() > 0){
            for (Map.Entry<FileDownLoad, Integer> entry : downLoadFiles.entrySet()) {
                FileDownLoad file = entry.getKey();
                if (file.fileId.equals(fileDownLoad.fileId)){
                    return;
                }
            }
        }
        //校验本地是否存在这个文件,若文件已存在但是大小校验不通过，需首先删除本地文件
        String fileName = fileDownLoad.fileId + FILE_SUFFIX + fileDownLoad.fileNameShow;
        long size = fileDownLoad.fileSize;
        File fileDir = new File(DIRECTORY);
        if (fileDir != null && fileDir.listFiles() != null && fileDir.listFiles().length > 0){
            for (File file : fileDir.listFiles()) {
                if (file.getAbsolutePath().endsWith(PATCH_SUFFIX)){
                    if (file.getName().equals(fileName) && Utils.getFileSize(file) == size){
                        //文件已下载
                        ServiceManager.getInstance().sendDownloadAck(fileName);
                        return;
                    }else if (file.getName().equals(fileName) && Utils.getFileSize(file) > size){
                        deleteFile(fileName);
                    }
                }
            }
        }

        downLoadFiles.put(fileDownLoad,STATUS_TO_DOWNLOAD);
        //刷新UI
        ServiceManager.getInstance().updateUI();
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
        File fileDir = new File(DIRECTORY);
        for (File file : fileDir.listFiles()) {
            if (file.getAbsolutePath().contains(fileId)){
                if (file.delete()){
                    Log.e(TAG, "delete:"+fileId);
                }
            }
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
    public void startDownLoad(){

        if (downLoadFiles == null || downLoadFiles.size() == 0){
            return;
        }

        FileDownLoad fileDownLoad = null;
        boolean isDownLoading = false;
        for (Map.Entry<FileDownLoad, Integer> entry : downLoadFiles.entrySet()) {
            FileDownLoad file = entry.getKey();
            int status = entry.getValue();
            if (status == STATUS_DOWNLOADING){
                isDownLoading = true;
            }

            if (status == STATUS_TO_DOWNLOAD && fileDownLoad == null){
                fileDownLoad = file;
            }
        }

        if (isDownLoading){
            return;
        }

        final String fileName = fileDownLoad.fileId + FILE_SUFFIX + fileDownLoad.fileNameShow;
        String url = fileDownLoad.fileUrl;

        //校验本地是否存在这个文件,若文件已存在但是大小校验不通过，需首先删除本地文件
        /*File fileDir = new File(DIRECTORY);
        if (fileDir != null && fileDir.listFiles() != null && fileDir.listFiles().length > 0){
            for (File file : fileDir.listFiles()) {
                if (file.getAbsolutePath().endsWith(PATCH_SUFFIX)){
                    if (file.getName().equals(fileName) && Utils.getFileSize(file) == size){
                        //文件已下载
                        ServiceManager.getInstance().sendDownloadAck(fileName);
                        return;
                    }else if (file.getName().equals(fileName) && Utils.getFileSize(file) > size){
                        deleteFile(fileName);
                    }
                }
            }
        }*/
        Log.e(TAG, "待下载的内容："+url);


        LoaderInfo loaderInfo = (new LoaderInfo.Builder()).dir(DIRECTORY).name(fileName).url(url).splitter(5).build();
        final FileDownLoad finalFileDownLoad = fileDownLoad;
        downLoadFiles.put(finalFileDownLoad,STATUS_DOWNLOADING);//正在下载此文件
        LoaderExecutor.load(loaderInfo, new LoaderListener() {
            @Override
            public void onLoad(String url, int size, int totalSize) {
                //刷新UI
                ServiceManager.getInstance().updateUI();
                Log.e(TAG, "开始,下载......");
                Log.e(TAG, "总大小："+totalSize+"下载大小:"+size+"下载进度："+((float)size/(float) totalSize));
                ServiceManager.getInstance().updateprocess("总大小："+totalSize+"下载大小:"+size+"下载进度："+((float)size/(float) totalSize));
            }

            @Override
            public void onCompleted(String url, String path) {
                super.onCompleted(url, path);
                Log.e(TAG, "完成,下载......");
                ServiceManager.getInstance().updateprocess("完成,下载......");
                //移除下载项
                downLoadFiles.remove(finalFileDownLoad);
                startDownLoad();
                //下载完成,发送下载完成指令
                ServiceManager.getInstance().sendDownloadAck(fileName);
                //刷新UI
                ServiceManager.getInstance().updateUI();
            }

            @Override
            public void onCancelled(String url) {
                super.onCancelled(url);
                Log.e(TAG, "取消,下载......"+url);
                //移除下载项
                downLoadFiles.remove(finalFileDownLoad);
                //开始下一项
                startDownLoad();
            }

            @Override
            public void onError(String url, LoaderError err) {
                super.onError(url, err);
                Log.e(TAG, "异常,下载......"+err.getMsg()+"----错误码:"+err.getCode());
                ServiceManager.getInstance().updateprocess("异常,下载......");
                //更新状态
                downLoadFiles.put(finalFileDownLoad,STATUS_ERROR_DOWNLOAD);
                //开始下一项
                startDownLoad();
            }
        });
    }

    /**
     * 移除下载项
     * @param url
     */
    private void clearItem(String url){
//        if (url == null || url.equals("") ){
//            return;
//        }
//        if (downLoadFiles != null && downLoadFiles.size() > 0){
//            Iterator<FileDownLoad> sListIterator = downLoadFiles.iterator();
//            while(sListIterator.hasNext()){
//                FileDownLoad file = sListIterator.next();
//                if(url.equals(file.fileUrl)){
//                    Log.e(TAG, "移除下载任务:"+file.fileUrl);
//                    sListIterator.remove();
//                }
//            }
//        }
    }

    public List<VideoItem> getVideoFiles(){
        List<String> fileList = new ArrayList<>();
        List<String> downLoading = new ArrayList<>();
        File fileDir = new File(DIRECTORY);
        if (fileDir != null && fileDir.listFiles() != null && fileDir.listFiles().length > 0){
            for (File file : fileDir.listFiles()) {
                if (file.getAbsolutePath().endsWith(PATCH_SUFFIX)){
                    fileList.add(file.getName());
                }else if (file.getAbsolutePath().endsWith(TEMP_SUFFIX)){
                    downLoading.add(file.getName());
                }
            }
        }

        //过滤掉正在下载的
        List<String> list = new ArrayList<>();
        for (String s:fileList) {
            boolean is = false;
            for (String s2:downLoading) {
                if (s2.equals(s+TEMP_SUFFIX)){
                    is = true;
                }
            }
            if (is == false){
                list.add(s);
            }
        }

        //搞完本地文件
        List<VideoItem> items = new ArrayList<>();
        for (String s:list) {
            String[] temp = s.split(FILE_SUFFIX);
            VideoItem item = new VideoItem();
            item.fileId = temp[0];
            item.fileName = s;
            item.fileNameShow = temp[1];
            item.fileStatus = STATUS_COMPLETE_DOWNLOAD;
            items.add(item);
        }

        //正在下载的文件搞进来
        for (Map.Entry<FileDownLoad, Integer> entry : downLoadFiles.entrySet()) {
            FileDownLoad file = entry.getKey();
            VideoItem item = new VideoItem();
            item.fileId = file.fileId;
            item.fileNameShow = file.fileNameShow;
            item.fileName = file.fileId + FILE_SUFFIX + file.fileNameShow;
            item.fileStatus = entry.getValue();
            if (item.fileStatus == STATUS_DOWNLOADING){//计算进度
                long size = Utils.getFileSize(new File(DIRECTORY + "/" + item.fileName));
                float progress = size*100 / file.fileSize;
                float num = (float)Math.round(progress*10)/10;
                item.progress = num;
            }
            items.add(item);
        }

        return items;
    }


    /**
     * 删除下载任务
     * @param fileId
     */
    public void deteTask(String fileId){
        Iterator<Map.Entry<FileDownLoad, Integer>> iter = downLoadFiles.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<FileDownLoad, Integer> entry =  iter.next();
            FileDownLoad file = entry.getKey();
            if (file.fileId.equals(fileId)){
                iter.remove();
                //刷新UI
                ServiceManager.getInstance().updateUI();
            }
        }
    }

    /**
     * 重试
     */
    public void repty(String fileId){
        Iterator<Map.Entry<FileDownLoad, Integer>> iter = downLoadFiles.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<FileDownLoad, Integer> entry =  iter.next();
            FileDownLoad file = entry.getKey();
            if (file.fileId.equals(fileId)){
                entry.setValue(STATUS_TO_DOWNLOAD);
                //刷新UI
                ServiceManager.getInstance().updateUI();
            }
        }
    }




}
