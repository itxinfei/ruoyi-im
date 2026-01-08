package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImUserConfig;
import com.ruoyi.im.mapper.ImUserConfigMapper;
import com.ruoyi.im.service.ImConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户配置服务实现
 * 使用数据库表存储用户配置
 *
 * @author ruoyi
 */
@Service
public class ImConfigServiceImpl implements ImConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ImConfigServiceImpl.class);

    @Autowired
    private ImUserConfigMapper imUserConfigMapper;

    @Autowired
    private ObjectMapper objectMapper;

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
        return getUserSettings(userId, "NOTIFICATION", DEFAULT_NOTIFICATION_SETTINGS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotificationSettings(Long userId, Map<String, Object> settings) {
        updateUserSettings(userId, "NOTIFICATION", settings);
    }

    @Override
    public Map<String, Object> getPrivacySettings(Long userId) {
        return getUserSettings(userId, "PRIVACY", DEFAULT_PRIVACY_SETTINGS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivacySettings(Long userId, Map<String, Object> settings) {
        updateUserSettings(userId, "PRIVACY", settings);
    }

    @Override
    public Map<String, Object> getGeneralSettings(Long userId) {
        return getUserSettings(userId, "DISPLAY", DEFAULT_GENERAL_SETTINGS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGeneralSettings(Long userId, Map<String, Object> settings) {
        updateUserSettings(userId, "DISPLAY", settings);
    }

    @Override
    public List<Map<String, Object>> getBlockedUsers(Long userId) {
        // 从拉黑配置中获取
        List<Map<String, Object>> blockedUsers = new ArrayList<>();
        try {
            List<ImUserConfig> configs = imUserConfigMapper.selectByUserIdAndType(userId, "blocked");
            for (ImUserConfig config : configs) {
                Map<String, Object> userConfig = new HashMap<>();
                userConfig.put("userId", Long.valueOf(config.getConfigKey()));
                userConfig.put("blocked", true);
                userConfig.put("blockTime", config.getUpdateTime());
                blockedUsers.add(userConfig);
            }
        } catch (Exception e) {
            logger.error("获取拉黑用户列表失败，userId={}", userId, e);
        }
        return blockedUsers;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blockUser(Long userId, Long targetUserId) {
        ImUserConfig config = new ImUserConfig();
        config.setUserId(userId);
        config.setConfigType("blocked");
        config.setConfigKey(targetUserId.toString());
        config.setConfigValue("true");
        imUserConfigMapper.insertOrUpdate(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unblockUser(Long userId, Long targetUserId) {
        // 删除拉黑配置
        ImUserConfig config = imUserConfigMapper.selectByUserIdAndKey(userId, "blocked", targetUserId.toString());
        if (config != null) {
            imUserConfigMapper.deleteById(config.getId());
        }
    }

    /**
     * 获取用户设置
     *
     * @param userId 用户ID
     * @param type 配置类型
     * @param defaults 默认值
     * @return 设置Map
     */
    private Map<String, Object> getUserSettings(Long userId, String type, Map<String, Object> defaults) {
        Map<String, Object> result = new HashMap<>(defaults);

        try {
            List<ImUserConfig> configs = imUserConfigMapper.selectByUserIdAndType(userId, type);
            for (ImUserConfig config : configs) {
                try {
                    Object value = objectMapper.readValue(config.getConfigValue(), Object.class);
                    result.put(config.getConfigKey(), value);
                } catch (Exception e) {
                    // 如果无法解析为JSON，直接使用字符串值
                    result.put(config.getConfigKey(), config.getConfigValue());
                }
            }
        } catch (Exception e) {
            logger.error("获取用户设置失败，userId={}, type={}", userId, type, e);
        }

        return result;
    }

    /**
     * 更新用户设置
     *
     * @param userId 用户ID
     * @param type 配置类型
     * @param settings 设置Map
     */
    private void updateUserSettings(Long userId, String type, Map<String, Object> settings) {
        try {
            for (Map.Entry<String, Object> entry : settings.entrySet()) {
                ImUserConfig config = new ImUserConfig();
                config.setUserId(userId);
                config.setConfigType(type);
                config.setConfigKey(entry.getKey());
                config.setConfigValue(objectMapper.writeValueAsString(entry.getValue()));
                imUserConfigMapper.insertOrUpdate(config);
            }
        } catch (Exception e) {
            logger.error("更新用户设置失败，userId={}, type={}", userId, type, e);
            throw new RuntimeException("更新用户设置失败", e);
        }
    }
}
