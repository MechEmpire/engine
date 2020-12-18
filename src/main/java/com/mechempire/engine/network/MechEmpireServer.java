package com.mechempire.engine.network;

import com.mechempire.engine.constant.ServerConstant;
import com.mechempire.engine.core.IServer;
import com.mechempire.engine.network.handles.GameServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

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

    @Resource
    GameServerHandler gameServerHandler;

    @Override
    public void run() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .localAddress(new InetSocketAddress(ServerConstant.host, ServerConstant.port));
            log.info("server run on {}:{}", ServerConstant.host, ServerConstant.port);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new IdleStateHandler(
                                    ServerConstant.SESSION_HEART_READ_TIMEOUT,
                                    ServerConstant.SESSION_HEART_WRITE_TIMEOUT,
                                    ServerConstant.SESSION_HEART_ALL_TIMEOUT
                            )
                    );
                    socketChannel.pipeline().addLast(gameServerHandler);
                }
            });

            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            if (channelFuture.isSuccess()) {
                log.info("Server started successfully.");
            }
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}