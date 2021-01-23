package com.mechempire.engine.factory;

import com.mechempire.sdk.core.game.AbstractGameMapComponent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * package: com.mechempire.sdk.core.factory
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/16 上午11:09
 */
class GameMapComponentFactory {

    /**
     * component 计数器
     */
    private static final AtomicInteger componentCount = new AtomicInteger(0);

    /**
     * 创建游戏组件对象, 为每一个对象填充 id
     *
     * @param componentClazz 游戏组建对象类
     * @return 游戏组件对象
     */
    static <T extends AbstractGameMapComponent> T getComponent(Class<T> componentClazz) throws Exception {
        if (null == componentClazz) {
            return null;
        }
        T component = componentClazz.newInstance();
        component.setId(componentCount.incrementAndGet());
        return component;
    }
}