package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImSystemConfig;
import com.ruoyi.im.mapper.ImSystemConfigMapper;
import com.ruoyi.im.service.ImSystemConfigService;
import com.ruoyi.im.util.ImRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现
 * 基于数据库驱动，并使用Redis存储系统级配置
 *
 * @author ruoyi
 */
@Service
public class ImSystemConfigServiceImpl implements ImSystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(ImSystemConfigServiceImpl.class);

    /**
     * Redis配置Key
     */
    private static final String SYSTEM_CONFIG_ALL_KEY = "im:system:config:all";

    /**
     * 消息撤回时间限制配置Key (与数据库中config_key一致)
     */
    private static final String CONFIG_RECALL_TIME_LIMIT = "message.recall.time.limit";

    /**
     * 默认消息撤回时间限制（分钟）
     */
    private static final Integer DEFAULT_RECALL_TIME_LIMIT = 5;

    @Autowired
    private ImRedisUtil redisUtil;

    @Autowired
    private ImSystemConfigMapper configMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        // 预热缓存
        loadConfigsToCache();
    }

    private void loadConfigsToCache() {
        try {
            List<ImSystemConfig> configs = configMapper.selectAllConfigs();
            Map<String, String> configMap = new HashMap<>();
            for (ImSystemConfig config : configs) {
                configMap.put(config.getConfigKey(), config.getConfigValue());
            }
            redisUtil.set(SYSTEM_CONFIG_ALL_KEY, configMap, 24, TimeUnit.HOURS);
            log.info("系统配置已加载至缓存，共 {} 项", configMap.size());
        } catch (Exception e) {
            log.error("预热系统配置缓存失败", e);
        }
    }

    @Override
    public Integer getMessageRecallTimeLimit() {
        String value = getConfigValue(CONFIG_RECALL_TIME_LIMIT);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.warn("解析撤回时间限制失败: {}", value, e);
            }
        }
        return DEFAULT_RECALL_TIME_LIMIT;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setMessageRecallTimeLimit(Integer minutes) {
        updateSystemConfig(CONFIG_RECALL_TIME_LIMIT, String.valueOf(minutes));
    }

    @Override
    public Map<String, Object> getAllSystemConfigs() {
        Object cached = redisUtil.get(SYSTEM_CONFIG_ALL_KEY);
        if (cached instanceof Map) {
            return (Map<String, Object>) cached;
        }
        
        // 缓存失效，从DB加载
        List<ImSystemConfig> configs = configMapper.selectAllConfigs();
        Map<String, Object> configMap = new HashMap<>();
        for (ImSystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        // 更新缓存
        redisUtil.set(SYSTEM_CONFIG_ALL_KEY, configMap, 24, TimeUnit.HOURS);
        return configMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSystemConfig(String configKey, Object configValue) {
        String strValue = String.valueOf(configValue);
        
        // 1. 更新数据库
        ImSystemConfig config = configMapper.selectByKey(configKey);
        if (config != null) {
            config.setConfigValue(strValue);
            config.setUpdateTime(LocalDateTime.now());
            configMapper.updateById(config);
        } else {
            // 如果配置项不存在，则创建
            config = new ImSystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(strValue);
            config.setConfigType("DYNAMIC");
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            configMapper.insert(config);
        }
        
        // 2. 清除/更新缓存
        loadConfigsToCache();
        log.info("系统配置已更新: {} = {}", configKey, strValue);
    }

    private String getConfigValue(String key) {
        Map<String, Object> configs = getAllSystemConfigs();
        Object value = configs.get(key);
        return value != null ? value.toString() : null;
    }
}