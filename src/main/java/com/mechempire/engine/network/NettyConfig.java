package com.mechempire.engine.network;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * package: com.mechempire.engine.network
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/19 下午7:11
 */
public class NettyConfig {

    /**
     * 客户端
     */
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}