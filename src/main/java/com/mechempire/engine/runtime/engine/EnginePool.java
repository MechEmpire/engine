package com.mechempire.engine.runtime.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2021-01-24 19:51
 * <p>
 * 池化执行引擎，资源复用
 */
@Lazy
@Slf4j
@Component
public class EnginePool {
    /**
     * 引擎数
     */
    private final int engineCount = 1;

    /**
     * 空闲引擎存活时间，默认为 20s
     */
    private final long keepAliveTime = 20 * 1000;

    /**
     * 获取空闲引擎时，最大等待时常，默认为 60s
     */
    private final long maxWaitTime = 60 * 1000;

    /**
     * 引擎池
     */
    private Engine[] pool;

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

    @Resource
    private ExecutorService threadPool;

    /**
     * 初始化函数
     */
//    public EnginePool() {
//        this.pool = new Engine[this.engineCount];
//        this.workQueue = new LinkedBlockingQueue<>();
//        this.idleQueue = new LinkedBlockingDeque<>();
//        this.freezeQueue = new LinkedBlockingQueue<>();
//        try {
//            for (int i = 0; i < this.engineCount; i++) {
//                Engine engine = new Engine(threadPool);
//                engine.init();
//                this.pool[i] = engine;
//                this.idleQueue.add(engine);
//            }
//        } catch (Exception e) {
//            log.error("init engine pool error: {}", e.getMessage(), e);
//        }
//    }

    /**
     * init
     */
    @PostConstruct
    public void init() {
        this.pool = new Engine[this.engineCount];
        this.workQueue = new LinkedBlockingQueue<>();
        this.idleQueue = new LinkedBlockingDeque<>();
        this.freezeQueue = new LinkedBlockingQueue<>();
        try {
            for (int i = 0; i < this.engineCount; i++) {
                Engine engine = new Engine(threadPool);
                engine.init();
                this.pool[i] = engine;
                this.idleQueue.add(engine);
            }
        } catch (Exception e) {
            log.error("init engine pool error: {}", e.getMessage(), e);
        }
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
                if (Objects.nonNull(engine)) {
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
     * 释放引擎, 进入空闲状态
     *
     * @param engine 引擎
     */
    public void releaseEngine(Engine engine) {
        try {
            engine.switchIdle();
            engine.close();
            idleQueue.add(engine);
            workQueue.remove(engine);
        } catch (Exception e) {
            log.error("release engine error: {}", e.getMessage(), e);
        }
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
