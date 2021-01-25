package com.mechempire.engine.runtime.engine;

import com.mechempire.sdk.core.game.AbstractGameMapComponent;
import com.mechempire.sdk.core.game.AbstractWorld;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/15 下午4:29
 * <p>
 * 世界静态信息存储
 */
@Slf4j
public class EngineWorld extends AbstractWorld {

    @Getter
    @Setter
    private double windowWidth = 0;

    @Getter
    @Setter
    private double windowHeight = 0;

    /**
     * map 名称
     */
    @Getter
    private final String mapName = "map_v1.tmx";


    /**
     * 地图组件列表
     */
    @Getter
    private final Map<Integer, AbstractGameMapComponent> components = new HashMap<>(16);

    EngineWorld() {
//        try {
//            TMXMapReader mapReader = new TMXMapReader();
//            org.mapeditor.core.Map originMap = mapReader.readMap(getClass().getResource("/map/map_v1.tmx").toString());
//            MapLayer layer = null;
//
//            for (int i = 0; i < originMap.getLayerCount(); i++) {
//                layer = originMap.getLayer(i);
//
//                if (layer instanceof ObjectGroup) {
//                    List<MapObject> objectList = ((ObjectGroup) layer).getObjects();
//                    for (MapObject mapObject : objectList) {
//                        AbstractGameMapComponent gameMapComponent =
//                                GameMapComponentFactory.getComponent(mapObject.getType(), (short) 1);
//
//                        if (null == gameMapComponent) {
//                            continue;
//                        }
//
////                        gameMapComponent.setShape(mapObject.getShape());
////                        gameMapComponent.setName(mapObject.getName());
////                        gameMapComponent.setAffinity(Short.parseShort(mapObject.getProperties().getProperties().get(0).getValue()));
////                        gameMapComponent.setId(mapObject.getId());
////                        gameMapComponent.setLength(CoordinateUtil.coordinateYConvert(mapObject.getHeight()));
////                        gameMapComponent.setWidth(CoordinateUtil.coordinateXConvert(mapObject.getWidth()));
////                        gameMapComponent.setStartX(CoordinateUtil.coordinateXConvert(mapObject.getX()));
////                        gameMapComponent.setStartY(CoordinateUtil.coordinateYConvert(mapObject.getY()));
////                        gameMapComponent.setType(mapObject.getType());
//
////                        this.putComponent(1, gameMapComponent);
//
////                        gameMap.addMapComponent(gameMapComponent);
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("init world error: {}", e.getMessage(), e);
//        }
    }

    /**
     * 给 world 中添加组件
     * 不允许覆盖
     *
     * @param componentId 组件 ID
     * @param component   组件对象
     */
    void putComponent(int componentId, AbstractGameMapComponent component) {

        if (null == component || components.containsKey(componentId)) {
            return;
        }

        this.components.put(componentId, component);
    }

    /**
     * 获取组件对象
     *
     * @param componentId 组件 id
     * @return 组件对象
     */
    public AbstractGameMapComponent getComponent(int componentId) {
        return this.components.get(componentId);
    }
}