package com.ruoyi.im;

import com.ruoyi.im.util.ImRedisUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@MapperScan("com.ruoyi.im.mapper")
public class ImApplication {
    private static final Logger log = LoggerFactory.getLogger(ImApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class, args);
    }

    @Configuration
    public static class RedisCacheCleanRunner implements CommandLineRunner {

        private final ImRedisUtil imRedisUtil;

        public RedisCacheCleanRunner(ImRedisUtil imRedisUtil) {
            this.imRedisUtil = imRedisUtil;
        }

        @Override
        public void run(String... args) {
            log.info("启动时清除会话列表缓存...");
            try {
                imRedisUtil.deletePattern("conversation:list:*");
                log.info("会话列表缓存已清除");
            } catch (Exception e) {
                log.warn("清除会话列表缓存失败: {}", e.getMessage());
            }
        }
    }
}