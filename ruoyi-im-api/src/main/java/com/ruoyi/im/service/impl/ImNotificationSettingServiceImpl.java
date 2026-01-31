package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImNotificationSetting;
import com.ruoyi.im.mapper.ImNotificationSettingMapper;
import com.ruoyi.im.service.ImNotificationSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户通知设置服务实现
 *
 * @author ruoyi
 */
@Service
public class ImNotificationSettingServiceImpl extends ServiceImpl<ImNotificationSettingMapper, ImNotificationSetting>
        implements ImNotificationSettingService {

    private static final Logger log = LoggerFactory.getLogger(ImNotificationSettingServiceImpl.class);

    private final ImNotificationSettingMapper notificationSettingMapper;

    public ImNotificationSettingServiceImpl(ImNotificationSettingMapper notificationSettingMapper) {
        this.notificationSettingMapper = notificationSettingMapper;
    }

    @Override
    public ImNotificationSetting getNotificationSetting(Long userId) {
        try {
            ImNotificationSetting setting = notificationSettingMapper.selectByUserId(userId);
            if (setting == null) {
                log.warn("用户通知设置不存在: userId={}", userId);
                return createDefaultSetting(userId);
            }
            return setting;
        } catch (Exception e) {
            log.error("获取用户通知设置失败: userId={}", userId, e);
            return createDefaultSetting(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotificationSetting(Long userId, ImNotificationSetting setting) {
        try {
            setting.setUserId(userId);
            notificationSettingMapper.insertOrUpdate(setting);
            log.info("更新用户通知设置: userId={}", userId);
        } catch (Exception e) {
            log.error("更新用户通知设置失败: userId={}", userId, e);
            throw new RuntimeException("更新用户通知设置失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultNotificationSetting(Long userId) {
        try {
            ImNotificationSetting defaultSetting = createDefaultSetting(userId);
            notificationSettingMapper.insertOrUpdate(defaultSetting);
            log.info("初始化默认通知设置: userId={}", userId);
        } catch (Exception e) {
            log.error("初始化默认通知设置失败: userId={}", userId, e);
            throw new RuntimeException("初始化默认通知设置失败", e);
        }
    }

    /**
     * 创建默认通知设置
     */
    private ImNotificationSetting createDefaultSetting(Long userId) {
        ImNotificationSetting setting = new ImNotificationSetting();
        setting.setUserId(userId);
        setting.setEnabled(1);
        setting.setDesktopNotification(1);
        setting.setSoundEnabled(1);
        setting.setSoundType("default");
        setting.setCustomSoundUrl(null);
        setting.setShowPreview(1);
        setting.setDndEnabled(0);
        setting.setDndStartTime("22:00");
        setting.setDndEndTime("08:00");
        setting.setMentionOnly(0);
        return setting;
    }
}
