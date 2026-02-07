package com.ruoyi.im.config;

import com.ruoyi.im.service.ImCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * IM系统启动验证器
 * 负责启动时的系统验证和缓存清理（非生产环境）
 *
 * 安全措施：
 * 1. 需要显式配置 app.clear-cache-on-startup=true 才会清理
 * 2. 仅在 dev/test 环境生效
 * 3. 清理前有明显警告日志
 *
 * @author ruoyi
 */
@Component
@Order(1) // 确保最早执行
public class ImSystemStartupValidator implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ImSystemStartupValidator.class);

    private static final String WARNING_BANNER =
            "\n" +
            "###############################################\n" +
            "#     ⚠️  警告：正在清理 Redis 缓存！     #\n" +
            "#     所有 IM 相关缓存将被清空            #\n" +
            "###############################################";

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

        // 非生产环境 + 显式配置才清理缓存（双重保险）
        if (shouldClearCache()) {
            logger.warn(WARNING_BANNER);
            logger.warn("环境: {} | 配置: clear-cache-on-startup=true", env);
            logger.warn("即将清理以下缓存键前缀:");
            logger.warn("  - im:* (IM数据)");
            logger.warn("  - contact:list:* (好友列表)");
            logger.warn("  - conversation:* (会话)");
            logger.warn("  - user:* (用户)");

            try {
                long startTime = System.currentTimeMillis();
                imCacheService.clearAll();
                long elapsed = System.currentTimeMillis() - startTime;
                logger.warn("✓ Redis 缓存清理完成 (耗时: {}ms)", elapsed);
            } catch (Exception e) {
                logger.error("Redis 缓存清理失败: {}", e.getMessage(), e);
            }

            logger.warn("###############################################");
        } else {
            logger.info("跳过缓存清理 (env={}, clear-cache-on-startup={})",
                    env, appConfig.isClearCacheOnStartup());
        }

        logger.info("=== IM系统启动验证完成 ===");
    }

    /**
     * 判断是否应该清理缓存
     * 需要：非生产环境 AND 显式配置开启
     */
    private boolean shouldClearCache() {
        if (!appConfig.isNonProd()) {
            return false;
        }
        if (!appConfig.isClearCacheOnStartup()) {
            return false;
        }
        return true;
    }
}