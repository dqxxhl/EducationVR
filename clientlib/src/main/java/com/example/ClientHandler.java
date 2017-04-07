package com.example;

import com.sd.vr.ctrl.netty.protobuf.MessageProto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by hl09287 on 2017/3/24.
 */

public class ClientHandler extends SimpleChannelInboundHandler {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        System.out.println("断线了。---------");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println("出现异常了。。。。。。。。。。。。。");
        cause.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);



    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Response:\n"+msg.toString());
    }
}
