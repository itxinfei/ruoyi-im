package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImUserSetting;
import com.ruoyi.web.mapper.ImUserSettingMapper;
import com.ruoyi.web.service.ImUserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户设置Service实现
 *
 * @author ruoyi
 */
@Service
public class ImUserSettingServiceImpl implements ImUserSettingService {

    @Autowired
    private ImUserSettingMapper imUserSettingMapper;

    @Override
    public ImUserSetting selectImUserSettingById(Long id) {
        return imUserSettingMapper.selectImUserSettingById(id);
    }

    @Override
    public ImUserSetting selectByUserIdAndKey(Long userId, String settingKey) {
        return imUserSettingMapper.selectByUserIdAndKey(userId, settingKey);
    }

    @Override
    public List<ImUserSetting> selectImUserSettingList(ImUserSetting imUserSetting) {
        return imUserSettingMapper.selectImUserSettingList(imUserSetting);
    }

    @Override
    public List<ImUserSetting> selectSettingsByUserId(Long userId) {
        return imUserSettingMapper.selectSettingsByUserId(userId);
    }

    @Override
    public List<ImUserSetting> selectSettingsByType(Long userId, String settingType) {
        return imUserSettingMapper.selectSettingsByType(userId, settingType);
    }

    @Override
    public int insertImUserSetting(ImUserSetting imUserSetting) {
        return imUserSettingMapper.insertImUserSetting(imUserSetting);
    }

    @Override
    public int updateImUserSetting(ImUserSetting imUserSetting) {
        return imUserSettingMapper.updateImUserSetting(imUserSetting);
    }

    @Override
    public int deleteImUserSettingByIds(Long[] ids) {
        return imUserSettingMapper.deleteImUserSettingByIds(ids);
    }

    @Override
    public int deleteImUserSettingById(Long id) {
        return imUserSettingMapper.deleteImUserSettingById(id);
    }

    @Override
    public int deleteSettingsByUserId(Long userId) {
        return imUserSettingMapper.deleteSettingsByUserId(userId);
    }

    @Override
    public int resetUserSettings(Long userId) {
        return imUserSettingMapper.resetUserSettings(userId);
    }

    @Override
    public Map<String, Object> getSettingStatistics() {
        return imUserSettingMapper.getSettingStatistics();
    }

    @Override
    public String getSettingValue(Long userId, String settingKey, String defaultValue) {
        ImUserSetting setting = selectByUserIdAndKey(userId, settingKey);
        if (setting != null && setting.getSettingValue() != null) {
            return setting.getSettingValue();
        }
        return defaultValue;
    }

    @Override
    public int updateSettingValue(Long userId, String settingKey, String settingValue) {
        ImUserSetting setting = new ImUserSetting();
        setting.setUserId(userId);
        setting.setSettingKey(settingKey);
        setting.setSettingValue(settingValue);

        ImUserSetting existing = selectByUserIdAndKey(userId, settingKey);
        if (existing != null) {
            setting.setId(existing.getId());
            return updateImUserSetting(setting);
        } else {
            return insertImUserSetting(setting);
        }
    }
}
