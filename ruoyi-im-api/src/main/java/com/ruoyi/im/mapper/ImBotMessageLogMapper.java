package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImBotMessageLog;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 机器人消息日志Mapper接口
 *
 * @author ruoyi
 */
public interface ImBotMessageLogMapper extends BaseMapper<ImBotMessageLog> {

    /**
     * 根据机器人ID查询日志列表
     *
     * @param botId 机器人ID
     * @return 日志列表
     */
    List<ImBotMessageLog> selectByBotId(@Param("botId") Long botId);

    /**
     * 根据群组ID查询日志列表
     *
     * @param groupId 群组ID
     * @return 日志列表
     */
    List<ImBotMessageLog> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据时间范围查询日志列表
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志列表
     */
    List<ImBotMessageLog> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 统计机器人的消息数量
     *
     * @param botId 机器人ID
     * @return 消息数量
     */
    int countByBotId(@Param("botId") Long botId);

    /**
     * 删除指定时间之前的日志
     *
     * @param beforeTime 指定时间
     * @return 影响行数
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);
}
