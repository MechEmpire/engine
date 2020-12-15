package com.mechempire.engine.runtime;

import com.mechempire.engine.core.IEngine;
import com.mechempire.sdk.core.game.AbstractTeam;
import com.mechempire.sdk.core.game.IMechControlFlow;
import com.mechempire.sdk.core.message.AbstractMessage;
import com.mechempire.sdk.core.message.IConsumer;
import com.mechempire.sdk.core.message.IProducer;
import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.LocalCommandMessageProducer;

import java.util.Arrays;
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
     * 红方指令消息队列生产者
     */
    private final IProducer redCommandMessageProducer = new LocalCommandMessageProducer();

    /**
     * 蓝方指令消息队列生产者
     */
    private final IProducer blueCommandMessageProducer = new LocalCommandMessageProducer();

    /**
     * 红方指令消息队列消费者
     */
    private final IConsumer redCommandMessageConsumer = new LocalCommandMessageConsumer();

    /**
     * 蓝方指令消息队列消费者
     */
    private final IConsumer blueCommandMessageConsumer = new LocalCommandMessageConsumer();

    /**
     * 线程屏障
     */
    private final CyclicBarrier barrier = new CyclicBarrier(5);

    /**
     * 线程池
     */
    private final ExecutorService threadPool = new ThreadPoolExecutor(4, 4,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(4)
    );

    /**
     * 引擎启动方法
     */
    @Override
    public void run() {
        try {
            injectProducerAndTeam("agent_red.jar", redCommandMessageProducer, redCommandMessageConsumer);
            injectProducerAndTeam("agent_blue.jar", blueCommandMessageProducer, blueCommandMessageConsumer);
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向 agent 注入 team 信息和消息生产者
     *
     * @param agentName              agent 名称
     * @param commandMessageProducer 消息生产者
     * @param commandMessageConsumer 消息消费者
     * @throws Exception 异常
     */
    private void injectProducerAndTeam(String agentName, IProducer commandMessageProducer, IConsumer commandMessageConsumer) throws Exception {
        BlockingQueue<AbstractMessage> redQueue = new LinkedBlockingQueue<>(20);
        commandMessageProducer.setQueue(redQueue);
        commandMessageConsumer.setQueue(redQueue);
        AbstractTeam team = TeamFactory.newTeam(agentName);
        IMechControlFlow controlFlow = MechControlFactory.getTeamControl(agentName);

        threadPool.execute(() -> {
            try {
                barrier.await();
                controlFlow.run(commandMessageProducer, team);
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });
        executeConsumerThread(commandMessageConsumer);
    }

    /**
     * 启动命令消费线程
     *
     * @param commandMessageConsumer 消费者
     */
    private void executeConsumerThread(IConsumer commandMessageConsumer) {
        threadPool.execute(() -> {
            try {
                barrier.await();
                while (true) {
                    CommandMessage commandMessage = (CommandMessage) commandMessageConsumer.consume();
                    if (null != commandMessage) {
                        System.out.printf("%s, team_id: %d \n", Thread.currentThread().getName(), commandMessage.getTeamId());
                        byte[] command = commandMessage.getCommandSeq();
                        System.out.println(Arrays.toString(command));
                    }
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });
    }
}