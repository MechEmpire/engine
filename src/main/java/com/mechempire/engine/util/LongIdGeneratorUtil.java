package com.mechempire.engine.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * package: com.mechempire.engine.util
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/18 上午10:04
 * <p>
 * long 型 id 生成器
 */
public class LongIdGeneratorUtil {

    /**
     * 原子计数器
     */
    private static AtomicLong idGen = new AtomicLong(0);

    public static long generateId() {
        return idGen.incrementAndGet();
    }
}