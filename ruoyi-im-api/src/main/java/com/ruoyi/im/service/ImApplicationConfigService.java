package com.ruoyi.im.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImApplicationConfig;
import com.ruoyi.im.dto.app.ImAppConfigRequest;
import com.ruoyi.im.dto.app.ImAppConfigRequest.ConfigItem;

import java.util.List;
import java.util.Map;

/**
 * 应用配置服务接口
 *
 * @author ruoyi
 */
public interface ImApplicationConfigService {

    /**
     * 获取应用的所有配置项
     *
     * @param appId 应用ID
     * @return 配置项列表
     */
    List<ImApplicationConfig> getAppConfigs(Long appId);

    /**
     * 获取应用的所有配置项（按分组组织）
     *
     * @param appId 应用ID
     * @return 分组配置项映射
     */
    Map<String, List<ImApplicationConfig>> getAppConfigsGrouped(Long appId);

    /**
     * 获取配置项值
     *
     * @param appId 应用ID
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(Long appId, String configKey);

    /**
     * 获取应用的所有配置（键值对形式）
     *
     * @param appId 应用ID
     * @return 配置键值对
     */
    Map<String, Object> getAppConfigMap(Long appId);

    /**
     * 批量保存/更新配置项
     *
     * @param appId 应用ID
     * @param configItems 配置项列表
     */
    void saveConfigItems(Long appId, List<ConfigItem> configItems);

    /**
     * 删除配置项
     *
     * @param configId 配置ID
     */
    void deleteConfig(Long configId);

    /**
     * 删除应用的所有配置
     *
     * @param appId 应用ID
     */
    void deleteAppConfigs(Long appId);

    /**
     * 验证配置项
     *
     * @param configItem 配置项
     * @return 验证结果，true表示有效
     */
    boolean validateConfigItem(ConfigItem configItem);

    /**
     * 获取默认配置值
     *
     * @param appId 应用ID
     * @return 默认配置值
     */
    Map<String, Object> getDefaultConfigs(Long appId);
}
