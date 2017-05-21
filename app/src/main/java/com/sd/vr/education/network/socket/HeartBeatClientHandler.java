package com.sd.vr.education.network.socket;

import android.os.Message;
import android.util.Log;

import com.sd.vr.ctrl.netty.protobuf.MessageProto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 心跳包
 * Created by hl09287 on 2017/4/17.
 */

public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = HeartBeatClientHandler.class.getName();
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                // 发送心跳包
                Log.e(TAG, "发送心跳包");
                MessageProto.HeartBeatRequest heartBeatRequest = MessageProto.HeartBeatRequest.newBuilder().build();
                MessageProto.MessageRequest request = MessageProto.MessageRequest.newBuilder().setType(MessageProto.Types.HEARTBEAT).setHeartBeatRequest(heartBeatRequest).build();
                ctx.writeAndFlush(request);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
