package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImUserSettings;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户设置 Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImUserSettingsMapper extends BaseMapper<ImUserSettings> {

    /**
     * 根据用户ID查询设置列表
     *
     * @param userId 用户ID
     * @return 设置列表
     */
    List<ImUserSettings> selectByUserId(Long userId);

    /**
     * 根据用户ID和设置类型查询设置列表
     *
     * @param userId 用户ID
     * @param settingType 设置类型
     * @return 设置列表
     */
    List<ImUserSettings> selectByUserIdAndType(Long userId, String settingType);

    /**
     * 根据用户ID和设置键查询设置
     *
     * @param userId 用户ID
     * @param settingKey 设置键
     * @return 设置对象
     */
    ImUserSettings selectByUserIdAndKey(Long userId, String settingKey);

    /**
     * 更新设置
     *
     * @param settings 设置对象
     * @return 更新行数
     */
    int updateSettings(List<ImUserSettings> settings);

    /**
     * 删除用户设置
     *
     * @param userId 用户ID
     * @param settingKey 设置键
     * @return 删除行数
     */
    int deleteByUserIdAndKey(Long userId, String settingKey);
}
