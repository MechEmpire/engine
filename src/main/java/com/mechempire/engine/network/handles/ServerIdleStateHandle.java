package com.mechempire.engine.network.handles;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * package: com.mechempire.engine.network.handles
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/18 下午5:17
 */
@Slf4j
public class ServerIdleStateHandle extends ChannelInboundHandlerAdapter {

    /**
     * 指定时间内未收到客户端发送的读事件
     * 关闭长时间闲置的连接
     *
     * @param ctx ctx
     * @param evt evt
     * @throws Exception 异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                final SocketAddress socketAddress = ctx.channel().remoteAddress();
                ctx.channel().close().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("close idle connect: {} for {} done", socketAddress, event.state());
                    } else {
                        log.info("close idle connect: {} for {} fail", socketAddress, event.state());
                    }
                });
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}