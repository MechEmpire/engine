package com.mechempire.engine.network.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * package: com.mechempire.engine.network
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2021-01-20 12:38
 */
public class SessionManager {

    private static volatile SessionManager instance = null;

    private Map<Long, NettySession> sessionIdMap;

    private SessionManager() {
        this.sessionIdMap = new ConcurrentHashMap<>();
    }

    public static SessionManager getInstance() {
        if (null == instance) {
            synchronized (SessionManager.class) {
                if (null == instance) {
                    instance = new SessionManager();
                }
            }
        }

        return instance;
    }

    public NettySession findBySessionId(Long id) {
        return sessionIdMap.get(id);
    }

    public void put(Long id, NettySession session) {
        sessionIdMap.put(id, session);
    }

    public void removeBySessionId(Long id) {
        sessionIdMap.remove(id);
    }
}
