package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImNotificationSetting;

/**
 * 用户通知设置服务接口
 *
 * @author ruoyi
 */
public interface ImNotificationSettingService {

    /**
     * 获取用户通知设置
     *
     * @param userId 用户ID
     * @return 通知设置对象
     */
    ImNotificationSetting getNotificationSetting(Long userId);

    /**
     * 更新通知设置
     *
     * @param userId 用户ID
     * @param setting 通知设置对象
     */
    void updateNotificationSetting(Long userId, ImNotificationSetting setting);

    /**
     * 初始化默认通知设置
     *
     * @param userId 用户ID
     */
    void initDefaultNotificationSetting(Long userId);
}
