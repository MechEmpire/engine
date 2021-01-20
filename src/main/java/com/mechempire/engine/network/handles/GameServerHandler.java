package com.mechempire.engine.network.handles;

import com.mechempire.engine.network.NettyConfig;
import com.mechempire.engine.network.session.NettyTCPSession;
import com.mechempire.engine.network.session.SessionManager;
import com.mechempire.engine.network.session.builder.NettyTCPSessionBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * package: com.mechempire.engine.server.handles
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/17 下午4:01
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class GameServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * client session
     */
    private NettyTCPSession nettyTCPSession;

    @Resource
    private SessionManager sessionManager;

    /**
     * session builder
     */
    @Resource
    private NettyTCPSessionBuilder nettyTCPSessionBuilder;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        nettyTCPSession = (NettyTCPSession) nettyTCPSessionBuilder.buildSession(ctx.channel());
        sessionManager.put(nettyTCPSession.getSessionId(), nettyTCPSession);
        log.info("channel_active, channel_id: {}, session_id: {}", ctx.channel().id(), nettyTCPSession.getSessionId());
        NettyConfig.channelGroup.add(ctx.channel());
        ctx.flush();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf inBuffer = (ByteBuf) msg;
        String received = inBuffer.toString(CharsetUtil.UTF_8);
        log.info("server received: " + received);
        if (received.equals("ping")) {
            ctx.write(Unpooled.copiedBuffer("pong", CharsetUtil.UTF_8));
        } else {
            ctx.write(Unpooled.copiedBuffer(received, CharsetUtil.UTF_8));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        NettyConfig.channelGroup.remove(ctx.channel());
        if (null != nettyTCPSession) {
            ctx.flush();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("game server error: {}", cause.getMessage(), cause);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (!(evt instanceof IdleStateEvent)) {
//            return;
//        }
//
//        IdleStateEvent e = (IdleStateEvent) evt;
//        if (e.state() == IdleState.READER_IDLE) {
//            ctx.flush();
//            ctx.close();
//            log.info("disconnection due to inbound traffic.");
//        }
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