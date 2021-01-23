package com.mechempire.engine.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private static Map<Integer, MechEmpireEngine> engineTable = new ConcurrentHashMap<>(16);

    /**
     * 添加引擎
     *
     * @param empireEngine 引擎对象
     */
    public static void addEngine(MechEmpireEngine empireEngine) {

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
    public static MechEmpireEngine getEngine(Integer id) {
        return engineTable.get(id);
    }
}
