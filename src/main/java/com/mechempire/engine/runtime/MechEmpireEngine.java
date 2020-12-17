package com.mechempire.engine.runtime;

import com.mechempire.engine.core.IBattleControl;
import com.mechempire.engine.core.IEngine;
import com.mechempire.sdk.constant.RuntimeConstant;
import com.mechempire.sdk.core.game.AbstractGameMapComponent;
import com.mechempire.sdk.core.game.AbstractMech;
import com.mechempire.sdk.core.game.AbstractTeam;
import com.mechempire.sdk.core.game.IMechControlFlow;
import com.mechempire.sdk.core.message.AbstractMessage;
import com.mechempire.sdk.core.message.IConsumer;
import com.mechempire.sdk.core.message.IProducer;
import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.LocalCommandMessageProducer;
import com.mechempire.sdk.util.ClassCastUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
     * 指令消息队列消费者
     */
    private final IConsumer commandMessageConsumer = new LocalCommandMessageConsumer();

    /**
     * 指令消息队列
     */
    private final BlockingQueue<AbstractMessage> commandMessageQueue = new LinkedBlockingQueue<>(20);

    /**
     * 对战计算控制对象
     */
    @Resource
    private IBattleControl battleControl;

    /**
     * 线程屏障
     */
    private final CyclicBarrier barrier = new CyclicBarrier(4);

    /**
     * 世界, 对战运行时数据记录
     */
    @Resource
    private EngineWorld engineWorld;

    /**
     * 线程池
     */
    private final ExecutorService threadPool = new ThreadPoolExecutor(3, 3,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(4)
    );

    /**
     * 引擎启动方法
     */
    @Override
    public void run() {
        try {
            injectProducerAndTeam("agent_red.jar", redCommandMessageProducer);
            injectProducerAndTeam("agent_blue.jar", blueCommandMessageProducer);
            commandMessageConsumer.setQueue(commandMessageQueue);
            executeConsumerThread(commandMessageConsumer);
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
     * @throws Exception 异常
     */
    private void injectProducerAndTeam(String agentName, IProducer commandMessageProducer) throws Exception {
        commandMessageProducer.setQueue(commandMessageQueue);
        AbstractTeam team = TeamFactory.newTeam(agentName);

        for (AbstractGameMapComponent component : team.getMechList()) {
            AbstractMech mech = ClassCastUtil.cast(component);
            engineWorld.putComponent(mech.getId(), mech);
            engineWorld.putComponent(mech.getAmmunition().getId(), mech.getAmmunition());
            engineWorld.putComponent(mech.getVehicle().getId(), mech.getVehicle());
            engineWorld.putComponent(mech.getWeapon().getId(), mech.getWeapon());
        }

        IMechControlFlow controlFlow = MechControlFactory.getTeamControl(agentName);
        threadPool.execute(() -> {
            try {
                barrier.await();
                controlFlow.run(commandMessageProducer, team);
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });
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
                List<CommandMessage> messagesPerFrame = new ArrayList<>(40);
                long startTime = System.currentTimeMillis();
                
                while (true) {
                    CommandMessage commandMessage = (CommandMessage) commandMessageConsumer.consume();
                    if (null != commandMessage) {
                        messagesPerFrame.add(commandMessage);
                    }

                    // each frame
                    if (0 == (System.currentTimeMillis() - startTime) % RuntimeConstant.FRAME_GAP) {
                        battleControl.battle(messagesPerFrame);
                        messagesPerFrame.clear();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}