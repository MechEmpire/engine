package com.mechempire.engine.util;

import io.netty.channel.ChannelHandlerContext;

/**
 * package: com.mechempire.engine.util
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/17 下午10:33
 */
public class NetworkUtil {

    /**
     * 获取客户端 IP 地址
     *
     * @param ctx 请求上下文
     * @return IP 地址
     */
    public static String getRemoteAddress(ChannelHandlerContext ctx) {
        return ctx.channel().remoteAddress().toString();
    }

    /**
     * 获取 IP 字符串表达
     *
     * @param ctx 请求上下文
     * @return 字符串
     */
    public static String getIPString(ChannelHandlerContext ctx) {
        String socketString = ctx.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        return socketString.substring(1, colonAt);
    }
}