package com.mechempire.engine.runtime.engine;

import com.google.common.collect.Lists;
import com.google.protobuf.Any;
import com.mechempire.engine.core.IEngine;
import com.mechempire.engine.factory.EngineIdFactory;
import com.mechempire.engine.network.session.NettySession;
import com.mechempire.engine.runtime.AgentLoader;
import com.mechempire.engine.runtime.CommandMessageReader;
import com.mechempire.engine.runtime.LocalCommandMessageConsumer;
import com.mechempire.engine.runtime.OneMechBattleControl;
import com.mechempire.engine.util.UnsafeUtil;
import com.mechempire.sdk.constant.*;
import com.mechempire.sdk.core.factory.PositionFactory;
import com.mechempire.sdk.core.game.*;
import com.mechempire.sdk.core.message.AbstractMessage;
import com.mechempire.sdk.core.message.IConsumer;
import com.mechempire.sdk.core.message.IProducer;
import com.mechempire.sdk.proto.CommonDataProto;
import com.mechempire.sdk.runtime.AgentWorld;
import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.LocalCommandMessageProducer;
import com.mechempire.sdk.util.ClassCastUtil;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static com.mechempire.sdk.core.factory.GameMapComponentFactory.createComponent;

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
    @Setter
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
    private static final Unsafe UNSAFE = UnsafeUtil.unsafe;

    /**
     * statusOffset
     */
    private static final long STATUS_OFFSET = UnsafeUtil.getFieldOffset(Engine.class, "status");

    /**
     * 世界, 对战运行时数据记录
     */
    @Getter
    private EngineWorld engineWorld;

    /**
     * agent world
     */
    @Setter
    private AgentWorld agentWorld;

    /**
     * 对战计算控制对象
     * todo 这里需要想办法改成接口
     */
    private OneMechBattleControl battleControl;

    /**
     * 监听 sessions
     */
    private final List<NettySession> watchSessions = Lists.newLinkedList();

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
     * <p>
     * todo 需要做成公共线程池
     */
    private ExecutorService threadPool;

    public Engine(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    public void init() throws Exception {
        id = EngineIdFactory.getId();
        status = EngineStatus.CLOSED;
        // 红方指令消费队列
        redCommandMessageProducer = new LocalCommandMessageProducer();
        // 蓝方指令消费队列
        blueCommandMessageProducer = new LocalCommandMessageProducer();
        battleControl = new OneMechBattleControl();
        engineWorld = new EngineWorld();
        agentWorld = new AgentWorld();
    }

    @Override
    public void recycle() throws Exception {
        barrier.reset();
        redCommandMessageProducer.reset();
        blueCommandMessageProducer.reset();

        // todo 状态修改需要再设计一下
        engineWorld.setEngineStatus(EngineStatus.CREATED);
        agentWorld.setEngineStatus(EngineStatus.CREATED);

        engineWorld.loadGameMap();

        injectProducerAndTeam(agentRedName, redCommandMessageProducer, TeamAffinity.RED);
        injectProducerAndTeam(agentBlueName, blueCommandMessageProducer, TeamAffinity.BLUE);
        battleControl.setEngineWorld(engineWorld);
        battleControl.setCommandMessageReader(new CommandMessageReader());
    }

    @Override
    public void run() throws Exception {
        status = EngineStatus.OCCUPIED;
        engineWorld.setEngineStatus(status);
        agentWorld.setEngineStatus(status);
        try {
            commandMessageConsumer.setQueue(commandMessageQueue);
            executeConsumerThread(commandMessageConsumer);
            barrier.await();
        } catch (Exception e) {
            log.error("engine run error: {}", e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        log.info("engine: {} has been closed.", id);
        status = EngineStatus.CLOSED;
        engineWorld.setEngineStatus(EngineStatus.CLOSED);
        agentWorld.setEngineStatus(EngineStatus.CLOSED);
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
        return UNSAFE.compareAndSwapObject(this, STATUS_OFFSET, status, EngineStatus.IDLE) && flushIdleStartTime();
    }

    @Override
    public boolean switchOccupied() {
        return UNSAFE.compareAndSwapObject(this, STATUS_OFFSET, status, EngineStatus.OCCUPIED) && flushUsageCount();
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
     * @param teamAffinity           队伍属性, 红 or 蓝
     */
    private void injectProducerAndTeam(String agentName, IProducer commandMessageProducer, TeamAffinity teamAffinity) {
        commandMessageProducer.setQueue(commandMessageQueue);
        AbstractTeam team = newTeam(agentName, teamAffinity);

        if (Objects.isNull(team)) {
            log.error("{} newTeam get null.", agentName);
            return;
        }

        // init component
        for (AbstractGameMapComponent component : team.getMechList()) {
            AbstractMech mech = ClassCastUtil.cast(component);
            engineWorld.putComponent(mech.getId(), mech);
            engineWorld.putComponent(mech.getAmmunition().getId(), mech.getAmmunition());
            engineWorld.putComponent(mech.getVehicle().getId(), mech.getVehicle());
            engineWorld.putComponent(mech.getWeapon().getId(), mech.getWeapon());
        }

        IMechControlFlow controlFlow = this.getTeamControl(agentName);

        if (Objects.isNull(controlFlow)) {
            log.error("{} control flow is null.", agentName);
            return;
        }

        threadPool.submit(() -> {
            try {
                barrier.await();
                MechRunResult result = MechRunResult.SUCCESS;
                while (!engineWorld.getEngineStatus().equals(EngineStatus.CLOSED) && !result.equals(MechRunResult.FAILED)) {
                    result = controlFlow.run(commandMessageProducer, team, agentWorld);
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                log.error("run control flow error: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 创建队伍
     *
     * @param agentName    jar 包名称
     * @param teamAffinity 队伍属性, 红 or 蓝
     * @return 队伍对象
     */
    private AbstractTeam newTeam(String agentName, TeamAffinity teamAffinity) {
        try {
            URLClassLoader classLoader = AgentLoader.getAgentClassLoader(agentName);
            Class<AbstractTeam> agentTeam = ClassCastUtil.cast(classLoader.loadClass(AGENT_TEAM_CLASS));
            AbstractTeam team = agentTeam.newInstance();
            List<AbstractMech> mechList = Lists.newArrayList();
            for (Class<?> clazz : team.getMechClassList()) {
                AbstractMech mech = newMech(ClassCastUtil.cast(clazz), teamAffinity);
                mech.setTeam(team);
                mechList.add(mech);
            }
            team.setTeamAffinity(teamAffinity);
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
    private <M extends AbstractMech> M newMech(Class<M> mechClazz, TeamAffinity teamAffinity) throws Exception {
        M mech = createComponent(mechClazz);
        assert mech != null;
        mech.setMapComponent(MapComponent.DEFAULT_MECH);
        mech.getMapComponent().setClazz(mechClazz);
        mech.setAffinity(teamAffinity.getCode());

        // set start point of mech
        if (teamAffinity.equals(TeamAffinity.RED)) {
            mech.setStartX(80.0);
            mech.setStartY(1135.0);
        } else {
            mech.setStartX(1135.0);
            mech.setStartY(80.0);
        }

        // 装配载具, 设置所属机甲,大小
        AbstractVehicle vehicle = createComponent(ClassCastUtil.cast(mech.getVehicleComponent().getClazz()));
        assert vehicle != null;
        vehicle.setStartX(mech.getStartX());
        vehicle.setStartY(mech.getStartY());
        vehicle.setMapComponent(mech.getVehicleComponent());
        vehicle.setMech(mech);
        vehicle.setShape(new Rectangle(vehicle.getStartX(), vehicle.getStartY(), vehicle.getWidth(), vehicle.getLength()));
        vehicle.setAffinity(teamAffinity.getCode());
        mech.setWidth(vehicle.getWidth());
        mech.setLength(vehicle.getLength());
        mech.setVehicle(vehicle);

        // 装配武器, 设置所属机甲
        AbstractWeapon weapon = createComponent(ClassCastUtil.cast(mech.getWeaponComponent().getClazz()));
        assert weapon != null;
        weapon.setMech(mech);
        weapon.setStartX(mech.getStartX());
        weapon.setStartY(mech.getStartY());
        weapon.setMapComponent(mech.getWeaponComponent());
        weapon.setShape(new Rectangle(weapon.getStartX(), weapon.getStartY(), weapon.getWidth(), weapon.getLength()));
        weapon.setAffinity(teamAffinity.getCode());
        mech.setWeapon(weapon);

        // 装配弹药, 设置所属机甲
        AbstractAmmunition ammunition = createComponent(ClassCastUtil.cast(mech.getAmmunitionComponent().getClazz()));
        assert ammunition != null;
        ammunition.setMech(mech);
        ammunition.setStartX(mech.getStartX());
        ammunition.setStartY(mech.getStartY());
        ammunition.setMapComponent(mech.getAmmunitionComponent());
        ammunition.setShape(new Rectangle(ammunition.getStartX(), ammunition.getStartY(), ammunition.getWidth(), ammunition.getLength()));
        ammunition.setAffinity(teamAffinity.getCode());
        mech.setAmmunition(ammunition);

        // 更新初始位置信息
        AbstractPosition position = PositionFactory.getPosition(mech);
        mech.updatePosition(position);
        mech.setShape(new Rectangle(mech.getStartX(), mech.getStartY(), mech.getWidth(), mech.getLength()));
        return mech;
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
        threadPool.submit(() -> {
            try {
                barrier.await();
                List<CommandMessage> messagesPerFrame = Lists.newArrayListWithCapacity(40);
                long startTime = System.currentTimeMillis();

                CommonDataProto.ResultMessageList.ResultMessage.Builder resultMessageBuilder =
                        CommonDataProto.ResultMessageList.ResultMessage.newBuilder();

                CommonDataProto.ResultMessageList.Builder resultMessageListBuilder =
                        CommonDataProto.ResultMessageList.newBuilder();

                CommonDataProto.CommonData.Builder commonDataBuilder = CommonDataProto.CommonData.newBuilder();

                // todo 修改为定时器
                while (this.status == EngineStatus.OCCUPIED) {
                    CommandMessage commandMessage = (CommandMessage) commandMessageConsumer.consume();
                    if (Objects.nonNull(commandMessage) && Objects.nonNull(commandMessage.getByteSeq()) && commandMessage.getByteSeq().length > 0) {
                        messagesPerFrame.add(commandMessage);
                    }

                    long now = System.currentTimeMillis();
                    // each frame
                    if (0 != (now - startTime) % RuntimeConstant.FRAME_GAP) {
                        continue;
                    }

                    if (Objects.isNull(this.battleControl)) {
                        continue;
                    }

                    this.battleControl.battle(messagesPerFrame);

                    resultMessageListBuilder.clear();
                    if (Objects.isNull(this.engineWorld)) {
                        continue;
                    }

                    // 填充位置变更的组件
                    this.engineWorld.getGameMap().getComponents().forEach((k, v) -> {
                        AbstractPosition position = v.getPosition();
                        if (Objects.isNull(position)) {
                            return;
                        }
                        resultMessageBuilder.clear();
                        resultMessageBuilder
                                .setComponentId(v.getId())
                                .setPositionX(position.getX())
                                .setPositionY(position.getY());
                        resultMessageListBuilder.addResultMessage(resultMessageBuilder.build());
                    });

                    commonDataBuilder.clear();
                    commonDataBuilder.setData(Any.pack(resultMessageListBuilder.build()));
                    commonDataBuilder.setMessage("running");
                    commonDataBuilder.setCommand(CommonDataProto.CommonData.CommandEnum.RUNNING);

                    watchSessions.forEach((s) -> s.getChannel().writeAndFlush(commonDataBuilder.build()));
                    messagesPerFrame.clear();
                }
            } catch (Exception e) {
                log.error("execute consumer thread error: {}", e.getMessage(), e);
            }
        });
    }
}