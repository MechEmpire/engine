package com.mechempire.engine.runtime;

import com.mechempire.sdk.core.game.AbstractGameMapComponent;
import com.mechempire.sdk.core.game.AbstractWorld;
import lombok.Getter;

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
class EngineWorld extends AbstractWorld {

    /**
     * 地图组件列表
     */
    private final Map<Integer, AbstractGameMapComponent> components = new HashMap<>(16);

    /**
     * map 名称
     */
    @Getter
    private final String mapName = "map_v1.tmx";

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
    AbstractGameMapComponent getComponent(int componentId) {
        return this.components.get(componentId);
    }

    /**
     * 获取组件 map
     *
     * @return 组件 map
     */
    Map<Integer, AbstractGameMapComponent> getComponents() {
        return components;
    }
}