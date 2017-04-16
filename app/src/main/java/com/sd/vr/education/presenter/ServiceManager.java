package com.sd.vr.education.presenter;

import static com.sd.vr.ctrl.netty.protobuf.MessageProto.CtrlDictateNotice;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.Dictate;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.MessageRequest;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.MessageResponse;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.RespStatus;

import com.sd.vr.ctrl.netty.protobuf.MessageProto;
import com.sd.vr.education.VREducationMainActivity;
import com.sd.vr.education.entity.FileDownLoad;
import com.sd.vr.education.network.socket.NettyClient;
import com.sd.vr.education.utils.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 网络请求管理类
 * @author QingYuhan
 * @since 2017/3/16
 */

public class ServiceManager {

    private static ServiceManager serviceManager = null;
    private static final String TAG = ServiceManager.class.getName();
    private static final String HOST = "115.29.226.88";
    private static final int PORT = 8011;
    private static final String SPLIT = ",";

    private NettyClient mClient;
    private ViewAction mAction;
    private VideoAction mVideoAction;
    private Context mContext;

    private ServiceManager(){
        Log.e(TAG, "init ServiceManager");
        initSocketClient();
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
    private void initSocketClient(){
        mClient = new NettyClient(HOST, PORT);
        mClient.setmUIHandler(new UIhandler());
        mClient.start();
    }

    public void sendRequest(MessageRequest request){
        mClient.sendRequest(request);
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

    /**
     * 向服务端注册设备
     */
    public void register(){
        MessageProto.ReConnectRequest reConnectRequest = MessageProto.ReConnectRequest.newBuilder().setEventId("REGISTER").setEquipmentId(Utils.getDeviceId(mContext)).build();
        MessageProto.MessageRequest request = MessageProto.MessageRequest.newBuilder().setType(MessageProto.Types.RECONNECT).setReConnectRequest(reConnectRequest).build();
        System.out.println("发送数据："+request.toString());
        sendRequest(request);
    }


    public class UIhandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG,"收到消息："+msg.obj.toString());
            if (msg.obj != null && (msg.obj instanceof MessageResponse)){
                MessageResponse messageResponse = (MessageResponse) msg.obj;
                mAction.showToast(messageResponse.toString()+"");

                if (messageResponse.getStatus().equals(RespStatus.SUCCESS)){
                    switch (messageResponse.getType()){

                        case CTRL_DICTATE_NOTICE :
                            // 接收到控制指令
                            CtrlDictateNotice ctrlDictateNotice = messageResponse.getCtrlDictateNotice();
                            handleCtrlDictateNotice(ctrlDictateNotice);
                            break;
                        case DOWNLOAD_NOTICE://下载文件
                            MessageProto.DownLoadNotice downLoadNotice = messageResponse.getDownLoadNotice();
                            String fileIdsDownLoad = downLoadNotice.getFileIds();
                            if (fileIdsDownLoad == null || fileIdsDownLoad.equals("")){
                                return;
                            }
                            String[] fileIdsTempDownLoad = fileIdsDownLoad.split(SPLIT);

                            String fileSize = downLoadNotice.getFileSize();
                            if (fileSize == null || fileSize.equals("")){
                                return;
                            }
                            String[] fileSizeDownLoad = fileSize.split(SPLIT);

                            if (fileIdsTempDownLoad.length != fileSizeDownLoad.length){
                                return;
                            }

                            if (fileIdsTempDownLoad.length > 0){
                                List<FileDownLoad> fileIdsList = new ArrayList<>();
                                for (int i = 0; i < fileIdsTempDownLoad.length; i++){//解析需要下载文件数组
                                    FileDownLoad temp = new FileDownLoad();
                                    temp.fileUrl = fileIdsTempDownLoad[i];
                                    temp.fileSize = Long.valueOf(fileSizeDownLoad[i]);
                                    if (temp.fileUrl != null && !temp.fileUrl.equals("") && temp.fileUrl.endsWith(FilesManager.PATCH_SUFFIX) && temp.fileSize > 0){
                                        temp.fileName = null;
                                        String tempString = "fileId=";
                                        int index = temp.fileUrl.indexOf(tempString);
                                        String target = temp.fileUrl.substring(index+tempString.length(), temp.fileUrl.length());
                                        temp.fileName = target;
                                        fileIdsList.add(temp);
                                    }
                                }
                                FilesManager.getInstance().downLoad(fileIdsList);
                            }

                            break;
                        case PLAY_PROGRESS_NOTICE://跳转播放器指定位置
                            MessageProto.PlayProgressNotice playProgressNotice = messageResponse.getPlayProgressNotice();
                            if (mVideoAction != null){
                                if (playProgressNotice.getPosition() != null && !playProgressNotice.getPosition().equals("")){
                                    mVideoAction.seekTo(Long.valueOf(playProgressNotice.getPosition()));
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
                        case PLAY_PROGRESS:
                            MessageProto.PlayProgressResponse playProgressResponse = messageResponse.getPlayProgressResponse();
                            String progress = playProgressResponse.getPosition();
                            if (progress == null || progress.equals("")){
                                return;
                            }
                            long progressLong = Long.valueOf(progress);
                            if (progressLong >= 0 && mVideoAction != null){
                                mVideoAction.seekTo(progressLong);
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
                            mVideoAction.start(ctrlDictateNotice.getFileId());
                        }
                    }else {
                        //打开一个VideoPlayerActivity
                        if (mAction != null){
                            if (ctrlDictateNotice.getFileId() != null && !ctrlDictateNotice.getFileId().equals("")){
                                mAction.start(ctrlDictateNotice.getFileId());
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
                        mVideoAction.play();
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
