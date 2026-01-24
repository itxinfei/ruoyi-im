package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImUserSetting;
import java.util.List;
import java.util.Map;

/**
 * 用户设置Service接口
 *
 * @author ruoyi
 */
public interface ImUserSettingService {

    /**
     * 查询用户设置
     *
     * @param id 用户设置主键
     * @return 用户设置
     */
    ImUserSetting selectImUserSettingById(Long id);

    /**
     * 根据用户ID和设置键查询设置
     *
     * @param userId 用户ID
     * @param settingKey 设置键
     * @return 用户设置
     */
    ImUserSetting selectByUserIdAndKey(Long userId, String settingKey);

    /**
     * 查询用户设置列表
     *
     * @param imUserSetting 用户设置
     * @return 用户设置集合
     */
    List<ImUserSetting> selectImUserSettingList(ImUserSetting imUserSetting);

    /**
     * 查询用户的所有设置
     *
     * @param userId 用户ID
     * @return 设置列表
     */
    List<ImUserSetting> selectSettingsByUserId(Long userId);

    /**
     * 查询指定类型的设置
     *
     * @param userId 用户ID
     * @param settingType 设置类型
     * @return 设置列表
     */
    List<ImUserSetting> selectSettingsByType(Long userId, String settingType);

    /**
     * 新增用户设置
     *
     * @param imUserSetting 用户设置
     * @return 结果
     */
    int insertImUserSetting(ImUserSetting imUserSetting);

    /**
     * 修改用户设置
     *
     * @param imUserSetting 用户设置
     * @return 结果
     */
    int updateImUserSetting(ImUserSetting imUserSetting);

    /**
     * 批量删除用户设置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImUserSettingByIds(Long[] ids);

    /**
     * 删除用户设置信息
     *
     * @param id 用户设置主键
     * @return 结果
     */
    int deleteImUserSettingById(Long id);

    /**
     * 删除用户的所有设置
     *
     * @param userId 用户ID
     * @return 结果
     */
    int deleteSettingsByUserId(Long userId);

    /**
     * 重置用户设置为默认值
     *
     * @param userId 用户ID
     * @return 结果
     */
    int resetUserSettings(Long userId);

    /**
     * 获取设置统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getSettingStatistics();

    /**
     * 获取设置值
     *
     * @param userId 用户ID
     * @param settingKey 设置键
     * @param defaultValue 默认值
     * @return 设置值
     */
    String getSettingValue(Long userId, String settingKey, String defaultValue);

    /**
     * 更新设置值
     *
     * @param userId 用户ID
     * @param settingKey 设置键
     * @param settingValue 设置值
     * @return 结果
     */
    int updateSettingValue(Long userId, String settingKey, String settingValue);
}
