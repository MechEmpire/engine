package com.mechempire.engine.bean;

import com.mechempire.engine.runtime.EngineWorld;
import com.mechempire.engine.runtime.MechEmpireEngine;
import com.mechempire.engine.runtime.OneMechBattleControl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/16 下午3:55
 */
@Configuration
public class EngineBeans {
    @Bean
    public MechEmpireEngine mechEmpireEngine() {
        return new MechEmpireEngine();
    }

    @Bean
    public EngineWorld engineWorld() {
        return new EngineWorld();
    }

    @Bean
    public OneMechBattleControl oneMechBattleControl() {
        return new OneMechBattleControl();
    }
}