package com.mechempire.engine;

import com.mechempire.engine.runtime.MechEmpireEngine;

/**
 * package: com.mechempire.engine
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 上午11:07
 * <p>
 * 引擎入口
 */
public class EngineApplication {

    public static void main(String[] args) {
        MechEmpireEngine mechEmpireEngine = new MechEmpireEngine();
        mechEmpireEngine.run();
    }
}