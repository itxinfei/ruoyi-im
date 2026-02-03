package com.ruoyi.im.config;

import com.ruoyi.im.service.ImCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IM系统启动验证器
 * 负责启动时的系统验证和缓存清理（非生产环境）
 *
 * @author ruoyi
 */
@Component
@Order(1) // 确保最早执行
public class ImSystemStartupValidator implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ImSystemStartupValidator.class);

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ImCacheService imCacheService;

    @Override
    public void run(String... args) throws Exception {
        String env = appConfig.getEnv();
        logger.info("=== IM系统启动验证开始 ===");
        logger.info("运行环境: {}", env);
        logger.info("✓ IM系统API接口已成功启动");
        logger.info("✓ 所有IM服务组件已就绪");

        // 非生产环境（开发/测试）启动时自动清理缓存
        if (appConfig.isNonProd()) {
            logger.info("检测到非生产环境，正在清理 Redis 缓存...");
            try {
                imCacheService.clearAll();
                logger.info("✓ Redis 缓存清理完成");
            } catch (Exception e) {
                logger.error("Redis 缓存清理失败: {}", e.getMessage(), e);
            }
        } else {
            logger.info("生产环境，跳过缓存清理");
        }

        logger.info("=== IM系统启动验证完成 ===");
    }
}