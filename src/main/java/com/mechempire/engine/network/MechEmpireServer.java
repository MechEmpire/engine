package com.mechempire.engine.network;

import com.mechempire.engine.constant.ServerConstant;
import com.mechempire.engine.core.IServer;
import com.mechempire.engine.network.handles.GameServerHandler;
import com.mechempire.engine.network.session.builder.NettyTCPSessionBuilder;
import com.mechempire.sdk.proto.ResultMessageProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * package: com.mechempire.engine.server
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/17 下午3:33
 * <p>
 * Game Server
 */
@Slf4j
@Component
public class MechEmpireServer implements IServer {

    /**
     * boss 线程组用于处理连接工作
     */
    private final EventLoopGroup boss = new NioEventLoopGroup(1);

    /**
     * work 线程组用于数据处理
     */
    private final EventLoopGroup worker = new NioEventLoopGroup(1);

    @Resource
    private GameServerHandler gameServerHandler;

    @Resource
    private NettyTCPSessionBuilder nettyTCPSessionBuilder;

    @Override
    public void run() throws InterruptedException {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            ChannelFuture channelFuture = serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    // 服务端可连接队列数, 对应 TCP/IP 协议 listen 函数的 backlog 参数
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 设置 TCP 长连接, 一般如果两个小时内没有数据的通信时, TCP 会自动发送一个活动探测数据报文
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 将小的数据包包装成更大的帧进行传送, 提高网络的负载
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 心跳逻辑
                            socketChannel.pipeline().addLast(new IdleStateHandler(5, 5, 5));
                            // 添加 ProtobufVarint32FrameDecoder，主要用于 Protobuf 半包处理
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            socketChannel.pipeline().addLast(new ProtobufDecoder(ResultMessageProto.CommonData.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            socketChannel.pipeline().addLast(new ProtobufEncoder());
                            socketChannel.pipeline().addLast(gameServerHandler);
                        }
                    })
                    .bind(ServerConstant.host, ServerConstant.port)
                    .sync();
            log.info("server bind on {}:{}", ServerConstant.host, ServerConstant.port);
            if (channelFuture.isSuccess()) {
                log.info("server started successfully.");
            }
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("server start error: {}", e.getMessage(), e);
        } finally {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
        }
    }
}