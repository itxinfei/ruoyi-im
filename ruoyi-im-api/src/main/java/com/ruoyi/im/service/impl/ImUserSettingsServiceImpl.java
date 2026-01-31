package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.dto.settings.UserSettingsBatchUpdateRequest;
import com.ruoyi.im.dto.settings.UserSettingsUpdateRequest;
import com.ruoyi.im.dto.settings.UserSettingsVO;
import com.ruoyi.im.domain.ImUserSettings;
import com.ruoyi.im.mapper.ImUserSettingsMapper;
import com.ruoyi.im.service.ImUserSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户设置服务实现
 *
 * @author ruoyi
 */
@Service
public class ImUserSettingsServiceImpl extends ServiceImpl<ImUserSettingsMapper, ImUserSettings>
        implements ImUserSettingsService {

    private static final Logger log = LoggerFactory.getLogger(ImUserSettingsServiceImpl.class);

    private final ImUserSettingsMapper userSettingsMapper;

    public ImUserSettingsServiceImpl(ImUserSettingsMapper userSettingsMapper) {
        this.userSettingsMapper = userSettingsMapper;
    }

    @Override
    public UserSettingsVO getUserSettings(Long userId) {
        try {
            // 查询用户所有设置
            List<ImUserSettings> settingsList = userSettingsMapper.selectByUserId(userId);

            // 分类设置
            UserSettingsVO vo = new UserSettingsVO();

            // 通用设置
            UserSettingsVO.GeneralSettings general = new UserSettingsVO.GeneralSettings();
            vo.setGeneral(general);

            // 通知设置
            UserSettingsVO.NotificationSettings notification = new UserSettingsVO.NotificationSettings();
            vo.setNotification(notification);

            // 填充通用设置
            Map<String, String> generalMap = settingsList.stream()
                    .filter(s -> "general".equals(s.getSettingType()))
                    .collect(Collectors.toMap(ImUserSettings::getSettingKey, ImUserSettings::getSettingValue));

            general.setTheme(generalMap.getOrDefault("theme", "light"));
            general.setLanguage(generalMap.getOrDefault("language", "zh-CN"));
            general.setTimeFormat(generalMap.getOrDefault("timeFormat", "24h"));
            general.setAutoStart(Boolean.parseBoolean(generalMap.getOrDefault("autoStart", "false")));
            general.setMinimizeToTray(Boolean.parseBoolean(generalMap.getOrDefault("minimizeToTray", "true")));

            return vo;
        } catch (Exception e) {
            log.error("获取用户设置失败: userId={}", userId, e);
            return new UserSettingsVO();
        }
    }

    @Override
    public List<ImUserSettings> getSettingsByType(Long userId, String settingType) {
        try {
            return userSettingsMapper.selectByUserIdAndType(userId, settingType);
        } catch (Exception e) {
            log.error("获取用户设置失败: userId={}, type={}", userId, settingType, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetting(Long userId, UserSettingsUpdateRequest request) {
        try {
            // 查询现有设置
            ImUserSettings existing = userSettingsMapper.selectByUserIdAndKey(userId, request.getSettingKey());

            if (existing != null) {
                // 更新现有设置
                existing.setSettingValue(request.getSettingValue());
                userSettingsMapper.updateById(existing);
                log.info("更新用户设置: userId={}, key={}", userId, request.getSettingKey());
            } else {
                // 插入新设置
                ImUserSettings newSetting = new ImUserSettings();
                newSetting.setUserId(userId);
                newSetting.setSettingKey(request.getSettingKey());
                newSetting.setSettingValue(request.getSettingValue());
                newSetting.setSettingType(inferSettingType(request.getSettingKey()));
                userSettingsMapper.insert(newSetting);
                log.info("创建用户设置: userId={}, key={}", userId, request.getSettingKey());
            }
        } catch (Exception e) {
            log.error("更新用户设置失败: userId={}, key={}", userId, request.getSettingKey(), e);
            throw new RuntimeException("更新用户设置失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSettings(Long userId, UserSettingsBatchUpdateRequest request) {
        try {
            if (request.getSettings() == null || request.getSettings().isEmpty()) {
                return;
            }

            List<ImUserSettings> settingsList = new ArrayList<>();

            for (UserSettingsBatchUpdateRequest.SettingItem item : request.getSettings()) {
                // 查询现有设置
                ImUserSettings existing = userSettingsMapper.selectByUserIdAndKey(userId, item.getKey());

                if (existing != null) {
                    existing.setSettingValue(item.getValue());
                    settingsList.add(existing);
                } else {
                    ImUserSettings newSetting = new ImUserSettings();
                    newSetting.setUserId(userId);
                    newSetting.setSettingKey(item.getKey());
                    newSetting.setSettingValue(item.getValue());
                    newSetting.setSettingType(inferSettingType(item.getKey()));
                    settingsList.add(newSetting);
                }
            }

            // 批量更新
            if (!settingsList.isEmpty()) {
                userSettingsMapper.updateSettings(settingsList);
                log.info("批量更新用户设置: userId={}, count={}", userId, settingsList.size());
            }
        } catch (Exception e) {
            log.error("批量更新用户设置失败: userId={}", userId, e);
            throw new RuntimeException("批量更新用户设置失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSetting(Long userId, String settingKey) {
        try {
            userSettingsMapper.deleteByUserIdAndKey(userId, settingKey);
            log.info("删除用户设置: userId={}, key={}", userId, settingKey);
        } catch (Exception e) {
            log.error("删除用户设置失败: userId={}, key={}", userId, settingKey, e);
            throw new RuntimeException("删除用户设置失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultSettings(Long userId) {
        try {
            // 检查是否已有设置
            List<ImUserSettings> existingSettings = userSettingsMapper.selectByUserId(userId);
            if (existingSettings != null && !existingSettings.isEmpty()) {
                log.info("用户已有设置，跳过初始化: userId={}", userId);
                return;
            }

            // 创建默认设置
            List<ImUserSettings> defaultSettings = new ArrayList<>();

            // 通用设置默认值
            String[][] generalDefaults = {
                    {"theme", "light", "general"},
                    {"language", "zh-CN", "general"},
                    {"timeFormat", "24h", "general"},
                    {"autoStart", "false", "general"},
                    {"minimizeToTray", "true", "general"}
            };

            for (String[] defaults : generalDefaults) {
                ImUserSettings setting = new ImUserSettings();
                setting.setUserId(userId);
                setting.setSettingKey(defaults[0]);
                setting.setSettingValue(defaults[1]);
                setting.setSettingType(defaults[2]);
                defaultSettings.add(setting);
            }

            // 批量插入
            for (ImUserSettings setting : defaultSettings) {
                userSettingsMapper.insert(setting);
            }

            log.info("初始化用户默认设置: userId={}, count={}", userId, defaultSettings.size());
        } catch (Exception e) {
            log.error("初始化用户默认设置失败: userId={}", userId, e);
            throw new RuntimeException("初始化用户默认设置失败", e);
        }
    }

    /**
     * 根据设置键推断设置类型
     */
    private String inferSettingType(String settingKey) {
        if ("theme".equals(settingKey) || "language".equals(settingKey) ||
            "timeFormat".equals(settingKey) || "autoStart".equals(settingKey) ||
            "minimizeToTray".equals(settingKey)) {
            return "general";
        } else if ("desktopNotification".equals(settingKey) || "soundEnabled".equals(settingKey) ||
            "soundType".equals(settingKey) || "showPreview".equals(settingKey) ||
            "dndEnabled".equals(settingKey) || "dndStartTime".equals(settingKey) ||
            "dndEndTime".equals(settingKey) || "mentionOnly".equals(settingKey)) {
            return "notification";
        } else if ("twoFactorEnabled".equals(settingKey)) {
            return "security";
        } else if ("keepOnLogout".equals(settingKey)) {
            return "storage";
        }
        return "general";
    }
}
