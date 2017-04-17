package com.sd.vr.education.network.socket;

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
 * Created by hl09287 on 2017/3/27.
 */

public class NettyClient {

    public static final int RECEIVE_RESPONSE = 1;
    private String mHost = null;
    private int mPort;
    private ServiceManager.UIhandler mUIHandler;//负责主线程的消息分发
    public ConnectionThread mConnectionThread;
    private SocketChannel socketChannel;


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

    public void sendRequest(MessageProto.MessageRequest request){
//        mConnectionThread.getSocketChannel().writeAndFlush(request);
        socketChannel.writeAndFlush(request);
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

        public ConnectionThread(String host, int port) {
            this.mHost = host;
            this.mPort = port;
        }

        @Override
        public void run() {
            final HashedWheelTimer timer = new HashedWheelTimer();
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            final Bootstrap bootstrap = new Bootstrap();
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
                    ch.pipeline().addLast(new ConnectionWatchdog(bootstrap, mHost, mPort,timer, NettyClient.this, true));
                    ch.pipeline().addLast(new HeartBeatClientHandler());
                }
            });

            try {
                ChannelFuture f = bootstrap.connect(mHost, mPort).sync();
                if (f.isSuccess()){
                    socketChannel = (SocketChannel) f.channel();
                }
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
