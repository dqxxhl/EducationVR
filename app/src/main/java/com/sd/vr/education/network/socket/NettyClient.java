package com.sd.vr.education.network.socket;

import android.os.Handler;
import android.util.Log;

import com.sd.vr.ctrl.netty.protobuf.MessageProto;
import com.sd.vr.education.presenter.ServiceManager;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

/**
 * TCP客户端
 * Created by hl09287 on 2017/3/27.
 */

public class NettyClient {

    private static final String TAG = NettyClient.class.getName();
    public static final int RECEIVE_RESPONSE = 1;

    public String getmHost() {
        return mHost;
    }

    private String mHost = null;
    private int mPort;
    private ServiceManager.UIhandler mUIHandler;//负责主线程的消息分发
    public ConnectionThread mConnectionThread;
    public SocketChannel socketChannel;
    public boolean isChonglian = true;


    public NettyClient(String host, int port){
        this.mHost = host;
        this.mPort = port;
    }

    public void setmUIHandler(ServiceManager.UIhandler mUIHandler) {
        this.mUIHandler = mUIHandler;
    }

    public void start(){

        mConnectionThread = new ConnectionThread(mHost, mPort);
        mConnectionThread.start();
    }

    public void stop(){

        this.isChonglian = false;
        if (socketChannel != null) {
            socketChannel.close();
        }

        if (mConnectionThread!=null){
            mConnectionThread.interrupt();
            mConnectionThread.clearThread();
        }



    }

    /**
     * 网络恢复后重连服务器
     */
    public void notifyNetworkChange(){
        if (mConnectionThread == null){
            start();
        }
        Thread.State state = mConnectionThread.getState();
        if (state.equals(Thread.State.TERMINATED)){
            mConnectionThread.getTimer().stop();
            Log.e(TAG, "mConnectionThread已跑完,新建一个线程");
            start();
        }
    }

    public void sendRequest(MessageProto.MessageRequest request){
        if (socketChannel != null){
            Log.e(TAG, "发送消息:\n"+request.toString());
            socketChannel.writeAndFlush(request);
        }
    }

    public void handlerReceiveResponse(Object msg){
        mUIHandler.sendMessage(mUIHandler.obtainMessage(NettyClient.RECEIVE_RESPONSE, msg));
    }

    public void register(){
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                //注册设备
                ServiceManager.getInstance().register();
            }
        });
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    private class ConnectionThread extends Thread{

        private String mHost = null;
        private int mPort;
        private int reconnect;
        private Handler handler = new Handler();
        private HashedWheelTimer timer = new HashedWheelTimer();

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap();
        private ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, mHost, mPort,timer, NettyClient.this, true);

        public ConnectionThread(String host, int port) {
            this.mHost = host;
            this.mPort = port;
        }

        public HashedWheelTimer getTimer() {
            return timer;
        }

        public void clearThread(){
            if (watchdog != null){
                watchdog.stopTimer();
            }
        }

        @Override
        public void run() {

            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(eventLoopGroup);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                    ch.pipeline().addLast(new ProtobufDecoder(MessageProto.MessageResponse.getDefaultInstance()));
                    ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                    ch.pipeline().addLast(new ProtobufEncoder());
                    ch.pipeline().addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new ClientHandler(NettyClient.this));
                    isChonglian = true;
                    ch.pipeline().addLast(watchdog);
                    ch.pipeline().addLast(new HeartBeatClientHandler());
                }
            });
            connect(bootstrap);
        }

        private void connect(Bootstrap bootstrap){
            try {
                ChannelFuture f = bootstrap.connect(mHost, mPort).sync();
                if (f.isSuccess()){
                    Log.e(TAG, "连接服务器成功"+"mHost:"+mHost);
                    mUIHandler.sendMessage(mUIHandler.obtainMessage(ServiceManager.SAVE_IP, mHost));
                    socketChannel = (SocketChannel) f.channel();
                }
                f.channel().closeFuture().sync();
                ServiceManager.getInstance().showTips("链接主控成功");
            } catch (Exception e) {
                if (isChonglian){
                    Log.e(TAG, "连接主控失败,尝试重连");
                    if (reconnect <= 1){
                        ServiceManager.getInstance().showTips("链接主控失败，请确认网络连接");
                    }
                    e.printStackTrace();
                    //连接不成功，继续尝试连接
                    reconnect++;
                    if (reconnect < 200){
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        connect(bootstrap);
                    }
                }
            }
        }
    }
}
