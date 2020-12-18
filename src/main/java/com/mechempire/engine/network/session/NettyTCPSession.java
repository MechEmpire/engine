package com.mechempire.engine.network.session;

import com.mechempire.engine.util.LongIdGeneratorUtil;
import io.netty.channel.Channel;
import lombok.Getter;

/**
 * package: com.mechempire.engine.network.session
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/18 上午9:47
 * <p>
 * Netty tcp session
 */
public class NettyTCPSession extends NettySession {

    @Getter
    private final long sessionId;

    public NettyTCPSession(Channel channel) {
        super(channel);
        this.sessionId = LongIdGeneratorUtil.generateId();
    }
}