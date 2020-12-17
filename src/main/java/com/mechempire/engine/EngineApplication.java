package com.mechempire.engine;

import com.mechempire.engine.bean.EngineBeans;
import com.mechempire.engine.bean.ServerBeans;
import com.mechempire.engine.runtime.MechEmpireEngine;
import com.mechempire.engine.server.MechEmpireServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * package: com.mechempire.engine
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 上午11:07
 * <p>
 * 引擎入口
 */
@Slf4j
public class EngineApplication {

    /**
     * 入口
     *
     * @param args 参数
     */
    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(EngineBeans.class);
        ctx.register(ServerBeans.class);
        ctx.refresh();

        Thread engineThread = new Thread(() -> {
            MechEmpireEngine mechEmpireEngine = ctx.getBean(MechEmpireEngine.class);
            mechEmpireEngine.run();

            log.info("mechempire engine is running...");
        });
        engineThread.start();

        Thread serverThread = new Thread(() -> {
            try {
                MechEmpireServer mechEmpireServer = ctx.getBean(MechEmpireServer.class);
                mechEmpireServer.run();

                log.info("mechempire game server is running...");
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        serverThread.start();
        ctx.close();
    }
}