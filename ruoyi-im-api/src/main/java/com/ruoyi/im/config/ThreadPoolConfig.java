package com.ruoyi.im.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 
 * 配置异步任务执行的线程池，优化系统性能
 * 
 * @author ruoyi
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolConfig.class);

    /**
     * 配置异步任务执行器
     * 
     * @return 线程池执行器
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        log.info("初始化异步任务线程池");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数
        executor.setMaxPoolSize(20);
        
        // 队列容量
        executor.setQueueCapacity(200);
        
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);
        
        // 线程名称前缀
        executor.setThreadNamePrefix("im-async-");
        
        // 拒绝策略：当线程池达到最大容量时的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 初始化
        executor.initialize();
        
        log.info("异步任务线程池初始化完成：核心线程数={}, 最大线程数={}, 队列容量={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * 配置消息推送执行器
     * 
     * @return 消息推送线程池执行器
     */
    @Bean("messagePushExecutor")
    public Executor messagePushExecutor() {
        log.info("初始化消息推送线程池");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(9);
        
        // 最大线程数
        executor.setMaxPoolSize(99);
        
        // 队列容量
        executor.setQueueCapacity(1000);
        
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(30);
        
        // 线程名称前缀
        executor.setThreadNamePrefix("im-message-push-");
        
        // 拒绝策略：丢弃最老的任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        
        // 初始化
        executor.initialize();
        
        log.info("消息推送线程池初始化完成：核心线程数={}, 最大线程数={}, 队列容量={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}