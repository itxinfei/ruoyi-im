package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImScheduledMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时消息Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImScheduledMessageMapper extends BaseMapper<ImScheduledMessage> {

    /**
     * 查询待发送的定时消息
     *
     * @param currentTime 当前时间
     * @return 待发送消息列表
     */
    @Select("SELECT * FROM im_scheduled_message " +
            "WHERE status = 'PENDING' " +
            "AND scheduled_time <= #{currentTime} " +
            "ORDER BY scheduled_time ASC " +
            "LIMIT 100")
    List<ImScheduledMessage> selectPendingMessages(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询用户的定时消息列表
     *
     * @param userId 用户ID
     * @return 消息列表
     */
    @Select("SELECT * FROM im_scheduled_message " +
            "WHERE user_id = #{userId} " +
            "AND status IN ('PENDING', 'SENT', 'FAILED') " +
            "ORDER BY scheduled_time DESC")
    List<ImScheduledMessage> selectByUserId(@Param("userId") Long userId);
}
