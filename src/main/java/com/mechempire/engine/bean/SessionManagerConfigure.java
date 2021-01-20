package com.mechempire.engine.bean;

import com.mechempire.engine.network.session.SessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * package: com.mechempire.engine.bean
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2021-01-20 16:57
 */
@Configuration
public class SessionManagerConfigure {

    @Bean
    public SessionManager sessionManager() {
        return SessionManager.getInstance();
    }
}
