package com.ruoyi.im.service;

import com.ruoyi.im.dto.setting.UserSettingUpdateRequest;
import com.ruoyi.im.dto.setting.UserSettingsBatchUpdateRequest;
import com.ruoyi.im.vo.setting.UserSettingVO;

import java.util.List;
import java.util.Map;

/**
 * 用户设置服务接口
 *
 * @author ruoyi
 */
public interface IImUserSettingService {

    /**
     * 获取用户所有设置
     *
     * @param userId 用户ID
     * @return 设置列表
     */
    List<UserSettingVO> getUserSettings(Long userId);

    /**
     * 获取用户设置（按类型）
     *
     * @param userId      用户ID
     * @param settingType 设置类型
     * @return 设置列表
     */
    List<UserSettingVO> getUserSettingsByType(Long userId, String settingType);

    /**
     * 获取用户单个设置值
     *
     * @param userId     用户ID
     * @param settingKey 设置键名
     * @return 设置值，不存在返回null
     */
    String getSettingValue(Long userId, String settingKey);

    /**
     * 获取用户设置映射（键值对）
     *
     * @param userId 用户ID
     * @return 设置键值对映射
     */
    Map<String, String> getUserSettingsMap(Long userId);

    /**
     * 更新用户单个设置
     *
     * @param userId  用户ID
     * @param request 更新请求
     */
    void updateSetting(Long userId, UserSettingUpdateRequest request);

    /**
     * 批量更新用户设置
     *
     * @param userId  用户ID
     * @param request 批量更新请求
     */
    void batchUpdateSettings(Long userId, UserSettingsBatchUpdateRequest request);

    /**
     * 删除用户单个设置
     *
     * @param userId     用户ID
     * @param settingKey 设置键名
     */
    void deleteSetting(Long userId, String settingKey);
}
