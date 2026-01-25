package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImVideoMeetingParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频会议参与者Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImVideoMeetingParticipantMapper extends BaseMapper<ImVideoMeetingParticipant> {

    /**
     * 查询会议的参与者列表
     *
     * @param meetingId 会议ID
     * @return 参与者列表
     */
    List<ImVideoMeetingParticipant> selectByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 查询用户在某会议中的参与信息
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     * @return 参与者信息
     */
    ImVideoMeetingParticipant selectByMeetingAndUser(@Param("meetingId") Long meetingId, @Param("userId") Long userId);

    /**
     * 统计会议已加入的参与者数量
     *
     * @param meetingId 会议ID
     * @return 参与者数量
     */
    Integer countJoinedByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 查询正在共享屏幕的参与者
     *
     * @param meetingId 会议ID
     * @return 正在共享屏幕的参与者
     */
    ImVideoMeetingParticipant selectScreenSharer(@Param("meetingId") Long meetingId);

    /**
     * 更新参与者的屏幕共享状态
     *
     * @param id    参与者ID
     * @param status 共享状态
     * @return 更新行数
     */
    int updateScreenShareStatus(@Param("id") Long id, @Param("status") Boolean status);

    /**
     * 批量插入参与者
     *
     * @param participants 参与者列表
     * @return 插入行数
     */
    int batchInsert(@Param("participants") List<ImVideoMeetingParticipant> participants);
}
