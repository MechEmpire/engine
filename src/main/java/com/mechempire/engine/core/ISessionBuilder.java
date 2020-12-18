package com.mechempire.engine.core;

import io.netty.channel.Channel;

/**
 * package: com.mechempire.engine.network.session.builder
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/18 上午9:51
 * <p>
 * session 构造器接口
 */
public interface ISessionBuilder {

    /**
     * 构建实现了 ISession 接口的 session 对象
     *
     * @param channel netty channel
     * @return session 对象
     */
    ISession buildSession(Channel channel);
}