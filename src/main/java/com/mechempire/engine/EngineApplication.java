package com.mechempire.engine;

import com.mechempire.engine.network.MechEmpireServer;
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
        ctx.scan("com.mechempire");
        ctx.refresh();

        new Thread(() -> {
            try {
                ctx.getBean(MechEmpireServer.class).run();
                log.info("game server is running ...");
            } catch (Exception e) {
                log.error("game server run error: {}", e.getMessage(), e);
            }
        }).start();
        ctx.close();
    }
}