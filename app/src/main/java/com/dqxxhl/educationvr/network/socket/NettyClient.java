package com.dqxxhl.educationvr.network.socket;

import com.dqxxhl.educationvr.presenter.ServiceManager;
import com.sd.vr.ctrl.netty.protobuf.MessageProto;

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

/**
 * Created by hl09287 on 2017/3/27.
 */

public class NettyClient {

    public static final int RECEIVE_RESPONSE = 1;
    private String mHost = null;
    private int mPort;
    private ServiceManager.UIhandler mUIHandler;//负责主线程的消息分发
    private ConnectionThread mConnectionThread;


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
        mConnectionThread.getSocketChannel().writeAndFlush(request);
    }

    public void handlerReceiveResponse(Object msg){
        mUIHandler.sendMessage(mUIHandler.obtainMessage(NettyClient.RECEIVE_RESPONSE, msg));
    }

    private class ConnectionThread extends Thread{

        private String mHost = null;
        private int mPort;
        private SocketChannel socketChannel;

        public ConnectionThread(String host, int port) {
            this.mHost = host;
            this.mPort = port;
        }

        public SocketChannel getSocketChannel() {
            return socketChannel;
        }

        @Override
        public void run() {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
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
                    ch.pipeline().addLast(new ClientHandler(NettyClient.this));
                }
            });

            try {
                ChannelFuture f = bootstrap.connect(mHost, mPort).sync();
                System.out.println(f.isSuccess());
                if (f.isSuccess()){
                    socketChannel = (SocketChannel) f.channel();
                }
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
