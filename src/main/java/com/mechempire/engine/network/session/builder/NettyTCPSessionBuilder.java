package com.mechempire.engine.network.session.builder;

import com.mechempire.engine.core.ISession;
import com.mechempire.engine.core.ISessionBuilder;
import com.mechempire.engine.network.session.NettyTCPSession;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * package: com.mechempire.engine.network.session.builder
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/18 上午9:50
 */
public class NettyTCPSessionBuilder implements ISessionBuilder {

    private static final AttributeKey<Long> CHANNEL_SESSION_ID = AttributeKey.valueOf("channel_session_id");

    @Override
    public ISession buildSession(Channel channel) {
        NettyTCPSession nettySession = new NettyTCPSession(channel);
        channel.attr(CHANNEL_SESSION_ID).set(nettySession.getSessionId());
        return nettySession;
    }
}