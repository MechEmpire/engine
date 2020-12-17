package com.mechempire.engine.server;

import com.mechempire.engine.core.IServer;
import com.mechempire.engine.server.handles.GameServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

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
public class MechEmpireServer implements IServer {

    @Override
    public void run() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress("localhost", 6666));

            log.info("server run on localhost:6666");

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new GameServerHandler());
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