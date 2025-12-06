package com.thermaflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async task execution with Virtual Threads.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Task executor optimized for Virtual Threads.
     * Used for I/O-heavy operations like PDF generation.
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("vt-async-");
        executor.setVirtualThreads(true); // Enable Virtual Threads
        executor.initialize();
        return executor;
    }
}
