package com.mechempire.engine.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * package: com.mechempire.engine.config
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2021/4/3 下午6:28
 */
@Configuration
public class ThreadPoolConfig {
    @Bean
    public ExecutorService threadPool() {
        return new ThreadPoolExecutor(
                5, 5, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(5),
                new ThreadFactoryBuilder().setNameFormat("mechempire-engine-thread-%d").build()
        );
    }
}