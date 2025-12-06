package com.thermaflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async task execution.
 * 
 * Note: Virtual Threads are available in Java 21+. On Java 17, this uses
 * a standard ThreadPoolTaskExecutor optimized for I/O operations.
 * 
 * To enable Virtual Threads on Java 21+:
 * - Uncomment executor.setVirtualThreads(true)
 * - Set spring.threads.virtual.enabled=true in application.yml
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Task executor optimized for I/O-heavy operations like PDF generation.
     * On Java 21+, this can be configured to use Virtual Threads.
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        
        // Uncomment the following line when using Java 21+
        // executor.setVirtualThreads(true);
        
        executor.initialize();
        return executor;
    }
}
