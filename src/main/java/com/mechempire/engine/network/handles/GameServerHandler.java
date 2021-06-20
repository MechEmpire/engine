package com.mechempire.engine.network.handles;

import com.google.protobuf.Any;
import com.mechempire.engine.network.session.NettyTCPSession;
import com.mechempire.engine.network.session.SessionManager;
import com.mechempire.engine.network.session.builder.NettyTCPSessionBuilder;
import com.mechempire.engine.runtime.engine.Engine;
import com.mechempire.engine.runtime.engine.EnginePool;
import com.mechempire.engine.runtime.engine.EngineWorld;
import com.mechempire.sdk.core.game.AbstractGameMap;
import com.mechempire.sdk.proto.CommonDataProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

import static com.mechempire.sdk.util.SafeSetUtil.safeSet;

/**
 * package: com.mechempire.engine.server.handles
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/17 下午4:01
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class GameServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * session builder
     */
    @Resource
    private NettyTCPSessionBuilder nettyTCPSessionBuilder;

    /**
     * engine pool
     */
    @Resource
    private EnginePool enginePool;

    /**
     * data builder
     */
    private final CommonDataProto.CommonData.Builder builder = CommonDataProto.CommonData.newBuilder();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel_active, channel_id: {}", ctx.channel().id());
        ctx.flush();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CommonDataProto.CommonData req = (CommonDataProto.CommonData) msg;
        log.info("server receiver: " + req.getMessage());
        NettyTCPSession session;
        builder.clear();

        switch (req.getCommand()) {
            case PING:
                builder.setCommand(CommonDataProto.CommonData.CommandEnum.PING);
                builder.setMessage("pong");
                break;
            case INIT:
                // todo 修改成从引擎池获取
                Engine engine = enginePool.getIdleEngine();
                log.info("engine: {}", engine);
                engine.setAgentRedName("agent_blue.jar");
                engine.setAgentBlueName("agent_blue.jar");
                engine.recycle();

                CommonDataProto.InitRequest initRequest =
                        req.getData().unpack(CommonDataProto.InitRequest.class);
                // 地图大小
                double windowSize = initRequest.getScreenHeight() >= 900 ? 1280 : 640;
                EngineWorld engineWorld = engine.getEngineWorld();
                engineWorld.setWindowWidth(windowSize);
                engineWorld.setWindowLength(windowSize);

                // 设置 session
                session = (NettyTCPSession) nettyTCPSessionBuilder.buildSession(ctx.channel());
                SessionManager.addSession(ctx.channel().id(), session);
                engine.addWatchSession(session);
                session.setEngine(engine);

                // 回执消息
                CommonDataProto.EngineWorld.Builder engineWorldBuilder = CommonDataProto.EngineWorld.newBuilder();
                safeSet(engineWorld.getWindowLength(), engineWorldBuilder::setWindowLength);
                safeSet(engineWorld.getWindowWidth(), engineWorldBuilder::setWindowWidth);
                // set gameMap
                CommonDataProto.GameMap.Builder gameMapBuilder = CommonDataProto.GameMap.newBuilder();
                AbstractGameMap gameMap = engineWorld.getGameMap();
                safeSet(gameMap.getId(), gameMapBuilder::setId);
                safeSet(gameMap.getWidth(), gameMapBuilder::setWidth);
                safeSet(gameMap.getLength(), gameMapBuilder::setLength);
                safeSet(gameMap.getGridLength(), gameMapBuilder::setGridLength);
                safeSet(gameMap.getGridWidth(), gameMapBuilder::setGridWidth);
                safeSet(gameMap.getName(), gameMapBuilder::setMapName);

                // 填充 imageElement
                gameMap.getImageElementList().forEach(mapImageElement -> {
                    CommonDataProto.GameMap.ImageElement.Builder imageElementBuilder =
                            CommonDataProto.GameMap.ImageElement.newBuilder();
                    safeSet(mapImageElement.getSource(), imageElementBuilder::setSource);
                    safeSet(mapImageElement.getWidth(), imageElementBuilder::setWidth);
                    safeSet(mapImageElement.getHeight(), imageElementBuilder::setHeight);
                    safeSet(mapImageElement.getOpacity(), imageElementBuilder::setOpacity);
                    safeSet(mapImageElement.getOffsetX(), imageElementBuilder::setOffsetX);
                    safeSet(mapImageElement.getOffsetY(), imageElementBuilder::setOffsetY);
                    safeSet(CommonDataProto.GameMap.ImageElement.ElementType.valueOf(mapImageElement.getImageType().name()),
                            imageElementBuilder::setImageType);
                    gameMapBuilder.addMapImageElement(imageElementBuilder.build());
                });

                // 填充 components
                gameMap.getComponents().forEach((key, component) -> {
                    CommonDataProto.GameMap.MapComponent.Builder mapComponentBuilder =
                            CommonDataProto.GameMap.MapComponent.newBuilder();
                    safeSet(component.getId(), mapComponentBuilder::setId);
                    safeSet(component.getName(), mapComponentBuilder::setName);
                    safeSet(component.getMapComponent().name(), mapComponentBuilder::setType);
                    safeSet(component.getAffinity(), mapComponentBuilder::setAffinity);
                    safeSet(component.getStartX(), mapComponentBuilder::setStartX);
                    safeSet(component.getStartY(), mapComponentBuilder::setStartY);
                    safeSet(component.getLength(), mapComponentBuilder::setLength);
                    safeSet(component.getWidth(), mapComponentBuilder::setWidth);
                    // todo shape 这里还需要再完善
                    if (component.getShape() instanceof Rectangle) {
                        mapComponentBuilder.setShape(CommonDataProto.GameMap.MapComponent.ComponentShape.RECTANGLE2D);
                    } else if (component.getShape() instanceof Ellipse) {
                        mapComponentBuilder.setShape(CommonDataProto.GameMap.MapComponent.ComponentShape.ELLIPSE2D);
                    }
                    if (Objects.nonNull(component.getPosition())) {
                        safeSet(CommonDataProto.Position2D.newBuilder()
                                .setX(component.getPosition().getX())
                                .setY(component.getPosition().getY())
                                .build(), mapComponentBuilder::setPosition
                        );
                    }
                    gameMapBuilder.putComponents(key, mapComponentBuilder.build());
                });
                engineWorldBuilder.setGameMap(gameMapBuilder.build());
                builder.setData(Any.pack(engineWorldBuilder.build()));
                builder.setCommand(CommonDataProto.CommonData.CommandEnum.INIT);
                builder.setMessage("init");
                break;
            case START:
                session = (NettyTCPSession) SessionManager.getSession(ctx.channel().id());
                session.getEngine().run();
                log.info("send started.");
                builder.setCommand(CommonDataProto.CommonData.CommandEnum.START);
                builder.setMessage("started");
                break;
            default:
                builder.setMessage(req.getMessage());
                break;
        }

        ctx.writeAndFlush(builder.build());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        closeAndClear(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause instanceof IOException)) {
            log.error("game server error: {}", cause.getMessage(), cause);
        }
        closeAndClear(ctx);
    }

    /**
     * close channel
     *
     * @param channel channel
     */
    private void closeOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.flush();
            channel.close();
        }
    }

    /**
     * 清除 session
     *
     * @param ctx ctx
     * @throws Exception 异常
     */
    private void closeAndClear(ChannelHandlerContext ctx) throws Exception {
        NettyTCPSession session = (NettyTCPSession) SessionManager.getSession(ctx.channel().id());

        if (Objects.nonNull(session)) {
            enginePool.releaseEngine(session.getEngine());
            SessionManager.removeBySessionId(ctx.channel().id());
        }

        ctx.flush();
        ctx.close();
    }
}