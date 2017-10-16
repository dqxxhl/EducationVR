package com.sd.vr.education.network.socket;

import android.util.Log;

import com.sd.vr.education.presenter.ServiceManager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by hl09287 on 2017/3/24.
 */

public class ClientHandler extends SimpleChannelInboundHandler {

    private static final String TAG = ClientHandler.class.getName();

    private NettyClient mNettyClient;
    public ClientHandler(NettyClient nettyClient){
        this.mNettyClient = nettyClient;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.e(TAG, "断线了");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.e(TAG, "出现异常了");
        cause.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Log.e(TAG, "链接成功");
        ServiceManager.getInstance().showTips("链接主控成功");
        mNettyClient.register();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        mNettyClient.handlerReceiveResponse(msg);
    }
}
