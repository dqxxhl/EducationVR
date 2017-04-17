package com.sd.vr.education.network.socket;

import android.util.Log;

import com.sd.vr.ctrl.netty.protobuf.MessageProto;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

/**
 * Created by hl09287 on 2017/4/17.
 */

@ChannelHandler.Sharable
public class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask{

    private static final String TAG = ConnectionWatchdog.class.getName();
    private final Bootstrap bootstrap;
    private final Timer timer;
    private final int port;
    private final String host;
    private volatile boolean reconnect = true;
    private int attempts;
    private NettyClient nettyClient;

    public ConnectionWatchdog(Bootstrap bootstrap, String host, int port, Timer timer, NettyClient nettyClient, boolean reconnect){
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
        this.timer = timer;
        this.nettyClient = nettyClient;
        this.reconnect = reconnect;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "当前链路已经激活了，重连尝试次数重新置为0");
        attempts = 0;
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.e(TAG, "链接关闭");
        if(reconnect){
            if (attempts < 12) {
                attempts++;
            }else {
                return;
            }
            Log.e(TAG, "链接关闭,将进行重连");
            int timeout = 2 * attempts;
            Log.e(TAG, "重连时间间隔："+attempts);
            timer.newTimeout(this, timeout, TimeUnit.SECONDS);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        ChannelFuture f;
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<Channel>(){
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                    ch.pipeline().addLast(new ProtobufDecoder(MessageProto.MessageResponse.getDefaultInstance()));
                    ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                    ch.pipeline().addLast(new ProtobufEncoder());
                    ch.pipeline().addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new ClientHandler(nettyClient));
                    ch.pipeline().addLast(ConnectionWatchdog.this);
                    ch.pipeline().addLast(new HeartBeatClientHandler());
                }
            });
            f = bootstrap.connect(host,port);
        }

        f.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                boolean succeed = f.isSuccess();
                if (!succeed) {
                    Log.e(TAG, "重连失败");
                    f.channel().pipeline().fireChannelInactive();
                }else{
                    Log.e(TAG, "重连成功");
                    SocketChannel socketChannel = (SocketChannel) f.channel();
                    nettyClient.setSocketChannel(socketChannel);
                }
            }
        });
    }
}
