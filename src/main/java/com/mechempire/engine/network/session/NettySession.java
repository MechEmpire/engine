package com.mechempire.engine.network.session;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * package: com.mechempire.engine.server.session
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/18 上午8:55
 */
@Slf4j
@Data
public class NettySession implements ISession {

    /**
     * 会话 channel
     */
    protected volatile Channel channel;

    /**
     * player id
     */
    private long playerId;

    @Override
    public boolean isConnected() {
        if (null != channel) {
            return channel.isActive();
        }

        return false;
    }

    @Override
    public void close(boolean immediately) {
        if (null != channel) {
            channel.close();
        }
    }

    @Override
    public void write(byte[] message) throws Exception {
        if (null != channel) {
            channel.writeAndFlush(message);
        }
    }
}