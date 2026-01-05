package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 性能监控工具类
 * 
 * @author ruoyi
 */
public class PerformanceMonitor {
    
    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    /**
     * 开始监控
     * @param operation 操作名称
     * @return 监控上下文
     */
    public static MonitorContext start(String operation) {
        return new MonitorContext(operation);
    }
    
    /**
     * 监控上下文
     */
    public static class MonitorContext {
        private final String operation;
        private final long startTime;
        
        public MonitorContext(String operation) {
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
        }
        
        /**
         * 结束监控并记录日志
         */
        public void end() {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Performance Monitor - Operation: {}, Duration: {}ms", operation, duration);
        }
        
        /**
         * 结束监控并记录详细信息
         * @param details 详细信息
         */
        public void end(String details) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Performance Monitor - Operation: {}, Duration: {}ms, Details: {}", operation, duration, details);
        }
    }
}