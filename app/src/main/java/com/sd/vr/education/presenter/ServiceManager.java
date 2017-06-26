package com.sd.vr.education.presenter;

import static com.sd.vr.ctrl.netty.protobuf.MessageProto.CtrlDictateNotice;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.Dictate;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.MessageRequest;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.MessageResponse;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.RespStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sd.vr.ctrl.netty.protobuf.MessageProto;
import com.sd.vr.education.entity.VideoFile;
import com.sd.vr.education.network.socket.NettyClient;
import com.sd.vr.education.utils.DatabaseManager;
import com.sd.vr.education.utils.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 网络请求管理类
 * @author QingYuhan
 * @since 2017/3/16
 */

public class ServiceManager {

    private static ServiceManager serviceManager = null;
    private static final String TAG = ServiceManager.class.getName();
    private static final String HOST = "172.17.20.41";
    private static final int PORT = 8011;
    private static final String SPLIT = ",";
    public static final int SAVE_IP = 101;

    private NettyClient mClient;
    private ViewAction mAction;
    private VideoAction mVideoAction;
    private Context mContext;

    private ServiceManager(){
        Log.e(TAG, "init ServiceManager");
//        initSocketClient();
    }

    public static synchronized ServiceManager getInstance(){
        if (serviceManager == null){
            serviceManager = new ServiceManager();
        }

        return serviceManager;
    }

    public void initContext(Context context){
        this.mContext = context;
    }

    //初始化Socket
    public void initSocketClient(String host){
        if (mClient != null){
            FilesManager.getInstance().stopDownLoad();
            mClient.stop();
        }
        mClient = new NettyClient(host, PORT);
        mClient.setmUIHandler(new UIhandler());
        mClient.start();
    }

    public void tryInit(String host){
        if (mClient == null){
            mClient = new NettyClient(host, PORT);
            mClient.setmUIHandler(new UIhandler());
            mClient.start();
        }else {
            register();
        }
    }

    public void sendRequest(MessageRequest request){
        if (mClient != null){
            mClient.sendRequest(request);
        }
    }

    public void bindAction(ViewAction action){
        this.mAction = action;
    }

    public void bindVideoAction(VideoAction action){
        this.mVideoAction = action;
    }

    public void unBindVideoAction(){
        this.mVideoAction = null;
    }

    public void onNetChange(int netWorkState){
        Log.e(TAG, "网络状态变化:"+netWorkState);
        if (netWorkState != Utils.NETWORK_NONE){
            Log.e(TAG, "网络恢复了");
            //连接服务器
            if (mClient != null){
                mClient.notifyNetworkChange();
            }
            // 启动下载引擎
            FilesManager.getInstance().reStartDownload();
        }

        if (mAction != null){
            mAction.updateWiFi(netWorkState);
        }
    }

    public void updateprocess(String process){
        if (mAction != null){
            mAction.updateprocess(process);
        }
    }

    public void updateUI(){
        if (mAction != null){
            mAction.uodateUI();
        }
    }

    /**
     * 更新电量数据
     * @param batteryPct
     */
    public void updateDianliang(float batteryPct){
        if(mAction != null){
            mAction.updateDianliang(batteryPct);
        }
    }

    /**
     * 向服务端注册设备
     */
    public void register(){
        //向服务端发送数据
        MessageProto.RegisterRequest registerRequest = MessageProto.RegisterRequest.newBuilder().setEventId("REGISTER").setEquipmentId(Utils.getDeviceId(mContext)).build();
        MessageProto.MessageRequest request = MessageProto.MessageRequest.newBuilder().setType(MessageProto.Types.REGISTER).setRegisterRequest(registerRequest).build();
        sendRequest(request);
    }

    /**
     * 文件下载完接口
     * @param fileId
     */
    public void sendDownloadAck(String fileId){
        MessageProto.DownloadAck downloadAck = MessageProto.DownloadAck.newBuilder().setEventId("DOWNLOAD_ACK").setEquipmentId(Utils.getDeviceId(mContext)).setFileId(fileId).build();
        MessageProto.MessageRequest request = MessageProto.MessageRequest.newBuilder().setType(MessageProto.Types.DOWNLOAD_ACK).setDownloadAck(downloadAck).build();
        sendRequest(request);
    }

    /**
     * 向服务器请求当前的播放进度
     */
    public void requestProgress(){
        MessageProto.PlayProgressRequest playProgressRequest = MessageProto.PlayProgressRequest .newBuilder().setEventId("PLAY_PROGRESS").setEquipmentId(Utils.getDeviceId(mContext)).build();
        MessageProto.MessageRequest request = MessageProto.MessageRequest.newBuilder().setType(MessageProto.Types.PLAY_PROGRESS).setPlayProgressRequest(playProgressRequest).build();
        sendRequest(request);
    }

    public class UIhandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG,"收到消息："+msg.obj.toString());
            if (msg.what == SAVE_IP){
                String ip = (String) msg.obj;
                Utils.saveIP(mContext, ip);
            }

            if (msg.obj != null && (msg.obj instanceof MessageResponse)){
                MessageResponse messageResponse = (MessageResponse) msg.obj;
                if (mAction != null){
                    mAction.showToast(messageResponse.toString()+"");
                }

                if (messageResponse.getStatus().equals(RespStatus.SUCCESS)){
                    switch (messageResponse.getType()){

                        case CTRL_DICTATE_NOTICE :
                            // 接收到控制指令
                            CtrlDictateNotice ctrlDictateNotice = messageResponse.getCtrlDictateNotice();
                            handleCtrlDictateNotice(ctrlDictateNotice);
                            break;
                        case DOWNLOAD_NOTICE://下载文件
                            MessageProto.DownLoadNotice downLoadNotice = messageResponse.getDownLoadNotice();
                            List<MessageProto.MessageInfo> list = downLoadNotice.getMessageInfoList();
                            String key = downLoadNotice.getKey();
                            String length = downLoadNotice.getLength();

                            List<VideoFile> videoFileList = new ArrayList<>();
                            for (MessageProto.MessageInfo info : list) {//组装实体
                                VideoFile file = new VideoFile();
                                String temp = info.getFileId();
                                if (temp != null && !temp.equals("") && temp.endsWith(FilesManager.PATCH_SUFFIX)){
                                    String tempString = "fileId=";
                                    int index = temp.indexOf(tempString);
                                    String target = temp.substring(index+tempString.length(), temp.length());
                                    file.setFileId(target);
                                }
                                file.setFileUrl(temp);
                                file.setFileName(info.getFileName());
                                file.setFileContent(info.getContent());
                                file.setFileSize(Utils.stringToLong(info.getFileSize()));
                                file.setFileTitle(info.getTitle());
                                file.setImageUrl(info.getImageUrl());
                                file.setKey(key);
                                file.setLength(Integer.valueOf(length));
                                videoFileList.add(file);
                                List<VideoFile> tempList = DatabaseManager.getInstance().getQueryByWhere(VideoFile.class, "fileId", new String[]{file.getFileId()});
                                DatabaseManager.getInstance().deleteList(tempList);
                                DatabaseManager.getInstance().insert(file);
                            }

                            FilesManager.getInstance().downLoad(videoFileList);
                            break;
                        case PLAY_PROGRESS_NOTICE://跳转播放器指定位置,通知事件
                            MessageProto.PlayProgressNotice playProgressNotice = messageResponse.getPlayProgressNotice();
                            if (mVideoAction != null){
                                if (playProgressNotice.getPosition() != null && !playProgressNotice.getPosition().equals("")){
                                    MessageProto.PlayStatus playStatus = playProgressNotice.getPlayStatus();
                                    int status = 0;
                                    if (playStatus != null && playStatus.equals(MessageProto.PlayStatus.PLAYI_ING)){
                                        status = 1;
                                    }

                                    mVideoAction.seekTo(Long.valueOf(playProgressNotice.getPosition()), status);
                                }
                            }
                            break;
                        case DELETE_NOTICE://删除指定文件集合
                            MessageProto.DeleteNotice deleteNotice = messageResponse.getDeleteNotice();
                            String fileIds = deleteNotice.getFileIds();
                            if (fileIds == null || fileIds.equals("")){
                                return;
                            }
                            String[] fileIdsTemp = fileIds.split(SPLIT);
                            if (fileIds.length() > 0 ){
                                List<String> fileIdsList = Arrays.asList(fileIdsTemp);
                                FilesManager.getInstance().deleteFiles(fileIdsList);
                            }
                            break;
                        case PLAY_PROGRESS://请求的状态
                            MessageProto.PlayProgressResponse playProgressResponse = messageResponse.getPlayProgressResponse();
                            String progress = playProgressResponse.getPosition();
                            if (progress == null || progress.equals("")){
                                return;
                            }
                            long progressLong = Long.valueOf(progress);
                            if (progressLong >= 0 && mVideoAction != null){
                                mVideoAction.seekTo(progressLong,1);
                            }
                            break;
                        default:
                            break;

                    }
                }


            }
        }

        //处理控制指令
        public void handleCtrlDictateNotice(CtrlDictateNotice ctrlDictateNotice){
            Dictate dictate = ctrlDictateNotice.getDictate();
            switch (dictate){

                case START:
                    if (mVideoAction != null){
                        if (ctrlDictateNotice.getFileId() != null && !ctrlDictateNotice.getFileId().equals("")){
                            File file = FilesManager.getInstance().getFile(ctrlDictateNotice.getFileId());
                            if (file != null){
                                mVideoAction.start(file.getName());
                            }
                        }
                    }else {
                        //打开一个VideoPlayerActivity
                        if (mAction != null){
                            if (ctrlDictateNotice.getFileId() != null && !ctrlDictateNotice.getFileId().equals("")){
                                String sizeString = ctrlDictateNotice.getFileSize();
                                if (sizeString != null && !sizeString.equals("")){
                                    long size = Long.valueOf(sizeString);
                                    if (size > 0){
                                        mAction.start(ctrlDictateNotice.getFileId(), size);
                                    }
                                }
                            }
                        }
                    }
                    break;
                case STOP:
                    if (mVideoAction != null){
                        mVideoAction.pause();
                    }
                    break;
                case PLAY:
                    if (mVideoAction != null){
                        if (ctrlDictateNotice.getPosition() != null && !ctrlDictateNotice.getPosition().equals("")){
                            String positionString =  ctrlDictateNotice.getPosition();
                            long position =Utils.stringToLong(positionString);
                            if (position >= 0){
                                mVideoAction.play(position);
                            }
                        }
                    }
                    break;
                case END:
                    if (mVideoAction != null){
                        mVideoAction.stop();
                    }
                    break;
                case CLOSE:

                    break;
                default:
                    break;
            }

        }
    }




}
