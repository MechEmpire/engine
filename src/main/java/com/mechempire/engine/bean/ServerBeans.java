package com.mechempire.engine.bean;

import com.mechempire.engine.network.MechEmpireServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * package: com.mechempire.engine.bean
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/17 下午4:05
 */
@Configuration
public class ServerBeans {
    @Bean
    public MechEmpireServer mechEmpireServer() {
        return new MechEmpireServer();
    }
}