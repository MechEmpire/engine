package com.mechempire.engine.runtime.engine;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2021-01-23 15:24
 * <p>
 * 对战引擎管理器
 */
public class EngineManager {

    /**
     * 存储对战引擎
     */
    private static final Map<Integer, Engine> engineTable = Maps.newConcurrentMap();

    /**
     * 添加引擎
     *
     * @param empireEngine 引擎对象
     */
    public static void addEngine(Engine empireEngine) {

        if (null == empireEngine) {
            return;
        }

        engineTable.put(empireEngine.getId(), empireEngine);
    }

    /**
     * 获取引擎对象
     *
     * @param id id
     * @return 引擎对象
     */
    public static Engine getEngine(Integer id) {
        return engineTable.get(id);
    }

    /**
     * 移除 engine
     *
     * @param id id
     */
    public static void removeEngine(Integer id) {
        engineTable.remove(id);
    }
}
