package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 鎬ц兘鐩戞帶宸ュ叿绫? * 
 * @author ruoyi
 */
public class PerformanceMonitor {
    
    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    /**
     * 寮€濮嬬洃鎺?     * @param operation 鎿嶄綔鍚嶇О
     * @return 鐩戞帶涓婁笅鏂?     */
    public static MonitorContext start(String operation) {
        return new MonitorContext(operation);
    }
    
    /**
     * 鐩戞帶涓婁笅鏂?     */
    public static class MonitorContext {
        private final String operation;
        private final long startTime;
        
        public MonitorContext(String operation) {
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
        }
        
        /**
         * 缁撴潫鐩戞帶骞惰褰曟棩蹇?         */
        public void end() {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Performance Monitor - Operation: {}, Duration: {}ms", operation, duration);
        }
        
        /**
         * 缁撴潫鐩戞帶骞惰褰曡缁嗕俊鎭?         * @param details 璇︾粏淇℃伅
         */
        public void end(String details) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Performance Monitor - Operation: {}, Duration: {}ms, Details: {}", operation, duration, details);
        }
    }
}
