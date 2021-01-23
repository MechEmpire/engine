package com.mechempire.engine.network.handles;

import com.mechempire.engine.network.session.NettyTCPSession;
import com.mechempire.engine.network.session.SessionManager;
import com.mechempire.engine.network.session.builder.NettyTCPSessionBuilder;
import com.mechempire.engine.runtime.EngineManager;
import com.mechempire.engine.runtime.MechEmpireEngine;
import com.mechempire.sdk.proto.ResultMessageProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

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
     * session builder
     */
    @Resource
    private NettyTCPSessionBuilder nettyTCPSessionBuilder;

    private ResultMessageProto.CommonData.Builder builder = ResultMessageProto.CommonData.newBuilder();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel_active, channel_id: {}", ctx.channel().id());
        ctx.flush();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ResultMessageProto.CommonData req = (ResultMessageProto.CommonData) msg;
        log.info("server receiver: " + req.getMessage());
        builder = ResultMessageProto.CommonData.newBuilder();
        NettyTCPSession session = null;

        switch (req.getMessage()) {
            case "ping":
                builder.setMessage("pong");
                break;
            case "init":
                MechEmpireEngine engine = new MechEmpireEngine("agent_red.jar", "agent_blue.jar");
                session = (NettyTCPSession) nettyTCPSessionBuilder.buildSession(ctx.channel());
                SessionManager.addSession(ctx.channel().id(), session);
                engine.addWatchSession(session);
                session.setEngine(engine);
                EngineManager.addEngine(engine);
                builder.setMessage("init");
                break;
            case "start":
                session = (NettyTCPSession) SessionManager.getSession(ctx.channel().id());
                session.getEngine().run();
                log.info("session_status: {}", session.getEngine().getStatus());
                builder.setMessage("started");
                break;
            default:
                builder.setMessage(req.getMessage());
                break;
        }

        ctx.writeAndFlush(builder.build());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        closeAndClear(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause instanceof IOException)) {
            log.error("game server error: {}", cause.getMessage(), cause);
        }
        closeAndClear(ctx);
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

    /**
     * 清除 session
     *
     * @param ctx ctx
     * @throws Exception 异常
     */
    private void closeAndClear(ChannelHandlerContext ctx) throws Exception {
        NettyTCPSession session = (NettyTCPSession) SessionManager.getSession(ctx.channel().id());

        if (null != session) {
            session.getEngine().close();
            EngineManager.removeEngine(session.getEngine().getId());
            SessionManager.removeBySessionId(ctx.channel().id());
        }

        ctx.flush();
        ctx.close();
    }
}