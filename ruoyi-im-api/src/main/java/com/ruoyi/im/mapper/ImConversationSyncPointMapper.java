package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImConversationSyncPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 会话同步点 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImConversationSyncPointMapper extends BaseMapper<ImConversationSyncPoint> {

    /**
     * 根据用户ID和设备ID查询同步点
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 同步点实体
     */
    @Select("SELECT * FROM im_conversation_sync_point WHERE user_id = #{userId} AND device_id = #{deviceId}")
    ImConversationSyncPoint selectByUserAndDevice(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 查询用户的所有同步点
     *
     * @param userId 用户ID
     * @return 同步点列表
     */
    @Select("SELECT * FROM im_conversation_sync_point WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<ImConversationSyncPoint> selectByUserId(@Param("userId") Long userId);

    /**
     * 更新同步点
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param lastSyncTime 最后同步时间
     * @return 影响行数
     */
    @Update("UPDATE im_conversation_sync_point SET last_sync_time = #{lastSyncTime}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId}")
    int updateSyncPoint(@Param("userId") Long userId, @Param("deviceId") String deviceId, @Param("lastSyncTime") Long lastSyncTime);

    /**
     * 删除同步点
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 影响行数
     */
    int deleteByUserAndDevice(@Param("userId") Long userId, @Param("deviceId") String deviceId);
}
