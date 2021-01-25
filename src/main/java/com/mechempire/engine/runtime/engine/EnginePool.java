package com.mechempire.engine.runtime.engine;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2021-01-24 19:51
 * <p>
 * 执行引擎池
 * <p>
 * 池化执行引擎，资源复用
 */
@Slf4j
public class EnginePool {

    private int corePoolSize;

    private int maxPoolSize;

    /**
     * 空闲引擎存活时间，默认为 20s
     */
    private long keepAliveTime = 20 * 1000;

    /**
     * 获取空闲引擎时，最大等待时常，默认为 60s
     */
    private long maxWaitTime = 60 * 1000;

    private Engine[] pools;

    /**
     * 工作池，存放正在运行的引擎
     */
    private LinkedBlockingQueue<Engine> workQueue;

    /**
     * 空闲池，存放空闲的引擎
     */
    private LinkedBlockingDeque<Engine> idleQueue;

    /**
     * 回收池，已经被回收的引擎
     */
    private LinkedBlockingQueue<Engine> freezeQueue;

    /**
     * 构造函数
     *
     * @param corePoolSize 核心引擎数
     * @param maxPoolSize  最大引擎数
     */
    public EnginePool(int corePoolSize, int maxPoolSize) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.pools = new Engine[this.corePoolSize];
        this.workQueue = new LinkedBlockingQueue<>();
        this.idleQueue = new LinkedBlockingDeque<>();
        this.freezeQueue = new LinkedBlockingQueue<>();
    }

    /**
     * 获取一个空闲的执行引擎
     *
     * @return 引擎对象
     */
    public Engine getIdleEngine() {
        try {
            long waitTime = maxWaitTime;

            while (waitTime > 0) {
                long beginPollNanoTime = System.nanoTime();
                Engine engine = idleQueue.poll(waitTime, TimeUnit.MILLISECONDS);
                if (null != engine) {
                    if (engine.isClosed() && engine.switchOccupied() && working(engine)) {
                        return engine;
                    } else {
                        engine.switchIdle();
                        idleQueue.addLast(engine);
                    }
                }

                long timeConsuming = (System.nanoTime() - beginPollNanoTime) / (1000 * 1000);
                waitTime -= timeConsuming;
            }
        } catch (Exception e) {
            log.error("getIdleEngine error: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * 加入工作队列
     *
     * @param engine 引擎
     * @return 加入结果
     */
    private boolean working(Engine engine) {
        return workQueue.add(engine);
    }

    /**
     * 加入回收队列
     *
     * @param engine 引擎
     * @return 加入结果
     */
    private boolean freeze(Engine engine) {
        return idleQueue.remove(engine) && freezeQueue.add(engine);
    }
}
