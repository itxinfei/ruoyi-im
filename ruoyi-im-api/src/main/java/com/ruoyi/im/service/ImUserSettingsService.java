package com.ruoyi.im.service;

import com.ruoyi.im.dto.settings.UserSettingsBatchUpdateRequest;
import com.ruoyi.im.dto.settings.UserSettingsUpdateRequest;
import com.ruoyi.im.dto.settings.UserSettingsVO;
import com.ruoyi.im.domain.ImUserSettings;

import java.util.List;

/**
 * 用户设置服务接口
 *
 * @author ruoyi
 */
public interface ImUserSettingsService {

    /**
     * 获取用户所有设置
     *
     * @param userId 用户ID
     * @return 用户设置VO
     */
    UserSettingsVO getUserSettings(Long userId);

    /**
     * 获取用户指定类型的设置
     *
     * @param userId 用户ID
     * @param settingType 设置类型
     * @return 设置值列表
     */
    List<ImUserSettings> getSettingsByType(Long userId, String settingType);

    /**
     * 更新单个设置
     *
     * @param userId 用户ID
     * @param request 更新请求
     */
    void updateSetting(Long userId, UserSettingsUpdateRequest request);

    /**
     * 批量更新设置
     *
     * @param userId 用户ID
     * @param request 批量更新请求
     */
    void batchUpdateSettings(Long userId, UserSettingsBatchUpdateRequest request);

    /**
     * 删除设置
     *
     * @param userId 用户ID
     * @param settingKey 设置键
     */
    void deleteSetting(Long userId, String settingKey);

    /**
     * 初始化用户默认设置
     *
     * @param userId 用户ID
     */
    void initDefaultSettings(Long userId);
}
