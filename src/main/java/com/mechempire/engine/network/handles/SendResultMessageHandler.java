package com.mechempire.engine.network.handles;

import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * package: com.mechempire.engine.server.handles
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/17 下午10:54
 */
@Slf4j
public class SendResultMessageHandler extends ChannelInboundHandlerAdapter {

//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        log.info("send result message: {}", msg);
//        ctx.write(msg);
//    }
}