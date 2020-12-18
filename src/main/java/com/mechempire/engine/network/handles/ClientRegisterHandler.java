package com.mechempire.engine.network.handles;

import com.mechempire.engine.network.MechEmpireServer;
import com.mechempire.engine.util.NetworkUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * package: com.mechempire.engine.server.handles
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/17 下午10:30
 */
@Slf4j
public class ClientRegisterHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MechEmpireServer.map.put(NetworkUtil.getIPString(ctx), ctx.channel());
        log.info("client {} has been registered!", NetworkUtil.getRemoteAddress(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        MechEmpireServer.map.remove(NetworkUtil.getIPString(ctx));
        log.info("client {} has disconnected!", NetworkUtil.getRemoteAddress(ctx));
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.error("client register error: {}", cause.getMessage(), cause);
    }
}