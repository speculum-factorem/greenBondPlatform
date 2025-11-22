package com.esgbank.greenbond.core.config;

import com.esgbank.greenbond.core.util.MdcUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Async-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("Task rejected, thread pool is full and queue is also full");
            throw new org.springframework.core.task.TaskRejectedException("Task rejected, thread pool exhausted");
        });
        executor.initialize();
        return executor;
    }

    private static class MdcTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            Map<String, String> contextMap = MdcUtils.getCopyOfContextMap();
            return () -> {
                try {
                    MdcUtils.setRequestContext(contextMap);
                    runnable.run();
                } finally {
                    MdcUtils.clear();
                }
            };
        }
    }
}