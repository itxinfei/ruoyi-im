package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImUserSetting;

import java.util.List;

/**
 * 用户设置Mapper接口
 *
 * @author ruoyi
 */
public interface ImUserSettingMapper {

    /**
     * 查询用户设置
     *
     * @param id 设置ID
     * @return 用户设置
     */
    ImUserSetting selectImUserSettingById(Long id);

    /**
     * 根据用户ID和设置键名查询设置
     *
     * @param userId      用户ID
     * @param settingKey 设置键名
     * @return 用户设置
     */
    ImUserSetting selectByUserIdAndKey(Long userId, String settingKey);

    /**
     * 查询用户设置列表
     *
     * @param imUserSetting 用户设置
     * @return 用户设置集合
     */
    List<ImUserSetting> selectImUserSettingList(ImUserSetting imUserSetting);

    /**
     * 根据用户ID查询所有设置
     *
     * @param userId 用户ID
     * @return 用户设置集合
     */
    List<ImUserSetting> selectByUserId(Long userId);

    /**
     * 根据用户ID和设置类型查询设置列表
     *
     * @param userId      用户ID
     * @param settingType 设置类型
     * @return 用户设置集合
     */
    List<ImUserSetting> selectByUserIdAndType(Long userId, String settingType);

    /**
     * 新增用户设置
     *
     * @param imUserSetting 用户设置
     * @return 结果
     */
    int insertImUserSetting(ImUserSetting imUserSetting);

    /**
     * 批量新增用户设置
     *
     * @param list 用户设置集合
     * @return 结果
     */
    int batchInsertImUserSetting(List<ImUserSetting> list);

    /**
     * 修改用户设置
     *
     * @param imUserSetting 用户设置
     * @return 结果
     */
    int updateImUserSetting(ImUserSetting imUserSetting);

    /**
     * 删除用户设置
     *
     * @param id 设置ID
     * @return 结果
     */
    int deleteImUserSettingById(Long id);

    /**
     * 根据用户ID删除所有设置
     *
     * @param userId 用户ID
     * @return 结果
     */
    int deleteImUserSettingByUserId(Long userId);
}
