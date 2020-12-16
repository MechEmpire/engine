package com.mechempire.engine.runtime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/16 下午3:55
 */
@Configuration
public class RuntimeBeans {
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