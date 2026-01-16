package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImSystemConfig;
import java.util.List;
import java.util.Map;

/**
 * 系统配置Service接口
 *
 * @author ruoyi
 */
public interface ImSystemConfigService {

    /**
     * 查询系统配置
     *
     * @param id 系统配置主键
     * @return 系统配置
     */
    ImSystemConfig selectImSystemConfigById(Long id);

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 系统配置
     */
    ImSystemConfig selectImSystemConfigByKey(String configKey);

    /**
     * 查询系统配置列表
     *
     * @param imSystemConfig 系统配置
     * @return 系统配置集合
     */
    List<ImSystemConfig> selectImSystemConfigList(ImSystemConfig imSystemConfig);

    /**
     * 新增系统配置
     *
     * @param imSystemConfig 系统配置
     * @return 结果
     */
    int insertImSystemConfig(ImSystemConfig imSystemConfig);

    /**
     * 修改系统配置
     *
     * @param imSystemConfig 系统配置
     * @return 结果
     */
    int updateImSystemConfig(ImSystemConfig imSystemConfig);

    /**
     * 批量删除系统配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImSystemConfigByIds(Long[] ids);

    /**
     * 删除系统配置信息
     *
     * @param id 系统配置主键
     * @return 结果
     */
    int deleteImSystemConfigById(Long id);

    /**
     * 切换配置状态
     *
     * @param id 配置ID
     * @return 结果
     */
    int toggleStatus(Long id);

    /**
     * 获取系统配置统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getSystemConfigStatistics();
}
