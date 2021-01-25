package com.mechempire.engine.runtime.engine;

import com.google.protobuf.Any;
import com.mechempire.engine.constant.EngineStatus;
import com.mechempire.engine.core.IBattleControl;
import com.mechempire.engine.core.IEngine;
import com.mechempire.engine.factory.EngineIdFactory;
import com.mechempire.engine.network.session.NettySession;
import com.mechempire.engine.runtime.AgentLoader;
import com.mechempire.engine.runtime.CommandMessageReader;
import com.mechempire.engine.runtime.LocalCommandMessageConsumer;
import com.mechempire.engine.runtime.OneMechBattleControl;
import com.mechempire.engine.util.UnsafeUtil;
import com.mechempire.sdk.constant.RuntimeConstant;
import com.mechempire.sdk.core.factory.PositionFactory;
import com.mechempire.sdk.core.game.*;
import com.mechempire.sdk.core.message.AbstractMessage;
import com.mechempire.sdk.core.message.IConsumer;
import com.mechempire.sdk.core.message.IProducer;
import com.mechempire.sdk.proto.CommonDataProto;
import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.LocalCommandMessageProducer;
import com.mechempire.sdk.util.ClassCastUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 上午11:24
 * <p>
 * 机甲帝国对战引擎
 */
@Slf4j
public class Engine implements IEngine {

    /**
     * 引擎 id
     */
    @Getter
    private int id = 0;

    /**
     * 引擎状态
     */
    @Getter
    private EngineStatus status;

    private long idleStartTime;

    private long usageCount;

    /**
     * 红方 agent 名称
     */
    @Setter
    private String agentRedName;

    /**
     * 蓝方 agent 名称
     */
    @Setter
    private String agentBlueName;

    /**
     * 组件数
     */
    private Integer componentCount = 0;

    /**
     * 红方消息生产者
     */
    private IProducer redCommandMessageProducer;

    /**
     * 蓝方消息生产者
     */
    private IProducer blueCommandMessageProducer;

    /**
     * 指令消息队列消费者
     */
    private final IConsumer commandMessageConsumer = new LocalCommandMessageConsumer();

    /**
     * 指令消息队列
     */
    private final BlockingQueue<AbstractMessage> commandMessageQueue = new LinkedBlockingQueue<>(20);

    /**
     * 线程屏障, 保障所有游戏线程同时启动
     */
    private final CyclicBarrier barrier = new CyclicBarrier(4);

    /**
     * unsafe, 用于修改引擎状态值
     */
    private static Unsafe unsafe = UnsafeUtil.unsafe;

    /**
     * statusOffset
     */
    private static long statusOffset = UnsafeUtil.getFieldOffset(Engine.class, "status");

    /**
     * 世界, 对战运行时数据记录
     */
    @Getter
    private EngineWorld engineWorld;

    /**
     * 对战计算控制对象
     */
    private IBattleControl battleControl;

    /**
     * 监听 sessions
     */
    private List<NettySession> watchSessions = new LinkedList<>();

    /**
     * 用户 jar 包中的 team 类名
     */
    private static final String AGENT_TEAM_CLASS = "com.mechempire.agent.Team";

    /**
     * agent 主控制类
     */
    private static final String AGENT_MAIN_CLASS = "com.mechempire.agent.AgentMain";

    /**
     * 线程池
     */
    private final ExecutorService threadPool =
            new ThreadPoolExecutor(
                    3, 3, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(4)
            );

    @Override
    public void init() throws Exception {
        this.id = EngineIdFactory.getId();
        this.status = EngineStatus.CREATED;
        redCommandMessageProducer = new LocalCommandMessageProducer();
        blueCommandMessageProducer = new LocalCommandMessageProducer();
        engineWorld = new EngineWorld();
        injectProducerAndTeam(agentRedName, redCommandMessageProducer);
        injectProducerAndTeam(agentBlueName, blueCommandMessageProducer);
        battleControl = new OneMechBattleControl(engineWorld, new CommandMessageReader());
    }

    @Override
    public void run() throws Exception {
        this.status = EngineStatus.OCCUPIED;
        new Thread(() -> {
            try {
                commandMessageConsumer.setQueue(commandMessageQueue);
                executeConsumerThread(commandMessageConsumer);
                barrier.await();
            } catch (Exception e) {
                log.error("engine run error: {}", e.getMessage(), e);
            }
        }).start();
    }

    @Override
    public void close() throws Exception {
        engineWorld = null;
        redCommandMessageProducer = null;
        blueCommandMessageProducer = null;
        battleControl = null;
        this.status = EngineStatus.CLOSED;
    }

    @Override
    public boolean isClosed() {
        return this.status == EngineStatus.CLOSED;
    }

    @Override
    public boolean isIdle() {
        return this.status == EngineStatus.IDLE;
    }

    @Override
    public boolean isOccupied() {
        return this.status == EngineStatus.OCCUPIED;
    }

    @Override
    public boolean switchIdle() {
        return unsafe.compareAndSwapObject(this, statusOffset, status, EngineStatus.IDLE) && flushIdleStartTime();
    }

    @Override
    public boolean switchOccupied() {
        return unsafe.compareAndSwapObject(this, statusOffset, status, EngineStatus.OCCUPIED) && flushUsageCount();
    }

    public boolean flushIdleStartTime() {
        idleStartTime = System.currentTimeMillis();
        return true;
    }

    public boolean flushUsageCount() {
        usageCount += 1;
        return true;
    }

    /**
     * 添加监听 session
     *
     * @param session session
     */
    public void addWatchSession(NettySession session) {
        this.watchSessions.add(session);
    }

    /**
     * 向 agent 注入 team 信息和消息生产者
     *
     * @param agentName              agent 名称
     * @param commandMessageProducer 消息生产者
     */
    private void injectProducerAndTeam(String agentName, IProducer commandMessageProducer) {
        commandMessageProducer.setQueue(commandMessageQueue);
        AbstractTeam team = this.newTeam(agentName);
        assert null != team;
        for (AbstractGameMapComponent component : team.getMechList()) {
            AbstractMech mech = ClassCastUtil.cast(component);
            engineWorld.putComponent(mech.getId(), mech);
            engineWorld.putComponent(mech.getAmmunition().getId(), mech.getAmmunition());
            engineWorld.putComponent(mech.getVehicle().getId(), mech.getVehicle());
            engineWorld.putComponent(mech.getWeapon().getId(), mech.getWeapon());
        }

        IMechControlFlow controlFlow = this.getTeamControl(agentName);
        assert null != controlFlow;
        threadPool.execute(() -> {
            try {
                barrier.await();
                controlFlow.run(commandMessageProducer, team);
            } catch (InterruptedException | BrokenBarrierException e) {
                log.error("run control flow error: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 创建队伍
     *
     * @param agentName jar 包名称
     * @return 队伍对象
     */
    private AbstractTeam newTeam(String agentName) {
        try {
            URLClassLoader classLoader = AgentLoader.getAgentClassLoader(agentName);
            Class<AbstractTeam> agentTeam = ClassCastUtil.cast(classLoader.loadClass(AGENT_TEAM_CLASS));
            AbstractTeam team = agentTeam.newInstance();
            List<AbstractMech> mechList = new ArrayList<>(4);
            for (Class<?> clazz : team.getMechClassList()) {
                AbstractMech mech = this.newMech(ClassCastUtil.cast(clazz));
                mech.setTeam(team);
                mechList.add(mech);
            }
            team.setMechList(mechList);
            return team;
        } catch (Exception e) {
            log.error("new team exception: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * 创建机甲
     *
     * @param mechClazz 机甲类
     * @param <M>       类
     * @return 新机甲对象
     * @throws Exception 异常
     */
    private <M extends AbstractMech> M newMech(Class<M> mechClazz) throws Exception {
        M mech = this.getComponent(mechClazz);
        mech.setStartY(64.0);
        mech.setStartX(64.0);

        // 装配载具, 设置所属机甲,大小
        AbstractVehicle vehicle = this.getComponent(ClassCastUtil.cast(mech.getVehicleClazz()));
        vehicle.setStartY(mech.getStartX());
        vehicle.setStartY(mech.getStartY());
        vehicle.setMech(mech);
        mech.setWidth(vehicle.getWidth());
        mech.setLength(vehicle.getLength());
        mech.setVehicle(vehicle);

        // 装配武器, 设置所属机甲
        AbstractWeapon weapon = this.getComponent(ClassCastUtil.cast(mech.getWeaponClazz()));
        weapon.setMech(mech);
        mech.setWeapon(weapon);

        // 装配弹药, 设置所属机甲
        AbstractAmmunition ammunition = this.getComponent(ClassCastUtil.cast(mech.getAmmunitionClazz()));
        ammunition.setMech(mech);
        mech.setAmmunition(ammunition);

        // 更新初始位置信息
        AbstractPosition position = PositionFactory.getPosition(mech);
        mech.updatePosition(position);
        return mech;
    }

    /**
     * 生成地图组件
     *
     * @param componentClazz 组件类
     * @param <T>            类
     * @return 新组件
     * @throws Exception 异常
     */
    private <T extends AbstractGameMapComponent> T getComponent(Class<T> componentClazz) throws Exception {
        if (null == componentClazz) {
            return null;
        }
        T component = componentClazz.newInstance();
        component.setId(componentCount++);
        return component;
    }

    /**
     * 获取机甲主控制流类
     *
     * @param agentName jar 名称
     * @return 机甲控制流
     */
    private IMechControlFlow getTeamControl(String agentName) {
        try {
            URLClassLoader classLoader = AgentLoader.getAgentClassLoader(agentName);
            Class<?> agentTeam = classLoader.loadClass(AGENT_MAIN_CLASS);
            return (IMechControlFlow) agentTeam.newInstance();
        } catch (Exception e) {
            log.error("getTeamControl error: {}", e.getMessage(), e);
        }

        return null;
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
                int frameCount = 0;

                while (frameCount <= 500000 && this.status == EngineStatus.OCCUPIED) {
                    CommandMessage commandMessage = (CommandMessage) commandMessageConsumer.consume();
                    if (null != commandMessage) {
                        messagesPerFrame.add(commandMessage);
                    }

                    // each frame
                    if (0 != (System.currentTimeMillis() - startTime) % RuntimeConstant.FRAME_GAP) {
                        continue;
                    }
                    battleControl.battle(messagesPerFrame);

                    CommonDataProto.ResultMessageList.Builder resultMessages =
                            CommonDataProto.ResultMessageList.newBuilder();

                    engineWorld.getComponents().forEach((k, v) -> {
                        AbstractPosition position = v.getPosition();
                        CommonDataProto.ResultMessage.Builder builder =
                                CommonDataProto.ResultMessage.newBuilder();
                        builder.setComponentId(v.getId())
                                .setPositionX(position.getX())
                                .setPositionY(position.getY());
                        resultMessages.addResultMessage(builder.build());
                    });

                    CommonDataProto.CommonData.Builder commonDataBuilder =
                            CommonDataProto.CommonData.newBuilder();

                    commonDataBuilder.setData(Any.pack(resultMessages.build()));
                    commonDataBuilder.setMessage("battle_result_message");

                    watchSessions.forEach((s) -> {
                        s.getChannel().writeAndFlush(commonDataBuilder.build());
                        commonDataBuilder.clear();
                    });

                    messagesPerFrame.clear();
                    frameCount++;
                }
            } catch (Exception e) {
                log.error("execute consumer thread error: {}", e.getMessage(), e);
            }
        });
    }
}