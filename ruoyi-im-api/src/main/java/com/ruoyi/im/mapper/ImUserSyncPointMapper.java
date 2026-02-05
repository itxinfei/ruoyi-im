package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImUserSyncPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户消息同步点 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImUserSyncPointMapper extends BaseMapper<ImUserSyncPoint> {

    /**
     * 根据用户ID和设备ID查询同步点
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 同步点实体
     */
    @Select("SELECT * FROM im_user_sync_point WHERE user_id = #{userId} AND device_id = #{deviceId}")
    ImUserSyncPoint selectByUserAndDevice(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 更新同步点
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param lastSyncTime 最后同步时间
     * @param lastMessageId 最后消息ID
     * @return 影响行数
     */
    @Update("UPDATE im_user_sync_point SET last_sync_time = #{lastSyncTime}, last_message_id = #{lastMessageId} " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId}")
    int updateSyncPoint(@Param("userId") Long userId, 
                       @Param("deviceId") String deviceId,
                       @Param("lastSyncTime") Long lastSyncTime,
                       @Param("lastMessageId") Long lastMessageId);

    /**
     * 查询用户的所有同步点
     *
     * @param userId 用户ID
     * @return 同步点列表
     */
    @Select("SELECT * FROM im_user_sync_point WHERE user_id = #{userId} ORDER BY update_time DESC")
    java.util.List<ImUserSyncPoint> selectByUserId(@Param("userId") Long userId);
}
