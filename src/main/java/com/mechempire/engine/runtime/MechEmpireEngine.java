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
    private IProducer redCommandMessageProducer;

    /**
     * 蓝方指令消息队列生产者
     */
    private IProducer blueCommandMessageProducer;

    /**
     * 红方指令消息队列消费者
     */
    private IConsumer redCommandMessageConsumer;

    /**
     * 蓝方指令消息队列消费者
     */
    private IConsumer blueCommandMessageConsumer;

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

    @Override
    public void init() {
        redCommandMessageProducer = new LocalCommandMessageProducer();
        BlockingQueue<AbstractMessage> redQueue = new LinkedBlockingQueue<>(20);
        redCommandMessageProducer.setQueue(redQueue);

        blueCommandMessageProducer = new LocalCommandMessageProducer();
        BlockingQueue<AbstractMessage> blueQueue = new LinkedBlockingQueue<>(20);
        blueCommandMessageProducer.setQueue(blueQueue);

        redCommandMessageConsumer = new LocalCommandMessageConsumer();
        redCommandMessageConsumer.setQueue(redQueue);

        blueCommandMessageConsumer = new LocalCommandMessageConsumer();
        blueCommandMessageConsumer.setQueue(blueQueue);
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
            AbstractTeam redTeam = TeamFactory.newTeam("agent_red.jar");
            AbstractTeam blueTeam = TeamFactory.newTeam("agent_blue.jar");
            IMechControlFlow redControlFlow = MechControlFactory.getTeamControl("agent_red.jar");
            IMechControlFlow blueControlFlow = MechControlFactory.getTeamControl("agent_blue.jar");

            threadPool.execute(() -> {
                try {
                    barrier.await();
                    redControlFlow.run(redCommandMessageProducer, redTeam);
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });

            threadPool.execute(() -> {
                try {
                    barrier.await();
                    blueControlFlow.run(blueCommandMessageProducer, blueTeam);
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });

            executeConsumerThread(redCommandMessageConsumer);
            executeConsumerThread(blueCommandMessageConsumer);
            barrier.await();

        } catch (Exception e) {
            e.printStackTrace();
        }
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