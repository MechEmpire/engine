package com.mechempire.engine;

import com.mechempire.engine.runtime.MechEmpireEngine;
import com.mechempire.engine.runtime.RuntimeBeans;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(RuntimeBeans.class);
        ctx.refresh();

        MechEmpireEngine mechEmpireEngine = ctx.getBean(MechEmpireEngine.class);
        mechEmpireEngine.run();

        ctx.close();
    }
}