package com.sd.vr.education.presenter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.os.Environment;
import android.util.Log;
import android.widget.VideoView;

import com.sd.vr.education.entity.VideoFile;
import com.sd.vr.education.network.http.downloader.ErrorCode;
import com.sd.vr.education.network.http.downloader.LoaderExecutor;
import com.sd.vr.education.network.http.downloader.LoaderListener;
import com.sd.vr.education.network.http.downloader.LoaderInfo;
import com.sd.vr.education.network.http.downloader.blockload.LoaderTask;
import com.sd.vr.education.utils.DatabaseManager;
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

    //下载状态定义
    public static final int STATUS_TO_DOWNLOAD = 0;//待下载
    public static final int STATUS_DOWNLOADING = 1;//下载中
    public static final int STATUS_ERROR_DOWNLOAD = 2;//下载异常
    public static final int STATUS_COMPLETE_DOWNLOAD = 3;//下载完成



    public HashMap<VideoFile,Integer> downLoadFiles = new HashMap<>();
    private static FilesManager mFilesManager;

    private LoaderTask task;

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
    public void downLoad(VideoFile fileDownLoad){
        if (fileDownLoad == null || fileDownLoad.getFileUrl().equals("") || fileDownLoad.getFileId().equals("") || fileDownLoad.getFileSize() == 0){
            return;
        }
        if (downLoadFiles == null){
            downLoadFiles = new HashMap<>();
        }
        //如果已存在次下载项不再下载
        if (downLoadFiles.size() > 0){
            for (Map.Entry<VideoFile, Integer> entry : downLoadFiles.entrySet()) {
                VideoFile file = entry.getKey();
                if (file.getFileId().equals(fileDownLoad.getFileId())){
                    return;
                }
            }
        }
        //校验本地是否存在这个文件,若文件已存在但是大小校验不通过，需首先删除本地文件
        String fileName = fileDownLoad.getFileId();
        long size = fileDownLoad.getFileSize();
        File fileDir = new File(DIRECTORY);
        if (fileDir != null && fileDir.listFiles() != null && fileDir.listFiles().length > 0){
            for (File file : fileDir.listFiles()) {
                if (file.getAbsolutePath().endsWith(PATCH_SUFFIX)){
                    if (file.getName().equals(fileName) && Utils.getFileSize(file) == size){
                        //文件已下载
                        ServiceManager.getInstance().sendDownloadAck(fileDownLoad.getFileId());
                        //更新UI
                        ServiceManager.getInstance().updateUI();
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
    public void downLoad(List<VideoFile> files){
        if (files == null || files.size() == 0){
            return;
        }
        for (VideoFile file: files) {
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

        VideoFile fileDownLoad = null;
        boolean isDownLoading = false;
        for (Map.Entry<VideoFile, Integer> entry : downLoadFiles.entrySet()) {
            VideoFile file = entry.getKey();
            int status = entry.getValue();
            if (status == STATUS_DOWNLOADING){
                isDownLoading = true;
            }

            if (status == STATUS_TO_DOWNLOAD && fileDownLoad == null){
                fileDownLoad = file;
            }
        }

        if (isDownLoading || fileDownLoad == null){
            return;
        }

        final String fileName = fileDownLoad.getFileId();
        String url = fileDownLoad.getFileUrl();

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
        final VideoFile finalFileDownLoad = fileDownLoad;
        downLoadFiles.put(finalFileDownLoad,STATUS_DOWNLOADING);//正在下载此文件
        task = LoaderExecutor.load(loaderInfo, new LoaderListener() {
            @Override
            public void onLoad(String url, int size, int totalSize) {
                //刷新UI
                float temp = (float)size*100/(float) totalSize;
                float progress = (float)Math.round(temp*10)/10;
                if (Math.abs(finalFileDownLoad.getProgress() - progress) > 1){
                    finalFileDownLoad.setProgress(progress);
                    ServiceManager.getInstance().updateUI();
                }

                Log.e(TAG, "开始,下载......");
                Log.e(TAG, "总大小："+totalSize+"下载大小:"+size+"下载进度："+((float)size/(float) totalSize));
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
                ServiceManager.getInstance().sendDownloadAck(finalFileDownLoad.getFileId());
                //刷新UI
                finalFileDownLoad.setProgress(100f);
                ServiceManager.getInstance().updateUI();
            }

            @Override
            public void onCancelled(String url) {
                super.onCancelled(url);
                Log.e(TAG, "取消,下载......"+url);
                //移除下载项
//                downLoadFiles.remove(finalFileDownLoad);
                //开始下一项
//                startDownLoad();
            }

            @Override
            public void onError(String url, ErrorCode err) {
                super.onError(url, err);
                Log.e(TAG, "异常,下载......"+err.getMsg()+"----错误码:"+err.getCode());
                ServiceManager.getInstance().updateprocess("异常,下载......");
                //更新状态
                downLoadFiles.put(finalFileDownLoad,STATUS_ERROR_DOWNLOAD);
                //开始下一项
                startDownLoad();
                ServiceManager.getInstance().updateUI();
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

    public List<VideoFile> getVideoFiles(){

        List<VideoFile> items = new ArrayList<>();
        List<VideoFile> list = DatabaseManager.getInstance().getQueryAll(VideoFile.class);
        if (list == null || list.size() == 0){
            return items;
        }

        //本地文件，已下载+正在下载+已暂停+遗留文件
        List<String> fileList = new ArrayList<>();
        File fileDir = new File(DIRECTORY);
        if (fileDir != null && fileDir.listFiles() != null && fileDir.listFiles().length > 0){
            for (File file : fileDir.listFiles()) {
                if (file.getAbsolutePath().endsWith(PATCH_SUFFIX)){
                    fileList.add(file.getName());
                }
            }
        }

        for (VideoFile videofile : list) {
            String fileId = videofile.getFileId();
            boolean isCheck = false;
            //内存文件，正在下载+等待下载+已暂停(内存搜索)
            for (Map.Entry<VideoFile, Integer> entry : downLoadFiles.entrySet()) {
                VideoFile file = entry.getKey();
                if (fileId.equals(file.getFileId())){
                    int status = entry.getValue();
                    videofile.setFileStatus(status);
                    if (status == STATUS_DOWNLOADING){
                        long size = Utils.getFileSize(new File(DIRECTORY + "/" + fileId));
                        float progress = size*100 / file.getFileSize();
                        float num = (float)Math.round(progress*10)/10;
                        Log.e(TAG, "size:"+size+"fileSize:"+file.getFileSize()+"process:"+num);

                        float newNum = 0.0f;
                        if (task != null && task.getmConfigDesc() != null){
                            long fileSize = (long) task.getmConfigDesc().fileSize();
                            long length = (long)task.getmConfigDesc().loadedLength();
                            if (fileSize > 0){
                                newNum = length*100/fileSize;
                                Log.e(TAG, "进度-------："+newNum+"baifenbi:"+newNum+"loadedLength()"+task.getmConfigDesc().loadedLength()+"fileSize()"+task.getmConfigDesc().fileSize());
                            }
                        }
                        videofile.setProgress(newNum);


                    }
                    items.add(videofile);
                    isCheck = true;
                }
            }

            if (isCheck){
                continue;
            }

            //本地文件过滤
            for (String vediofileId:fileList) {
                if (fileId.equals(vediofileId)){
                    long size = Utils.getFileSize(new File(DIRECTORY + "/" + fileId));
                    if (size == videofile.getFileSize()){
                        videofile.setFileStatus(STATUS_COMPLETE_DOWNLOAD);
                    }else{
                        videofile.setFileStatus(STATUS_ERROR_DOWNLOAD);
                    }
                    items.add(videofile);
                    isCheck = true;
                }
            }

            if (isCheck){
                continue;
            }
            videofile.setFileStatus(STATUS_ERROR_DOWNLOAD);
            items.add(videofile);
        }

        return items;
    }


    /**
     * 删除下载任务
     * @param fileId
     */
    public void deteTask(String fileId){
        Iterator<Map.Entry<VideoFile, Integer>> iter = downLoadFiles.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<VideoFile, Integer> entry =  iter.next();
            VideoFile file = entry.getKey();
            if (file.getFileId().equals(fileId)){
                if(file.getFileStatus() == STATUS_DOWNLOADING){
                    task.stop();
                }
                iter.remove();
                startDownLoad();
                //刷新UI
                ServiceManager.getInstance().updateUI();
            }
        }
    }

    /**
     * 重试
     */
    public void repty(String fileId){
        Iterator<Map.Entry<VideoFile, Integer>> iter = downLoadFiles.entrySet().iterator();
        boolean isIn = false;
        while (iter.hasNext()) {
            Map.Entry<VideoFile, Integer> entry =  iter.next();
            VideoFile file = entry.getKey();
            if (file.getFileId().equals(fileId)){
                entry.setValue(STATUS_TO_DOWNLOAD);
                isIn = true;
                //刷新UI
                ServiceManager.getInstance().updateUI();
            }
        }

        if (isIn){
            startDownLoad();
        }else {
            List<VideoFile> files = DatabaseManager.getInstance().getQueryByWhere(VideoFile.class, "fileId",new String[]{fileId});
            if (files.size() > 0){
                downLoad(files);
            }
        }

    }

    public File getFile(String fileId){
        File fileDir = new File(DIRECTORY);
        for (File file : fileDir.listFiles()) {
            if (file.getAbsolutePath().contains(fileId)){
                return file;
            }
        }
        return null;
    }

    /**
     * 停止下载任务
     */
    public void stopDownLoad(){

        LoaderExecutor.cancel(task);
        downLoadFiles.clear();
        ServiceManager.getInstance().updateUI();

    }

    public void reStartDownload(){
        Iterator<Map.Entry<VideoFile, Integer>> iter = downLoadFiles.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<VideoFile, Integer> entry =  iter.next();
            VideoFile file = entry.getKey();
            downLoadFiles.put(file, STATUS_TO_DOWNLOAD);
        }

        startDownLoad();
    }




}
