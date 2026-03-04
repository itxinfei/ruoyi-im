package com.ruoyi.im.service.impl;

import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.service.ImSystemConfigService;
import com.ruoyi.im.util.ImRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现
 * 使用 Redis 存储系统级配置
 * 配置分类：
 * 1. 消息策略：撤回时限、最大消息长度
 * 2. 上传策略：单文件上限、会话过期时长
 * 3. 安全策略：登录失败阈值、密码复杂度级别
 *
 * @author ruoyi
 */
@Service
public class ImSystemConfigServiceImpl implements ImSystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(ImSystemConfigServiceImpl.class);

    /**
     * Redis 配置 Key 前缀
     */
    private static final String SYSTEM_CONFIG_PREFIX = "im:system:config:";

    // ==================== 消息策略配置 ====================

    /**
     * 消息撤回时间限制配置 Key（分钟）
     */
    private static final String CONFIG_RECALL_TIME_LIMIT = "message.recall.time.limit";

    /**
     * 默认消息撤回时间限制（分钟）
     */
    private static final Integer DEFAULT_RECALL_TIME_LIMIT = 5;

    /**
     * 最大消息长度配置 Key
     */
    private static final String CONFIG_MAX_MESSAGE_LENGTH = "message.max.length";

    /**
     * 默认最大消息长度
     */
    private static final Integer DEFAULT_MAX_MESSAGE_LENGTH = 2000;

    // ==================== 上传策略配置 ====================

    /**
     * 单文件上传上限配置 Key（MB）
     */
    private static final String CONFIG_UPLOAD_MAX_FILE_MB = "upload.maxFileMb";

    /**
     * 默认单文件上传上限（MB）
     */
    private static final Integer DEFAULT_UPLOAD_MAX_FILE_MB = 100;

    /**
     * 会话过期时长配置 Key（分钟）
     */
    private static final String CONFIG_SESSION_EXPIRE_MINUTES = "security.session.expireMinutes";

    /**
     * 默认会话过期时长（分钟）
     */
    private static final Integer DEFAULT_SESSION_EXPIRE_MINUTES = 120;

    // ==================== 安全策略配置 ====================

    /**
     * 登录失败阈值配置 Key
     */
    private static final String CONFIG_LOGIN_FAILURE_THRESHOLD = "security.login.failureThreshold";

    /**
     * 默认登录失败阈值
     */
    private static final Integer DEFAULT_LOGIN_FAILURE_THRESHOLD = 5;

    /**
     * 密码复杂度级别配置 Key（1=简单 2=中等 3=复杂）
     */
    private static final String CONFIG_PASSWORD_COMPLEXITY_LEVEL = "security.password.complexityLevel";

    /**
     * 默认密码复杂度级别
     */
    private static final Integer DEFAULT_PASSWORD_COMPLEXITY_LEVEL = 2;

    @Autowired
    private ImRedisUtil redisUtil;

    @Override
    public Integer getMessageRecallTimeLimit() {
        try {
            String key = SYSTEM_CONFIG_PREFIX + CONFIG_RECALL_TIME_LIMIT;
            Object value = redisUtil.get(key);
            if (value != null) {
                try {
                    return Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    log.warn("解析撤回时间限制失败：{}", value, e);
                }
            }
            return DEFAULT_RECALL_TIME_LIMIT;
        } catch (Exception e) {
            log.error("获取消息撤回时间限制失败", e);
            return DEFAULT_RECALL_TIME_LIMIT;
        }
    }

    @Override
    public void setMessageRecallTimeLimit(Integer minutes) {
        try {
            if (minutes == null || minutes < 0) {
                throw new IllegalArgumentException("撤回时间限制必须大于等于 0");
            }
            String key = SYSTEM_CONFIG_PREFIX + CONFIG_RECALL_TIME_LIMIT;
            redisUtil.set(key, String.valueOf(minutes), 30, TimeUnit.MINUTES);
            log.info("更新消息撤回时间限制：{} 分钟", minutes);
        } catch (Exception e) {
            log.error("设置消息撤回时间限制失败", e);
            throw new RuntimeException("设置消息撤回时间限制失败", e);
        }
    }

    @Override
    public Map<String, Object> getAllSystemConfigs() {
        Map<String, Object> configs = new HashMap<>();
        // 消息策略
        configs.put(CONFIG_RECALL_TIME_LIMIT, getMessageRecallTimeLimit());
        configs.put(CONFIG_MAX_MESSAGE_LENGTH, getMaxMessageLength());

        // 上传策略
        configs.put(CONFIG_UPLOAD_MAX_FILE_MB, getUploadMaxFileMb());
        configs.put(CONFIG_SESSION_EXPIRE_MINUTES, getSessionExpireMinutes());

        // 安全策略
        configs.put(CONFIG_LOGIN_FAILURE_THRESHOLD, getLoginFailureThreshold());
        configs.put(CONFIG_PASSWORD_COMPLEXITY_LEVEL, getPasswordComplexityLevel());

        return configs;
    }

    @Override
    public void updateSystemConfig(String configKey, Object configValue) {
        // 验证配置键是否支持
        if (!isValidConfigKey(configKey)) {
            throw new IllegalArgumentException("不支持的配置键：" + configKey);
        }

        // 值验证和转换
        Integer intValue = convertConfigValue(configKey, configValue);

        // 范围验证
        validateConfigRange(configKey, intValue);

        // 保存到 Redis
        String key = SYSTEM_CONFIG_PREFIX + configKey;
        redisUtil.set(key, String.valueOf(intValue), 30, TimeUnit.MINUTES);
        log.info("更新系统配置：key={}, value={}", configKey, intValue);
    }

    /**
     * 判断配置键是否有效
     */
    private boolean isValidConfigKey(String configKey) {
        return CONFIG_RECALL_TIME_LIMIT.equals(configKey)
            || CONFIG_MAX_MESSAGE_LENGTH.equals(configKey)
            || CONFIG_UPLOAD_MAX_FILE_MB.equals(configKey)
            || CONFIG_SESSION_EXPIRE_MINUTES.equals(configKey)
            || CONFIG_LOGIN_FAILURE_THRESHOLD.equals(configKey)
            || CONFIG_PASSWORD_COMPLEXITY_LEVEL.equals(configKey);
    }

    /**
     * 转换并验证配置值
     */
    private Integer convertConfigValue(String configKey, Object configValue) {
        try {
            if (configValue instanceof Integer) {
                return (Integer) configValue;
            } else if (configValue instanceof String) {
                return Integer.parseInt((String) configValue);
            } else if (configValue instanceof Number) {
                return ((Number) configValue).intValue();
            } else {
                throw new IllegalArgumentException("不支持的配置值类型：" + configValue.getClass().getSimpleName());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("配置值格式错误：" + configValue);
        }
    }

    /**
     * 验证配置值是否在允许范围内
     */
    private void validateConfigRange(String configKey, Integer value) {
        if (CONFIG_RECALL_TIME_LIMIT.equals(configKey)) {
            if (value < 0 || value > SystemConstants.CONFIG_MAX_WEEKS) {
                throw new IllegalArgumentException("撤回时间限制必须在 0-10080 分钟之间");
            }
        } else if (CONFIG_MAX_MESSAGE_LENGTH.equals(configKey)) {
            if (value < SystemConstants.CONFIG_MIN_VALUE || value > SystemConstants.CONFIG_MAX_VALUE) {
                throw new IllegalArgumentException("最大消息长度必须在 100-10000 之间");
            }
        } else if (CONFIG_UPLOAD_MAX_FILE_MB.equals(configKey)) {
            if (value < 1 || value > 2048) {
                throw new IllegalArgumentException("单文件上限必须在 1-2048 MB 之间");
            }
        } else if (CONFIG_SESSION_EXPIRE_MINUTES.equals(configKey)) {
            if (value < 10 || value > 43200) {
                throw new IllegalArgumentException("会话过期时长必须在 10-43200 分钟之间");
            }
        } else if (CONFIG_LOGIN_FAILURE_THRESHOLD.equals(configKey)) {
            if (value < 1 || value > 20) {
                throw new IllegalArgumentException("登录失败阈值必须在 1-20 之间");
            }
        } else if (CONFIG_PASSWORD_COMPLEXITY_LEVEL.equals(configKey)) {
            if (value < 1 || value > 3) {
                throw new IllegalArgumentException("密码复杂度级别必须在 1-3 之间");
            }
        }
    }

    /**
     * 检查配置是否需要 SUPER_ADMIN 权限
     * @param configKey 配置键
     * @return true 表示需要 SUPER_ADMIN 权限
     */
    public boolean requiresSuperAdmin(String configKey) {
        // 安全策略相关配置仅 SUPER_ADMIN 可修改
        return CONFIG_LOGIN_FAILURE_THRESHOLD.equals(configKey)
            || CONFIG_PASSWORD_COMPLEXITY_LEVEL.equals(configKey);
    }

    // ==================== 辅助方法 ====================

    private Integer getMaxMessageLength() {
        try {
            String key = SYSTEM_CONFIG_PREFIX + CONFIG_MAX_MESSAGE_LENGTH;
            Object value = redisUtil.get(key);
            if (value != null) {
                return Integer.parseInt(value.toString());
            }
            return DEFAULT_MAX_MESSAGE_LENGTH;
        } catch (Exception e) {
            log.error("获取最大消息长度失败", e);
            return DEFAULT_MAX_MESSAGE_LENGTH;
        }
    }

    private Integer getUploadMaxFileMb() {
        try {
            String key = SYSTEM_CONFIG_PREFIX + CONFIG_UPLOAD_MAX_FILE_MB;
            Object value = redisUtil.get(key);
            if (value != null) {
                return Integer.parseInt(value.toString());
            }
            return DEFAULT_UPLOAD_MAX_FILE_MB;
        } catch (Exception e) {
            log.error("获取单文件上限失败", e);
            return DEFAULT_UPLOAD_MAX_FILE_MB;
        }
    }

    private Integer getSessionExpireMinutes() {
        try {
            String key = SYSTEM_CONFIG_PREFIX + CONFIG_SESSION_EXPIRE_MINUTES;
            Object value = redisUtil.get(key);
            if (value != null) {
                return Integer.parseInt(value.toString());
            }
            return DEFAULT_SESSION_EXPIRE_MINUTES;
        } catch (Exception e) {
            log.error("获取会话过期时长失败", e);
            return DEFAULT_SESSION_EXPIRE_MINUTES;
        }
    }

    private Integer getLoginFailureThreshold() {
        try {
            String key = SYSTEM_CONFIG_PREFIX + CONFIG_LOGIN_FAILURE_THRESHOLD;
            Object value = redisUtil.get(key);
            if (value != null) {
                return Integer.parseInt(value.toString());
            }
            return DEFAULT_LOGIN_FAILURE_THRESHOLD;
        } catch (Exception e) {
            log.error("获取登录失败阈值失败", e);
            return DEFAULT_LOGIN_FAILURE_THRESHOLD;
        }
    }

    private Integer getPasswordComplexityLevel() {
        try {
            String key = SYSTEM_CONFIG_PREFIX + CONFIG_PASSWORD_COMPLEXITY_LEVEL;
            Object value = redisUtil.get(key);
            if (value != null) {
                return Integer.parseInt(value.toString());
            }
            return DEFAULT_PASSWORD_COMPLEXITY_LEVEL;
        } catch (Exception e) {
            log.error("获取密码复杂度级别失败", e);
            return DEFAULT_PASSWORD_COMPLEXITY_LEVEL;
        }
    }
}
