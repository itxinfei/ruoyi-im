package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImUserDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户设备 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImUserDeviceMapper extends BaseMapper<ImUserDevice> {

    /**
     * 根据用户ID和设备ID查询设备
     *
     * @param userId    用户ID
     * @param deviceId 设备ID
     * @return 设备实体
     */
    @Select("SELECT * FROM im_user_device WHERE user_id = #{userId} AND device_id = #{deviceId}")
    ImUserDevice selectByUserAndDevice(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 查询用户的所有在线设备
     *
     * @param userId 用户ID
     * @return 在线设备列表
     */
    @Select("SELECT * FROM im_user_device WHERE user_id = #{userId} AND online_status = 1 ORDER BY last_online_time DESC")
    List<ImUserDevice> selectOnlineDevicesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的所有设备
     *
     * @param userId 用户ID
     * @return 设备列表
     */
    @Select("SELECT * FROM im_user_device WHERE user_id = #{userId} ORDER BY last_online_time DESC")
    List<ImUserDevice> selectDevicesByUserId(@Param("userId") Long userId);

    /**
     * 更新设备在线状态
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param onlineStatus 在线状态
     * @return 影响行数
     */
    @Update("UPDATE im_user_device SET online_status = #{onlineStatus}, " +
            "last_online_time = NOW(), last_heartbeat_time = NOW() " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId}")
    int updateOnlineStatus(@Param("userId") Long userId,
                         @Param("deviceId") String deviceId,
                         @Param("onlineStatus") Integer onlineStatus);

    /**
     * 更新设备心跳时间
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 影响行数
     */
    @Update("UPDATE im_user_device SET last_heartbeat_time = NOW(), online_status = 1 " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId}")
    int updateHeartbeat(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 将用户的所有设备设为离线
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE im_user_device SET online_status = 0, is_active = 0 WHERE user_id = #{userId}")
    int setAllDevicesOffline(@Param("userId") Long userId);

    /**
     * 更新最后活跃时间
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 影响行数
     */
    @Update("UPDATE im_user_device SET last_heartbeat_time = NOW() " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId}")
    int updateLastHeartbeat(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 查询超时离线的设备
     *
     * @param timeoutMinutes 超时分钟数
     * @return 超时设备列表
     */
    @Select("SELECT * FROM im_user_device WHERE online_status = 1 " +
            "AND last_heartbeat_time < DATE_SUB(NOW(), INTERVAL #{timeoutMinutes} MINUTE)")
    List<ImUserDevice> selectTimeoutDevices(@Param("timeoutMinutes") int timeoutMinutes);
}
