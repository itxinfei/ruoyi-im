package com.ruoyi.im.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j配置
 * 配置熔断器、限流器等容错机制
 *
 * @author ruoyi
 */
@Configuration
public class Resilience4jConfig {

    /**
     * 配置全局熔断器
     */
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> CircuitBreakerConfig.custom()
                .slidingWindowSize(10)  // 滑动窗口大小
                .minimumNumberOfCalls(5) // 最小调用次数
                .failureRateThreshold(50) // 失败率阈值
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 熔断器开启后等待时间
                .permittedNumberOfCallsInHalfOpenState(3) // 半开状态下允许的调用次数
                .build());
    }

    /**
     * 配置全局超时限制
     */
    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10)) // 默认超时时间
                .build();
    }
}