package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.service.ImSystemConfigService;
import com.ruoyi.im.util.ImRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置服务实现
 * 使用Redis存储系统级配置
 *
 * @author ruoyi
 */
@Service
public class ImSystemConfigServiceImpl implements ImSystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(ImSystemConfigServiceImpl.class);

    /**
     * Redis配置Key前缀
     */
    private static final String SYSTEM_CONFIG_PREFIX = "im:system:config:";

    /**
     * 消息撤回时间限制配置Key
     */
    private static final String CONFIG_RECALL_TIME_LIMIT = "message.recall.time.limit";

    /**
     * 默认消息撤回时间限制（分钟）
     */
    private static final Integer DEFAULT_RECALL_TIME_LIMIT = 5;

    @Autowired
    private ImRedisUtil redisUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Integer getMessageRecallTimeLimit() {
        try {
            String key = SYSTEM_CONFIG_PREFIX + CONFIG_RECALL_TIME_LIMIT;
            String value = redisUtil.get(key);
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    log.warn("解析撤回时间限制失败: {}", value, e);
                }
            }
            // 返回默认值
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
                throw new IllegalArgumentException("撤回时间限制必须大于等于0");
            }
            String key = SYSTEM_CONFIG_PREFIX + CONFIG_RECALL_TIME_LIMIT;
            redisUtil.set(key, String.valueOf(minutes));
            log.info("更新消息撤回时间限制: {} 分钟", minutes);
        } catch (Exception e) {
            log.error("设置消息撤回时间限制失败", e);
            throw new RuntimeException("设置消息撤回时间限制失败", e);
        }
    }

    @Override
    public Map<String, Object> getAllSystemConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CONFIG_RECALL_TIME_LIMIT, getMessageRecallTimeLimit());
        return configs;
    }

    @Override
    public void updateSystemConfig(String configKey, Object configValue) {
        if (CONFIG_RECALL_TIME_LIMIT.equals(configKey)) {
            if (configValue instanceof Integer) {
                setMessageRecallTimeLimit((Integer) configValue);
            } else if (configValue instanceof String) {
                setMessageRecallTimeLimit(Integer.parseInt((String) configValue));
            } else {
                throw new IllegalArgumentException("不支持的配置值类型: " + configValue.getClass());
            }
        } else {
            throw new IllegalArgumentException("不支持的配置键: " + configKey);
        }
    }
}
