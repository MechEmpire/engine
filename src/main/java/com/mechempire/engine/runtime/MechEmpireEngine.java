package com.mechempire.engine.runtime;

import com.mechempire.engine.core.IEngine;
import com.mechempire.sdk.core.game.IMechControlFlow;

import java.util.concurrent.*;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 上午11:24
 * <p>
 * 机甲帝国引擎
 */
public class MechEmpireEngine implements IEngine {

    @Override
    public void init() {

    }

    /**
     * 引擎启动方法
     * <p>
     * 1. load agent.jar
     * 2. create two agent thread
     * 3. print CommandMessage
     * 4. send to MessageBus
     */
    @Override
    public void run() {

        try {
            CyclicBarrier barrier = new CyclicBarrier(2);
            ExecutorService threadPool = new ThreadPoolExecutor(2, 2,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(2));
            IMechControlFlow redAgentMain = AgentLoader.getAgentObject("agent_red.jar");
            threadPool.execute(redAgentMain);
            IMechControlFlow blueAgentMain = AgentLoader.getAgentObject("agent_blue.jar");
            threadPool.execute(blueAgentMain);
            barrier.await();
//            CommandMessage commandMessage = new CommandMessage();
//            CommandProduct commandProduct = new CommandProduct();
//            commandProduct.product(commandMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}