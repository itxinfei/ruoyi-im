package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImNotificationSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知设置 Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImNotificationSettingMapper extends BaseMapper<ImNotificationSetting> {

    /**
     * 根据用户ID查询通知设置
     *
     * @param userId 用户ID
     * @return 通知设置对象
     */
    ImNotificationSetting selectByUserId(Long userId);

    /**
     * 插入或更新通知设置
     *
     * @param setting 通知设置对象
     * @return 影响行数
     */
    int insertOrUpdate(ImNotificationSetting setting);
}
