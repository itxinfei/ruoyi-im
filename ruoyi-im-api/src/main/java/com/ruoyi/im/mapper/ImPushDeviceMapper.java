package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImPushDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推送设备Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImPushDeviceMapper extends BaseMapper<ImPushDevice> {

    /**
     * 获取用户的激活设备列表
     *
     * @param userId 用户ID
     * @return 设备列表
     */
    @Select("SELECT * FROM im_push_device " +
            "WHERE user_id = #{userId} AND is_active = 1 " +
            "ORDER BY last_active_time DESC")
    List<ImPushDevice> selectActiveDevicesByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的活跃设备
     *
     * @param userId 用户ID
     * @param lastActiveTime 活跃时间阈值
     * @return 设备列表
     */
    @Select("SELECT * FROM im_push_device " +
            "WHERE user_id = #{userId} " +
            "AND is_active = 1 " +
            "AND last_active_time >= #{lastActiveTime}")
    List<ImPushDevice> selectRecentlyActiveDevices(@Param("userId") Long userId,
                                                   @Param("lastActiveTime") LocalDateTime lastActiveTime);

    /**
     * 根据设备Token查询
     *
     * @param deviceToken 设备Token
     * @return 设备信息
     */
    @Select("SELECT * FROM im_push_device " +
            "WHERE device_token = #{deviceToken} " +
            "LIMIT 1")
    ImPushDevice selectByDeviceToken(@Param("deviceToken") String deviceToken);
}
