package com.mechempire.engine.network.session;

import io.netty.channel.ChannelId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * package: com.mechempire.engine.network
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2021-01-20 12:38
 */
public class SessionManager {

    private static Map<ChannelId, NettySession> sessionIdMap = new ConcurrentHashMap<>(16);

    /**
     * 获取会话对象
     *
     * @param id id
     * @return 会话对象
     */
    public static NettySession getSession(ChannelId id) {
        return sessionIdMap.get(id);
    }

    /**
     * 添加 session
     *
     * @param id      id
     * @param session session
     */
    public static void addSession(ChannelId id, NettySession session) {
        if (null == session) {
            return;
        }
        sessionIdMap.put(id, session);
    }

    /**
     * 移除 session
     *
     * @param id id
     */
    public static void removeBySessionId(ChannelId id) {
        sessionIdMap.remove(id);
    }
}
