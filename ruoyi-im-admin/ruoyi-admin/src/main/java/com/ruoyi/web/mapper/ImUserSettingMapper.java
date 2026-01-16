package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImUserSetting;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 用户设置Mapper接口
 *
 * @author ruoyi
 */
public interface ImUserSettingMapper {

    /**
     * 查询用户设置
     *
     * @param id 用户设置主键
     * @return 用户设置
     */
    ImUserSetting selectImUserSettingById(Long id);

    /**
     * 根据用户ID和设置键查询设置
     *
     * @param userId 用户ID
     * @param settingKey 设置键
     * @return 用户设置
     */
    ImUserSetting selectByUserIdAndKey(@Param("userId") Long userId, @Param("settingKey") String settingKey);

    /**
     * 查询用户设置列表
     *
     * @param imUserSetting 用户设置
     * @return 用户设置集合
     */
    List<ImUserSetting> selectImUserSettingList(ImUserSetting imUserSetting);

    /**
     * 查询用户的所有设置
     *
     * @param userId 用户ID
     * @return 设置列表
     */
    List<ImUserSetting> selectSettingsByUserId(Long userId);

    /**
     * 查询指定类型的设置
     *
     * @param userId 用户ID
     * @param settingType 设置类型
     * @return 设置列表
     */
    List<ImUserSetting> selectSettingsByType(@Param("userId") Long userId, @Param("settingType") String settingType);

    /**
     * 新增用户设置
     *
     * @param imUserSetting 用户设置
     * @return 结果
     */
    int insertImUserSetting(ImUserSetting imUserSetting);

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
     * @param id 用户设置主键
     * @return 结果
     */
    int deleteImUserSettingById(Long id);

    /**
     * 批量删除用户设置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImUserSettingByIds(Long[] ids);

    /**
     * 批量插入或更新用户设置
     *
     * @param settings 设置列表
     * @return 结果
     */
    int batchInsertOrUpdateSettings(@Param("settings") List<ImUserSetting> settings);

    /**
     * 删除用户的所有设置
     *
     * @param userId 用户ID
     * @return 结果
     */
    int deleteSettingsByUserId(Long userId);

    /**
     * 获取设置统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getSettingStatistics();

    /**
     * 重置用户设置为默认值
     *
     * @param userId 用户ID
     * @return 结果
     */
    int resetUserSettings(Long userId);
}
