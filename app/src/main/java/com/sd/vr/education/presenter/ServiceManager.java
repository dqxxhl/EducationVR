package com.sd.vr.education.presenter;

import static com.sd.vr.ctrl.netty.protobuf.MessageProto.CtrlDictateNotice;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.Dictate;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.MessageRequest;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.MessageResponse;
import static com.sd.vr.ctrl.netty.protobuf.MessageProto.RespStatus;

import com.sd.vr.ctrl.netty.protobuf.MessageProto;
import com.sd.vr.education.network.socket.NettyClient;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

    private NettyClient mClient;
    private ViewAction mAction;
    private VideoAction mVideoAction;

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

    public class UIhandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            System.out.println("收到消息："+msg.obj.toString());
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
                        case DOWNLOAD_NOTICE:
                            MessageProto.DownLoadNotice downLoadNotice = messageResponse.getDownLoadNotice();

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
                            String[] fileIdsTemp = fileIds.split(",");
                            if (fileIds.length() > 0 ){
                                List<String> fileIdsList = Arrays.asList(fileIdsTemp);
                                FilesManager.getInstance().deleteFiles(fileIdsList);
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
