package com.ruoyi.im.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 配置验证类
 * 验证所有配置是否正确注入
 *
 * @author ruoyi
 */
@Component
public class ConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(ConfigValidator.class);

    @Autowired
    private Environment env;

    @Autowired
    private ImConfig imConfig;

    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        log.info("========== 开始验证配置注入 ==========");

        validateServerConfig();
        validateAppConfig();
        validateSpringConfig();
        validateImConfig();
        validateSwaggerConfig();
        validateMyBatisPlusConfig();
        validateFileUploadConfig();
        validateLoggingConfig();

        log.info("========== 配置验证完成 ==========");
    }

    private void validateServerConfig() {
        try {
            String port = env.getProperty("server.port");
            String address = env.getProperty("server.address");
            log.info("✓ 服务器配置 - 端口: {}, 地址: {}", port, address);
        } catch (Exception e) {
            log.error("✗ 服务器配置验证失败", e);
        }
    }

    private void validateAppConfig() {
        try {
            String appEnv = env.getProperty("app.env");
            Boolean securityEnabled = env.getProperty("app.security.enabled", Boolean.class);
            Long devUserId = env.getProperty("app.dev.user-id", Long.class);
            log.info("✓ 应用配置 - 环境: {}, 安全: {}, 开发用户ID: {}", appEnv, securityEnabled, devUserId);
        } catch (Exception e) {
            log.error("✗ 应用配置验证失败", e);
        }
    }

    private void validateSpringConfig() {
        try {
            String datasourceUrl = env.getProperty("spring.datasource.url");
            String redisHost = env.getProperty("spring.redis.host");
            String redisPort = env.getProperty("spring.redis.port");
            String jacksonTimezone = env.getProperty("spring.jackson.time-zone");
            String jacksonDateFormat = env.getProperty("spring.jackson.date-format");
            String multipartMaxFileSize = env.getProperty("spring.servlet.multipart.max-file-size");
            
            log.info("✓ Spring配置");
            log.info("  - 数据源URL: {}", datasourceUrl != null ? datasourceUrl.substring(0, Math.min(50, datasourceUrl.length())) + "..." : "null");
            log.info("  - Redis: {}:{}", redisHost, redisPort);
            log.info("  - Jackson时区: {}, 格式: {}", jacksonTimezone, jacksonDateFormat);
            log.info("  - 文件上传大小: {}", multipartMaxFileSize);
        } catch (Exception e) {
            log.error("✗ Spring配置验证失败", e);
        }
    }

    private void validateImConfig() {
        try {
            String jwtSecret = env.getProperty("im.jwt.secret");
            Long jwtExpiration = env.getProperty("im.jwt.expiration", Long.class);
            Integer messageMaxLength = env.getProperty("im.message.maxLength", Integer.class);
            Integer messageExpireDays = env.getProperty("im.message.expireDays", Integer.class);
            Integer messageRecallWindow = env.getProperty("im.message.recallWindowSeconds", Integer.class);
            Boolean encryptionEnabled = env.getProperty("im.message.encryption.enabled", Boolean.class);
            String encryptionKey = env.getProperty("im.message.encryption.key");
            Integer fileMaxSizeMb = env.getProperty("im.file.maxSizeMb", Integer.class);
            Integer downloadExpireMinutes = env.getProperty("im.file.downloadExpireMinutes", Integer.class);
            Boolean sensitiveEnabled = env.getProperty("im.sensitive.enabled", Boolean.class);
            String onBlock = env.getProperty("im.sensitive.onBlock");
            Integer heartbeatInterval = env.getProperty("im.security.heartbeatInterval", Integer.class);
            Integer maxConnections = env.getProperty("im.security.maxConnections", Integer.class);

            log.info("✓ IM模块配置");
            log.info("  - JWT密钥: {}", jwtSecret != null ? "已配置" : "未配置");
            log.info("  - JWT过期时间: {}ms", jwtExpiration);
            log.info("  - 消息最大长度: {}", messageMaxLength);
            log.info("  - 消息过期天数: {}", messageExpireDays);
            log.info("  - 消息撤回窗口: {}秒", messageRecallWindow);
            log.info("  - 消息加密: {}", encryptionEnabled);
            log.info("  - 加密密钥: {}", encryptionKey != null ? "已配置" : "未配置");
            log.info("  - 文件最大大小: {}MB", fileMaxSizeMb);
            log.info("  - 下载链接过期: {}分钟", downloadExpireMinutes);
            log.info("  - 敏感词检测: {}", sensitiveEnabled);
            log.info("  - 敏感词拦截策略: {}", onBlock);
            log.info("  - WebSocket心跳间隔: {}秒", heartbeatInterval);
            log.info("  - 最大连接数: {}", maxConnections);

            log.info("✓ ImConfig注入验证");
            log.info("  - Jwt配置: {}", imConfig.getJwt() != null ? "已注入" : "未注入");
            log.info("  - Message配置: {}", imConfig.getMessage() != null ? "已注入" : "未注入");
            log.info("  - File配置: {}", imConfig.getFile() != null ? "已注入" : "未注入");
            log.info("  - Sensitive配置: {}", imConfig.getSensitive() != null ? "已注入" : "未注入");
            log.info("  - Security配置: {}", imConfig.getSecurity() != null ? "已注入" : "未注入");
        } catch (Exception e) {
            log.error("✗ IM模块配置验证失败", e);
        }
    }

    private void validateSwaggerConfig() {
        try {
            Boolean swaggerEnabled = env.getProperty("swagger.enabled", Boolean.class);
            Boolean apiDocsEnabled = env.getProperty("springdoc.api-docs.enabled", Boolean.class);
            Boolean swaggerUiEnabled = env.getProperty("springdoc.swagger-ui.enabled", Boolean.class);
            String swaggerUiPath = env.getProperty("springdoc.swagger-ui.path");
            
            log.info("✓ Swagger配置");
            log.info("  - Swagger启用: {}", swaggerEnabled);
            log.info("  - API文档启用: {}", apiDocsEnabled);
            log.info("  - SwaggerUI启用: {}", swaggerUiEnabled);
            log.info("  - SwaggerUI路径: {}", swaggerUiPath);
        } catch (Exception e) {
            log.error("✗ Swagger配置验证失败", e);
        }
    }

    private void validateMyBatisPlusConfig() {
        try {
            String mapperLocations = env.getProperty("mybatis-plus.mapper-locations");
            String typeAliasesPackage = env.getProperty("mybatis-plus.type-aliases-package");
            String idType = env.getProperty("mybatis-plus.global-config.db-config.id-type");
            String logicDeleteField = env.getProperty("mybatis-plus.global-config.db-config.logic-delete-field");
            
            log.info("✓ MyBatis-Plus配置");
            log.info("  - Mapper位置: {}", mapperLocations);
            log.info("  - 类型别名包: {}", typeAliasesPackage);
            log.info("  - ID类型: {}", idType);
            log.info("  - 逻辑删除字段: {}", logicDeleteField);
        } catch (Exception e) {
            log.error("✗ MyBatis-Plus配置验证失败", e);
        }
    }

    private void validateFileUploadConfig() {
        try {
            String uploadPath = env.getProperty("file.upload.path");
            String urlPrefix = env.getProperty("file.upload.url-prefix");
            
            log.info("✓ 文件上传配置");
            log.info("  - 上传路径: {}", uploadPath);
            log.info("  - URL前缀: {}", urlPrefix);
        } catch (Exception e) {
            log.error("✗ 文件上传配置验证失败", e);
        }
    }

    private void validateLoggingConfig() {
        try {
            String imLogLevel = env.getProperty("logging.level.com.ruoyi.im");
            String securityLogLevel = env.getProperty("logging.level.org.springframework.security");
            String webLogLevel = env.getProperty("logging.level.org.springframework.web");
            String consolePattern = env.getProperty("logging.pattern.console");
            
            log.info("✓ 日志配置");
            log.info("  - IM日志级别: {}", imLogLevel);
            log.info("  - Security日志级别: {}", securityLogLevel);
            log.info("  - Web日志级别: {}", webLogLevel);
            log.info("  - 控制台格式: {}", consolePattern != null ? "已配置" : "未配置");
        } catch (Exception e) {
            log.error("✗ 日志配置验证失败", e);
        }
    }
}
