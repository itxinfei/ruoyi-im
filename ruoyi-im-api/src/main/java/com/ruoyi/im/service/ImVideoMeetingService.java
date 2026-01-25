package com.ruoyi.im.service;

import com.ruoyi.im.dto.meeting.ImVideoMeetingCreateRequest;
import com.ruoyi.im.dto.meeting.ImVideoMeetingUpdateRequest;
import com.ruoyi.im.vo.meeting.ImVideoMeetingDetailVO;
import com.ruoyi.im.vo.meeting.ImVideoMeetingVO;

import java.util.List;

/**
 * 视频会议服务接口
 *
 * @author ruoyi
 */
public interface ImVideoMeetingService {

    /**
     * 创建会议
     *
     * @param request 创建请求
     * @param userId  当前用户ID
     * @return 会议ID
     */
    Long createMeeting(ImVideoMeetingCreateRequest request, Long userId);

    /**
     * 更新会议
     *
     * @param meetingId 会议ID
     * @param request  更新请求
     * @param userId   当前用户ID
     */
    void updateMeeting(Long meetingId, ImVideoMeetingUpdateRequest request, Long userId);

    /**
     * 取消会议
     *
     * @param meetingId 会议ID
     * @param userId    当前用户ID
     */
    void cancelMeeting(Long meetingId, Long userId);

    /**
     * 删除会议
     *
     * @param meetingId 会议ID
     * @param userId    当前用户ID
     */
    void deleteMeeting(Long meetingId, Long userId);

    /**
     * 开始会议
     *
     * @param meetingId 会议ID
     * @param userId    当前用户ID
     */
    void startMeeting(Long meetingId, Long userId);

    /**
     * 结束会议
     *
     * @param meetingId 会议ID
     * @param userId    当前用户ID
     */
    void endMeeting(Long meetingId, Long userId);

    /**
     * 加入会议
     *
     * @param meetingId   会议ID
     * @param password     会议密码（可选）
     * @param userId      用户ID
     * @return 会议详情
     */
    ImVideoMeetingDetailVO joinMeeting(Long meetingId, String password, Long userId);

    /**
     * 离开会议
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     */
    void leaveMeeting(Long meetingId, Long userId);

    /**
     * 获取会议详情
     *
     * @param meetingId 会议ID
     * @return 会议详情
     */
    ImVideoMeetingDetailVO getMeetingDetail(Long meetingId);

    /**
     * 获取用户的会议列表
     *
     * @param userId    用户ID
     * @param status    会议状态（可选）
     * @return 会议列表
     */
    List<ImVideoMeetingVO> getUserMeetings(Long userId, String status);

    /**
     * 获取会议参与者列表
     *
     * @param meetingId 会议ID
     * @return 参与者列表
     */
    List<?> getMeetingParticipants(Long meetingId);

    /**
     * 邀请用户加入会议
     *
     * @param meetingId 会议ID
     * @param userIds   被邀请的用户ID列表
     * @param inviterId 邀请人ID
     */
    void inviteUsers(Long meetingId, List<Long> userIds, Long inviterId);

    /**
     * 移除参与者
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     * @param operatorId 操作者ID
     */
    void removeParticipant(Long meetingId, Long userId, Long operatorId);

    /**
     * 静音/取消静音用户
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     * @param muted     是否静音
     */
    void muteParticipant(Long meetingId, Long userId, Boolean muted);

    /**
     * 开始屏幕共享
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     */
    void startScreenShare(Long meetingId, Long userId);

    /**
     * 停止屏幕共享
     *
     * @param meetingId 会议ID
     * @param userId    用户ID
     */
    void stopScreenShare(Long meetingId, Long userId);

    /**
     * 设置会议主持人
     *
     * @param meetingId  会议ID
     * @param userId     新主持人ID
     @param operatorId 操作者ID
     */
    void changeHost(Long meetingId, Long userId, Long operatorId);
}
