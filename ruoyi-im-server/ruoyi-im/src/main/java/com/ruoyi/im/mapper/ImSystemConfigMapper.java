package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImSystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统配置Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImSystemConfigMapper extends BaseMapper<ImSystemConfig> {

    /**
     * 根据配置键查询配置
     * 
     * @param configKey 配置键
     * @return 系统配置
     */
    ImSystemConfig selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 查询启用的配置列表
     * 
     * @return 系统配置列表
     */
    List<ImSystemConfig> selectEnabledConfigs();

    /**
     * 根据配置类型查询配置列表
     * 
     * @param type 配置类型
     * @return 系统配置列表
     */
    List<ImSystemConfig> selectConfigsByType(@Param("type") String type);

    /**
     * 搜索配置（根据配置键或描述）
     * 
     * @param keyword 搜索关键词
     * @return 系统配置列表
     */
    List<ImSystemConfig> searchConfigs(@Param("keyword") String keyword);

    /**
     * 检查配置键是否存在
     * 
     * @param configKey 配置键
     * @return 是否存在
     */
    boolean existsConfigKey(@Param("configKey") String configKey);

    /**
     * 更新配置值
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 更新数量
     */
    int updateConfigValue(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 批量更新配置状态
     * 
     * @param configIds 配置ID列表
     * @param enabled 是否启用
     * @return 更新数量
     */
    int updateEnabledBatch(@Param("configIds") List<Long> configIds, @Param("enabled") Boolean enabled);

    /**
     * 批量插入配置
     * 
     * @param configs 配置列表
     * @return 插入数量
     */
    int insertBatch(@Param("configs") List<ImSystemConfig> configs);

    /**
     * 批量删除配置
     * 
     * @param configIds 配置ID列表
     * @return 删除数量
     */
    int deleteBatchByIds(@Param("configIds") List<Long> configIds);

    /**
     * 统计各类型配置数量
     * 
     * @return 统计信息列表
     */
    List<Object> countConfigsByType();

    /**
     * 查询所有配置类型
     * 
     * @return 配置类型列表
     */
    List<String> selectAllTypes();

    /**
     * 导出配置数据
     * 
     * @param type 配置类型（可选）
     * @param enabled 是否启用（可选）
     * @return 系统配置列表
     */
    List<ImSystemConfig> exportConfigs(@Param("type") String type, @Param("enabled") Boolean enabled);

    /**
     * 重置配置为默认值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 更新数量
     */
    int resetConfigToDefault(@Param("configKey") String configKey, @Param("defaultValue") String defaultValue);
}