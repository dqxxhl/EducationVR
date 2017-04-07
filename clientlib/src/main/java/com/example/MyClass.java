package com.example;


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

public class MyClass {



    public static void main(String[] args) throws InterruptedException {

        SendThread sendThread = new SendThread();
        sendThread.start();


        MessageProto.ReConnectRequest reConnectRequest = MessageProto.ReConnectRequest.newBuilder().setEventId("1").setEquipmentId("1133232664545").build();

        MessageProto.MessageRequest request = MessageProto.MessageRequest.newBuilder().setType(MessageProto.Types.RECONNECT).setReConnectRequest(reConnectRequest).build();

        System.out.println("Request:\n"+request.toString());

        Thread.sleep(1000);
        sendThread.getSocketChannel().writeAndFlush(request);
    }
}
