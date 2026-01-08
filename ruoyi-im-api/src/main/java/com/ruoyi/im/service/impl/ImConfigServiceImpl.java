package com.ruoyi.im.service.impl;

import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.service.ImConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户配置服务实现
 * 注意：当前版本使用内存存储，生产环境应使用数据库表存储
 *
 * @author ruoyi
 */
@Service
public class ImConfigServiceImpl implements ImConfigService {

    @Autowired
    private ImFriendMapper imFriendMapper;

    // 内存存储用户配置（生产环境应使用数据库）
    private static final Map<String, Map<String, Object>> USER_CONFIG_CACHE = new HashMap<>();

    // 默认通知设置
    private static final Map<String, Object> DEFAULT_NOTIFICATION_SETTINGS = new HashMap<>();
    static {
        DEFAULT_NOTIFICATION_SETTINGS.put("messageNotification", true);
        DEFAULT_NOTIFICATION_SETTINGS.put("groupNotification", true);
        DEFAULT_NOTIFICATION_SETTINGS.put("systemNotification", true);
        DEFAULT_NOTIFICATION_SETTINGS.put("soundEnabled", true);
        DEFAULT_NOTIFICATION_SETTINGS.put("desktopNotification", true);
        DEFAULT_NOTIFICATION_SETTINGS.put("muteAll", false);
    }

    // 默认隐私设置
    private static final Map<String, Object> DEFAULT_PRIVACY_SETTINGS = new HashMap<>();
    static {
        DEFAULT_PRIVACY_SETTINGS.put("showOnlineStatus", true);
        DEFAULT_PRIVACY_SETTINGS.put("allowStrangerMessage", false);
        DEFAULT_PRIVACY_SETTINGS.put("allowVoiceCall", true);
        DEFAULT_PRIVACY_SETTINGS.put("allowVideoCall", true);
        DEFAULT_PRIVACY_SETTINGS.put("showLastSeen", false);
    }

    // 默认通用设置
    private static final Map<String, Object> DEFAULT_GENERAL_SETTINGS = new HashMap<>();
    static {
        DEFAULT_GENERAL_SETTINGS.put("language", "zh-CN");
        DEFAULT_GENERAL_SETTINGS.put("theme", "light");
        DEFAULT_GENERAL_SETTINGS.put("fontSize", "medium");
        DEFAULT_GENERAL_SETTINGS.put("enterToSend", true);
    }

    @Override
    public Map<String, Object> getNotificationSettings(Long userId) {
        return getUserSettings(userId, "notification", DEFAULT_NOTIFICATION_SETTINGS);
    }

    @Override
    public void updateNotificationSettings(Long userId, Map<String, Object> settings) {
        Map<String, Object> current = getNotificationSettings(userId);
        current.putAll(settings);
        setUserSettings(userId, "notification", current);
    }

    @Override
    public Map<String, Object> getPrivacySettings(Long userId) {
        return getUserSettings(userId, "privacy", DEFAULT_PRIVACY_SETTINGS);
    }

    @Override
    public void updatePrivacySettings(Long userId, Map<String, Object> settings) {
        Map<String, Object> current = getPrivacySettings(userId);
        current.putAll(settings);
        setUserSettings(userId, "privacy", current);
    }

    @Override
    public Map<String, Object> getGeneralSettings(Long userId) {
        return getUserSettings(userId, "general", DEFAULT_GENERAL_SETTINGS);
    }

    @Override
    public void updateGeneralSettings(Long userId, Map<String, Object> settings) {
        Map<String, Object> current = getGeneralSettings(userId);
        current.putAll(settings);
        setUserSettings(userId, "general", current);
    }

    @Override
    public List<Map<String, Object>> getBlockedUsers(Long userId) {
        // 从im_friend表中查询拉黑的好友
        List<Map<String, Object>> blockedUsers = new ArrayList<>();
        // 这里需要通过ImFriendMapper查询被拉黑的好友
        // 由于当前mapper没有直接方法，这里返回空列表
        // 实际实现需要添加相应的mapper方法
        return blockedUsers;
    }

    @Override
    public void blockUser(Long userId, Long targetUserId) {
        // 通过将好友状态设置为BLOCKED来实现拉黑
        // 实际实现需要调用ImFriendService.blockFriend方法
    }

    @Override
    public void unblockUser(Long userId, Long targetUserId) {
        // 通过将好友状态设置为NORMAL来实现解除拉黑
        // 实际实现需要调用ImFriendService.blockUser方法，blocked=false
    }

    /**
     * 获取用户设置
     */
    private Map<String, Object> getUserSettings(Long userId, String type, Map<String, Object> defaults) {
        String key = userId + ":" + type;
        return USER_CONFIG_CACHE.getOrDefault(key, new HashMap<>(defaults));
    }

    /**
     * 保存用户设置
     */
    private void setUserSettings(Long userId, String type, Map<String, Object> settings) {
        String key = userId + ":" + type;
        USER_CONFIG_CACHE.put(key, new HashMap<>(settings));
    }
}
