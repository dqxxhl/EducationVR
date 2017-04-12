package com.sd.vr.education.presenter;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sd.vr.education.network.socket.NettyClient;
import com.sd.vr.ctrl.netty.protobuf.MessageProto;

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

    public void sendRequest(MessageProto.MessageRequest request){
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
            if (msg.obj != null && (msg.obj instanceof MessageProto.MessageResponse)){
                MessageProto.MessageResponse messageResponse = (MessageProto.MessageResponse) msg.obj;
                mAction.showToast(messageResponse.toString()+"");

                if (messageResponse.getStatus().equals(MessageProto.RespStatus.SUCCESS)){
                    switch (messageResponse.getType()){
                    }
                }


            }
        }
    }




}
