package com.mechempire.engine.network.handles;

import com.mechempire.engine.network.session.NettyTCPSession;
import com.mechempire.engine.network.session.builder.NettyTCPSessionBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * package: com.mechempire.engine.server.handles
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/17 下午4:01
 */
@Slf4j
public class GameServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 客户端会话
     */
    private NettyTCPSession nettyTCPSession;

    @Resource
    private NettyTCPSessionBuilder tcpSessionBuilder;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        nettyTCPSession = (NettyTCPSession) tcpSessionBuilder.buildSession(ctx.channel());
        
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (null != nettyTCPSession) {
            closeOnFlush(nettyTCPSession.getChannel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf inBuffer = (ByteBuf) msg;
        String received = inBuffer.toString(CharsetUtil.UTF_8);
        log.info("Server received: " + received);
        ctx.write(Unpooled.copiedBuffer("Hello " + received, CharsetUtil.UTF_8));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        closeOnFlush(ctx.channel());
        log.error("game server error: {}", cause.getMessage(), cause);
    }

    /**
     * close channel
     *
     * @param channel channel
     */
    private void closeOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.flush();
            channel.close();
        }
    }
}