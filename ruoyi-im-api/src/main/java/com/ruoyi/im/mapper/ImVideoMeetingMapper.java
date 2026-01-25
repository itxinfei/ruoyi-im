package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImVideoMeeting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频会议Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImVideoMeetingMapper extends BaseMapper<ImVideoMeeting> {

    /**
     * 查询用户的会议列表
     *
     * @param userId 用户ID
     * @param status 会议状态（可选）
     * @return 会议列表
     */
    List<ImVideoMeeting> selectUserMeetings(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询即将开始的会议
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 会议列表
     */
    List<ImVideoMeeting> selectUpcomingMeetings(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 检查用户是否在会议中
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     * @return 参与者记录
     */
    com.ruoyi.im.domain.ImVideoMeetingParticipant selectParticipant(@Param("meetingId") Long meetingId, @Param("userId") Long userId);

    /**
     * 更新会议状态
     *
     * @param meetingId 会议ID
     * @param status    会议状态
     * @return 更新行数
     */
    int updateStatus(@Param("meetingId") Long meetingId, @Param("status") String status);
}
