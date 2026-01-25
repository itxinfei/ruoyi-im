package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImMessageMarker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息标记Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImMessageMarkerMapper extends BaseMapper<ImMessageMarker> {

    /**
     * 查询用户的标记列表
     *
     * @param userId     用户ID
     * @param markerType 标记类型
     * @return 标记列表
     */
    List<ImMessageMarker> selectUserMarkers(@Param("userId") Long userId,
                                           @Param("markerType") String markerType);

    /**
     * 查询消息的标记列表
     *
     * @param messageId 消息ID
     * @return 标记列表
     */
    List<ImMessageMarker> selectByMessageId(@Param("messageId") Long messageId);

    /**
     * 查询用户对消息的标记
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 标记
     */
    ImMessageMarker selectByMessageAndUser(@Param("messageId") Long messageId,
                                          @Param("userId") Long userId);

    /**
     * 查询待提醒的标记
     *
     * @param beforeTime 时间点之前
     * @return 标记列表
     */
    List<ImMessageMarker> selectPendingReminders(@Param("beforeTime") java.time.LocalDateTime beforeTime);

    /**
     * 统计用户待办数量
     *
     * @param userId 用户ID
     * @return 待办数量
     */
    Integer countUserTodos(@Param("userId") Long userId);
}
