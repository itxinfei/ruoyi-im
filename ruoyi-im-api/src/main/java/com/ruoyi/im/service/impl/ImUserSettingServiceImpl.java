package com.ruoyi.im.service.impl;

import com.ruoyi.im.constants.StatusConstants;
import com.ruoyi.im.domain.ImUserSetting;
import com.ruoyi.im.dto.setting.UserSettingUpdateRequest;
import com.ruoyi.im.dto.setting.UserSettingsBatchUpdateRequest;
import com.ruoyi.im.mapper.ImUserSettingMapper;
import com.ruoyi.im.service.IImUserSettingService;
import com.ruoyi.im.vo.setting.UserSettingVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户设置服务实现
 *
 * @author ruoyi
 */
@Service
public class ImUserSettingServiceImpl implements IImUserSettingService {

    private static final Logger logger = LoggerFactory.getLogger(ImUserSettingServiceImpl.class);

    private final ImUserSettingMapper userSettingMapper;

    public ImUserSettingServiceImpl(ImUserSettingMapper userSettingMapper) {
        this.userSettingMapper = userSettingMapper;
    }

    @Override
    public List<UserSettingVO> getUserSettings(Long userId) {
        List<ImUserSetting> settings = userSettingMapper.selectByUserId(userId);
        return settings.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSettingVO> getUserSettingsByType(Long userId, String settingType) {
        List<ImUserSetting> settings = userSettingMapper.selectByUserIdAndType(userId, settingType);
        return settings.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public String getSettingValue(Long userId, String settingKey) {
        ImUserSetting setting = userSettingMapper.selectByUserIdAndKey(userId, settingKey);
        return setting != null ? setting.getSettingValue() : null;
    }

    @Override
    public Map<String, String> getUserSettingsMap(Long userId) {
        List<ImUserSetting> settings = userSettingMapper.selectByUserId(userId);
        return settings.stream()
                .collect(Collectors.toMap(
                        ImUserSetting::getSettingKey,
                        ImUserSetting::getSettingValue,
                        (v1, v2) -> v1
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetting(Long userId, UserSettingUpdateRequest request) {
        ImUserSetting existing = userSettingMapper.selectByUserIdAndKey(userId, request.getSettingKey());

        if (existing != null) {
            // 更新已存在的设置
            existing.setSettingValue(request.getSettingValue());
            if (request.getSettingType() != null) {
                existing.setSettingType(request.getSettingType());
            }
            existing.setUpdateTime(LocalDateTime.now());
            userSettingMapper.updateImUserSetting(existing);
            logger.info("更新用户设置: userId={}, key={}, value={}", userId, request.getSettingKey(), request.getSettingValue());
        } else {
            // 新增设置
            ImUserSetting newSetting = new ImUserSetting();
            newSetting.setUserId(userId);
            newSetting.setSettingKey(request.getSettingKey());
            newSetting.setSettingValue(request.getSettingValue());
            newSetting.setSettingType(request.getSettingType() != null ? request.getSettingType() : inferTypeFromKey(request.getSettingKey()));
            newSetting.setCreateTime(LocalDateTime.now());
            newSetting.setUpdateTime(LocalDateTime.now());
            userSettingMapper.insertImUserSetting(newSetting);
            logger.info("新增用户设置: userId={}, key={}, value={}", userId, request.getSettingKey(), request.getSettingValue());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSettings(Long userId, UserSettingsBatchUpdateRequest request) {
        if (request.getSettings() == null || request.getSettings().isEmpty()) {
            return;
        }

        List<ImUserSetting> toInsert = new ArrayList<>();
        List<ImUserSetting> toUpdate = new ArrayList<>();

        for (Map.Entry<String, String> entry : request.getSettings().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            ImUserSetting existing = userSettingMapper.selectByUserIdAndKey(userId, key);
            if (existing != null) {
                existing.setSettingValue(value);
                existing.setUpdateTime(LocalDateTime.now());
                toUpdate.add(existing);
            } else {
                ImUserSetting newSetting = new ImUserSetting();
                newSetting.setUserId(userId);
                newSetting.setSettingKey(key);
                newSetting.setSettingValue(value);
                newSetting.setSettingType(inferTypeFromKey(key));
                newSetting.setCreateTime(LocalDateTime.now());
                newSetting.setUpdateTime(LocalDateTime.now());
                toInsert.add(newSetting);
            }
        }

        for (ImUserSetting setting : toUpdate) {
            userSettingMapper.updateImUserSetting(setting);
        }

        if (!toInsert.isEmpty()) {
            userSettingMapper.batchInsertImUserSetting(toInsert);
        }

        logger.info("批量更新用户设置: userId={}, 更新{}条, 新增{}条", userId, toUpdate.size(), toInsert.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSetting(Long userId, String settingKey) {
        ImUserSetting existing = userSettingMapper.selectByUserIdAndKey(userId, settingKey);
        if (existing != null) {
            userSettingMapper.deleteImUserSettingById(existing.getId());
            logger.info("删除用户设置: userId={}, key={}", userId, settingKey);
        }
    }

    /**
     * 根据设置键名推断设置类型
     *
     * @param key 设置键名
     * @return 设置类型
     */
    private String inferTypeFromKey(String key) {
        if (key.startsWith("chat.")) {
            return "CHAT";
        } else if (key.startsWith("notifications.") || key.startsWith("notification.")) {
            return "NOTIFICATION";
        } else if (key.startsWith("privacy.")) {
            return "PRIVACY";
        } else if (key.startsWith("file.")) {
            return StatusConstants.CloudResourceType.FILE;
        } else if (key.startsWith("data.")) {
            return "DATA";
        } else {
            return "GENERAL";
        }
    }

    /**
     * 转换为VO对象
     *
     * @param setting 设置实体
     * @return 设置VO
     */
    private UserSettingVO convertToVO(ImUserSetting setting) {
        UserSettingVO vo = new UserSettingVO();
        BeanUtils.copyProperties(setting, vo);
        return vo;
    }
}
