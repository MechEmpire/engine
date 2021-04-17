package com.mechempire.engine.runtime.engine;

import com.mechempire.sdk.constant.MapComponentConstant;
import com.mechempire.sdk.core.component.DefaultBaseCamp;
import com.mechempire.sdk.core.component.DefaultObstacle;
import com.mechempire.sdk.core.component.DefaultRoad;
import com.mechempire.sdk.core.game.AbstractGameMapComponent;
import com.mechempire.sdk.core.game.AbstractWorld;
import com.mechempire.sdk.runtime.GameMap;
import javafx.scene.shape.Ellipse;
import lombok.extern.slf4j.Slf4j;
import org.mapeditor.core.MapLayer;
import org.mapeditor.core.MapObject;
import org.mapeditor.core.ObjectGroup;
import org.mapeditor.io.TMXMapReader;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static com.mechempire.sdk.core.factory.GameMapComponentFactory.createComponent;
import static java.lang.Short.parseShort;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/15 下午4:29
 * <p>
 * 世界静态信息存储
 * 与客户端信息保持一致
 */
@Slf4j
public class EngineWorld extends AbstractWorld {

    EngineWorld() {
        gameMap = new GameMap();
        gameMap.setName("map_v1.tmx");
        try {
            TMXMapReader mapReader = new TMXMapReader();
            URL url = getClass().getResource("/map/" + gameMap.getName());
            if (Objects.isNull(url)) {
                return;
            }
            org.mapeditor.core.Map originMap = mapReader.readMap(url.toString());
            MapLayer layer;

            for (int i = 0; i < originMap.getLayerCount(); i++) {
                layer = originMap.getLayer(i);
                if (!(layer instanceof ObjectGroup)) {
                    continue;
                }
                List<MapObject> objectList = ((ObjectGroup) layer).getObjects();
                for (MapObject mapObject : objectList) {
                    AbstractGameMapComponent gameMapComponent = null;
                    if (MapComponentConstant.COMPONENT_BASECAMP.getName().equalsIgnoreCase(mapObject.getType())) {
                        gameMapComponent = createComponent(DefaultBaseCamp.class);
                        gameMapComponent.setType(MapComponentConstant.COMPONENT_BASECAMP);
                    } else if (MapComponentConstant.COMPONENT_OBSTACLE.getName().equalsIgnoreCase(mapObject.getType())) {
                        gameMapComponent = createComponent(DefaultObstacle.class);
                        gameMapComponent.setType(MapComponentConstant.COMPONENT_OBSTACLE);
                    } else if (MapComponentConstant.COMPONENT_ROAD.getName().equalsIgnoreCase(mapObject.getType())) {
                        gameMapComponent = createComponent(DefaultRoad.class);
                        gameMapComponent.setType(MapComponentConstant.COMPONENT_ROAD);
                    }

                    if (Objects.isNull(gameMapComponent)) {
                        continue;
                    }

                    if (mapObject.getShape() instanceof Rectangle2D) {
                        Rectangle2D originShape = (Rectangle2D) mapObject.getShape();
                        gameMapComponent.setShape(new javafx.scene.shape.Rectangle(originShape.getX(),
                                originShape.getY(), originShape.getWidth(), originShape.getHeight()));
                    } else if (mapObject.getShape() instanceof Ellipse2D) {
                        Ellipse2D originShape = (Ellipse2D) mapObject.getShape();
                        gameMapComponent.setShape(new Ellipse(originShape.getX(), originShape.getY(),
                                originShape.getWidth(), originShape.getHeight()));
                    }
                    gameMapComponent.setName(mapObject.getName());
                    gameMapComponent.setAffinity(parseShort(mapObject.getProperties().getProperties().get(0).getValue()));
                    gameMapComponent.setId(mapObject.getId());
                    gameMapComponent.setLength(mapObject.getHeight());
                    gameMapComponent.setWidth(mapObject.getWidth());
                    gameMapComponent.setStartX(mapObject.getX());
                    gameMapComponent.setStartY(mapObject.getY());
                    putComponent(gameMapComponent.getId(), gameMapComponent);
                }
            }
        } catch (Exception e) {
            log.error("init world error: {}", e.getMessage(), e);
        }
    }

    /**
     * 给 world 中添加组件
     * 不允许覆盖
     *
     * @param componentId 组件 ID
     * @param component   组件对象
     */
    void putComponent(int componentId, AbstractGameMapComponent component) {
        if (Objects.isNull(component) || gameMap.getComponents().containsKey(componentId)) {
            return;
        }

        gameMap.getComponents().put(componentId, component);
    }

    /**
     * 获取组件对象
     *
     * @param componentId 组件 id
     * @return 组件对象
     */
    public AbstractGameMapComponent getComponent(int componentId) {
        return gameMap.getComponents().get(componentId);
    }
}