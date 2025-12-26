package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImSystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImSystemConfigService extends IService<ImSystemConfig> {

    /**
     * 根据配置键获取配置值
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键获取配置值（带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 根据配置键获取整数配置值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 整数配置值
     */
    Integer getIntConfigValue(String configKey, Integer defaultValue);

    /**
     * 根据配置键获取布尔配置值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 布尔配置值
     */
    Boolean getBooleanConfigValue(String configKey, Boolean defaultValue);

    /**
     * 根据配置键获取长整数配置值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 长整数配置值
     */
    Long getLongConfigValue(String configKey, Long defaultValue);

    /**
     * 设置配置值
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否成功
     */
    boolean setConfigValue(String configKey, String configValue);

    /**
     * 批量设置配置值
     * 
     * @param configs 配置映射
     * @return 设置数量
     */
    int setConfigValueBatch(Map<String, String> configs);

    /**
     * 添加新配置
     * 
     * @param config 配置信息
     * @return 是否成功
     */
    boolean addConfig(ImSystemConfig config);

    /**
     * 批量添加配置
     * 
     * @param configs 配置列表
     * @return 添加数量
     */
    int addConfigBatch(List<ImSystemConfig> configs);

    /**
     * 删除配置
     * 
     * @param configId 配置ID
     * @return 是否成功
     */
    boolean deleteConfig(Long configId);

    /**
     * 批量删除配置
     * 
     * @param configIds 配置ID列表
     * @return 删除数量
     */
    int deleteConfigBatch(List<Long> configIds);

    /**
     * 更新配置信息
     * 
     * @param config 配置信息
     * @return 是否成功
     */
    boolean updateConfig(ImSystemConfig config);

    /**
     * 查询启用的配置列表
     * 
     * @return 配置列表
     */
    List<ImSystemConfig> selectEnabledConfigs();

    /**
     * 根据配置类型查询配置
     * 
     * @param type 配置类型
     * @return 配置列表
     */
    List<ImSystemConfig> selectConfigsByType(String type);

    /**
     * 搜索配置
     * 
     * @param keyword 搜索关键词
     * @return 配置列表
     */
    List<ImSystemConfig> searchConfigs(String keyword);

    /**
     * 检查配置键是否存在
     * 
     * @param configKey 配置键
     * @return 是否存在
     */
    boolean existsConfigKey(String configKey);

    /**
     * 启用/禁用配置
     * 
     * @param configId 配置ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean setConfigEnabled(Long configId, boolean enabled);

    /**
     * 批量启用/禁用配置
     * 
     * @param configIds 配置ID列表
     * @param enabled 是否启用
     * @return 更新数量
     */
    int setConfigEnabledBatch(List<Long> configIds, boolean enabled);

    /**
     * 获取所有配置类型
     * 
     * @return 配置类型列表
     */
    List<String> getAllConfigTypes();

    /**
     * 统计各类型配置数量
     * 
     * @return 统计信息
     */
    Map<String, Integer> countConfigsByType();

    /**
     * 导出配置数据
     * 
     * @param type 配置类型（可选）
     * @param enabled 是否启用（可选）
     * @return 配置列表
     */
    List<ImSystemConfig> exportConfigs(String type, Boolean enabled);

    /**
     * 从文件导入配置
     * 
     * @param filePath 文件路径
     * @param type 配置类型
     * @return 导入数量
     */
    int importConfigsFromFile(String filePath, String type);

    /**
     * 重置配置为默认值
     * 
     * @param configKey 配置键
     * @return 是否成功
     */
    boolean resetConfigToDefault(String configKey);

    /**
     * 批量重置配置为默认值
     * 
     * @param configKeys 配置键列表
     * @return 重置数量
     */
    int resetConfigsToDefaultBatch(List<String> configKeys);

    /**
     * 获取配置详细信息
     * 
     * @param configKey 配置键
     * @return 配置信息
     */
    ImSystemConfig getConfigDetail(String configKey);

    /**
     * 验证配置值格式
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否有效
     */
    boolean validateConfigValue(String configKey, String configValue);

    /**
     * 刷新配置缓存
     * 
     * @return 是否成功
     */
    boolean refreshConfigCache();

    /**
     * 获取配置变更历史
     * 
     * @param configKey 配置键
     * @param limit 限制数量
     * @return 变更历史列表
     */
    List<Map<String, Object>> getConfigChangeHistory(String configKey, int limit);
}