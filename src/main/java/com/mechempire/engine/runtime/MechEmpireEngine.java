package com.mechempire.engine.runtime;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mechempire.engine.core.IEngine;
import com.mechempire.sdk.core.game.IMechControlFlow;
import com.mechempire.sdk.core.message.AbstractMessage;
import com.mechempire.sdk.core.message.IConsumer;
import com.mechempire.sdk.core.message.IProducer;
import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.LocalCommandMessageProducer;

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

    /**
     * 指令消息队列生产者
     */
    private IProducer commandMessageProducer;

    /**
     * 指令消息队列消费者
     */
    private IConsumer commandMessageConsumer;

    @Override
    public void init() {
        commandMessageProducer = new LocalCommandMessageProducer();
        BlockingQueue<AbstractMessage> queue = new LinkedBlockingQueue<>(20);

        commandMessageProducer.setQueue(queue);
        commandMessageConsumer = new LocalCommandMessageConsumer();
        commandMessageConsumer.setQueue(queue);
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
            CyclicBarrier barrier = new CyclicBarrier(3);
            ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("agent-thread-%d").build();
            ExecutorService threadPool = new ThreadPoolExecutor(2, 2,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(2),
                    threadFactory
            );

            IMechControlFlow redAgentMain = AgentLoader.getAgentObject("agent_red.jar");
            threadPool.execute(() -> {
                try {
                    barrier.await();
                    redAgentMain.run(commandMessageProducer);
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });

            IMechControlFlow blueAgentMain = AgentLoader.getAgentObject("agent_blue.jar");
            threadPool.execute(() -> {
                try {
                    barrier.await();
                    blueAgentMain.run(commandMessageProducer);
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
            barrier.await();
//            CommandMessage commandMessage = new CommandMessage();
//            CommandProduct commandProduct = new CommandProduct();
//            commandProduct.product(commandMessage);

            Thread consumeThread = new Thread(() -> {
                while (true) {
                    CommandMessage commandMessage = (CommandMessage) commandMessageConsumer.consume();
                    if (null != commandMessage) {
                        System.out.println(commandMessage.getTeamId());
                    }
                }
            });
            consumeThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}