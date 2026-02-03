package com.ruoyi.im.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constant.ErrorCode;
import com.ruoyi.im.domain.ImApplicationConfig;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImApplicationConfigMapper;
import com.ruoyi.im.service.ImApplicationConfigService;
import com.ruoyi.im.dto.app.ImAppConfigRequest.ConfigItem;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用配置服务实现类
 *
 * @author ruoyi
 */
@Service
public class ImApplicationConfigServiceImpl implements ImApplicationConfigService {

    private static final Logger log = LoggerFactory.getLogger(ImApplicationConfigServiceImpl.class);

    @Resource
    private ImApplicationConfigMapper configMapper;

    @Override
    public List<ImApplicationConfig> getAppConfigs(Long appId) {
        return configMapper.selectByAppId(appId);
    }

    @Override
    public Map<String, List<ImApplicationConfig>> getAppConfigsGrouped(Long appId) {
        List<ImApplicationConfig> configs = getAppConfigs(appId);
        return configs.stream()
                .collect(Collectors.groupingBy(
                        config -> StringUtils.isBlank(config.getConfigGroup()) ? "默认" : config.getConfigGroup()
                ));
    }

    @Override
    public String getConfigValue(Long appId, String configKey) {
        ImApplicationConfig config = configMapper.selectByAppIdAndKey(appId, configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public Map<String, Object> getAppConfigMap(Long appId) {
        List<ImApplicationConfig> configs = getAppConfigs(appId);
        Map<String, Object> configMap = new HashMap<>();

        for (ImApplicationConfig config : configs) {
            Object value = parseConfigValue(config.getConfigValue(), config.getConfigType());
            configMap.put(config.getConfigKey(), value);
        }

        return configMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfigItems(Long appId, List<ConfigItem> configItems) {
        if (configItems == null || configItems.isEmpty()) {
            throw BusinessException.paramError("配置项不能为空");
        }

        for (ConfigItem item : configItems) {
            // 验证配置项
            if (!validateConfigItem(item)) {
                throw BusinessException.paramError("配置项验证失败: " + item.getKey());
            }

            // 检查配置是否已存在
            ImApplicationConfig existing = configMapper.selectByAppIdAndKey(appId, item.getKey());

            if (existing != null) {
                // 更新现有配置
                existing.setConfigValue(formatConfigValue(item.getValue(), item.getType()));
                if (item.getType() != null) {
                    existing.setConfigType(item.getType());
                }
                if (item.getDescription() != null) {
                    existing.setDescription(item.getDescription());
                }
                if (item.getRequired() != null) {
                    existing.setIsRequired(item.getRequired() ? 1 : 0);
                }
                if (item.getOptions() != null && !item.getOptions().isEmpty()) {
                    existing.setOptions(JSON.toJSONString(item.getOptions()));
                }
                if (item.getGroup() != null) {
                    existing.setConfigGroup(item.getGroup());
                }
                configMapper.updateById(existing);
            } else {
                // 新增配置
                ImApplicationConfig newConfig = new ImApplicationConfig();
                newConfig.setAppId(appId);
                newConfig.setConfigKey(item.getKey());
                newConfig.setConfigValue(formatConfigValue(item.getValue(), item.getType()));
                newConfig.setConfigType(item.getType() != null ? item.getType() : "STRING");
                newConfig.setDescription(item.getDescription());
                newConfig.setIsRequired(item.getRequired() != null && item.getRequired() ? 1 : 0);
                if (item.getOptions() != null && !item.getOptions().isEmpty()) {
                    newConfig.setOptions(JSON.toJSONString(item.getOptions()));
                }
                newConfig.setDefaultValue(formatConfigValue(item.getDefaultValue(), item.getType()));
                newConfig.setConfigGroup(item.getGroup());
                newConfig.setIsEnabled(1);
                configMapper.insert(newConfig);
            }
        }

        log.info("保存应用配置成功: appId={}, count={}", appId, configItems.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long configId) {
        configMapper.deleteById(configId);
        log.info("删除应用配置: configId={}", configId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAppConfigs(Long appId) {
        configMapper.deleteByAppId(appId);
        log.info("删除应用所有配置: appId={}", appId);
    }

    @Override
    public boolean validateConfigItem(ConfigItem configItem) {
        if (StringUtils.isBlank(configItem.getKey())) {
            return false;
        }

        // 验证必填项
        if (Boolean.TRUE.equals(configItem.getRequired()) && configItem.getValue() == null) {
            return false;
        }

        // 验证可选值
        if (configItem.getOptions() != null && !configItem.getOptions().isEmpty()) {
            if (configItem.getValue() != null && !configItem.getOptions().contains(configItem.getValue())) {
                return false;
            }
        }

        // 验证类型
        if (!isValidType(configItem.getType())) {
            return false;
        }

        return true;
    }

    @Override
    public Map<String, Object> getDefaultConfigs(Long appId) {
        List<ImApplicationConfig> configs = getAppConfigs(appId);
        Map<String, Object> defaultConfigs = new HashMap<>();

        for (ImApplicationConfig config : configs) {
            if (config.getDefaultValue() != null) {
                Object value = parseConfigValue(config.getDefaultValue(), config.getConfigType());
                defaultConfigs.put(config.getConfigKey(), value);
            }
        }

        return defaultConfigs;
    }

    /**
     * 格式化配置值为字符串
     */
    private String formatConfigValue(Object value, String type) {
        if (value == null) {
            return null;
        }

        switch (type != null ? type : "STRING") {
            case "JSON":
            case "ARRAY":
                return value instanceof String ? (String) value : JSON.toJSONString(value);
            case "BOOLEAN":
                return String.valueOf(Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value)));
            case "NUMBER":
                return String.valueOf(value);
            default:
                return String.valueOf(value);
        }
    }

    /**
     * 解析配置值为对象
     */
    private Object parseConfigValue(String value, String type) {
        if (value == null) {
            return null;
        }

        switch (type != null ? type : "STRING") {
            case "NUMBER":
                try {
                    if (value.contains(".")) {
                        return Double.parseDouble(value);
                    }
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    return value;
                }
            case "BOOLEAN":
                return Boolean.parseBoolean(value);
            case "JSON":
                return JSON.parseObject(value);
            case "ARRAY":
                return JSON.parseArray(value);
            default:
                return value;
        }
    }

    /**
     * 验证类型是否有效
     */
    private boolean isValidType(String type) {
        if (type == null) {
            return true;
        }
        return Arrays.asList("STRING", "NUMBER", "BOOLEAN", "JSON", "ARRAY").contains(type);
    }
}
