package com.ruoyi.im.service;

import java.util.List;
import java.util.Map;

/**
 * 用户配置服务接口
 *
 * @author ruoyi
 */
public interface ImConfigService {

    /**
     * 获取通知设置
     *
     * @param userId 用户ID
     * @return 通知设置
     */
    Map<String, Object> getNotificationSettings(Long userId);

    /**
     * 更新通知设置
     *
     * @param userId 用户ID
     * @param settings 设置项
     */
    void updateNotificationSettings(Long userId, Map<String, Object> settings);

    /**
     * 获取隐私设置
     *
     * @param userId 用户ID
     * @return 隐私设置
     */
    Map<String, Object> getPrivacySettings(Long userId);

    /**
     * 更新隐私设置
     *
     * @param userId 用户ID
     * @param settings 设置项
     */
    void updatePrivacySettings(Long userId, Map<String, Object> settings);

    /**
     * 获取通用设置
     *
     * @param userId 用户ID
     * @return 通用设置
     */
    Map<String, Object> getGeneralSettings(Long userId);

    /**
     * 更新通用设置
     *
     * @param userId 用户ID
     * @param settings 设置项
     */
    void updateGeneralSettings(Long userId, Map<String, Object> settings);

    /**
     * 获取黑名单用户列表
     *
     * @param userId 用户ID
     * @return 黑名单用户列表
     */
    List<Map<String, Object>> getBlockedUsers(Long userId);

    /**
     * 拉黑用户
     *
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     */
    void blockUser(Long userId, Long targetUserId);

    /**
     * 解除拉黑
     *
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     */
    void unblockUser(Long userId, Long targetUserId);
}
