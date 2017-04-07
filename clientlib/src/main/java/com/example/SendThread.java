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

/**
 * Created by hl09287 on 2017/3/27.
 */

public class SendThread extends Thread {

    private SocketChannel socketChannel;

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
                ch.pipeline().addLast(new ClientHandler());
            }
        });

        try {
            ChannelFuture f = bootstrap.connect("115.29.226.88", 8011).sync();
            System.out.println(f.isSuccess());
            socketChannel = (SocketChannel) f.channel();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
