package com.mechempire.engine.factory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * package: com.mechempire.engine.factory
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2021-01-23 15:32
 */
public class EngineIdFactory {

    /**
     * id 计数器
     */
    private final static AtomicInteger engineId = new AtomicInteger(0);

    /**
     * 获取一个引擎 id
     *
     * @return id
     */
    public static int getId() {
        return engineId.incrementAndGet();
    }
}
