package com.ruoyi.im.service;

import java.util.List;

/**
 * 视频通话服务接口
 *
 * @author ruoyi
 */
public interface ImVideoCallService {

    /**
     * 发起单聊通话
     *
     * @param callerId 发起者ID
     * @param calleeId 接收者ID
     * @param conversationId 会话ID
     * @param callType 通话类型 VIDEO/VOICE
     * @return 通话ID
     */
    Long initiateCall(Long callerId, Long calleeId, Long conversationId, String callType);

    /**
     * 发起群组多人通话
     *
     * @param callerId 发起者ID
     * @param conversationId 群组会话ID
     * @param callType 通话类型 VIDEO/VOICE
     * @param maxParticipants 最大参与者数（最多9人）
     * @param invitedUserIds 被邀请的用户ID列表
     * @return 通话ID
     */
    Long initiateGroupCall(Long callerId, Long conversationId, String callType,
                           Integer maxParticipants, List<Long> invitedUserIds);

    /**
     * 接听通话
     *
     * @param callId 通话ID
     * @param userId 接听用户ID
     */
    void acceptCall(Long callId, Long userId);

    /**
     * 拒绝通话
     *
     * @param callId 通话ID
     * @param userId 用户ID
     * @param reason 拒绝原因
     */
    void rejectCall(Long callId, Long userId, String reason);

    /**
     * 结束通话
     *
     * @param callId 通话ID
     * @param userId 用户ID
     */
    void endCall(Long callId, Long userId);

    /**
     * 加入群组通话
     *
     * @param callId 通话ID
     * @param userId 用户ID
     */
    void joinGroupCall(Long callId, Long userId);

    /**
     * 离开群组通话
     *
     * @param callId 通话ID
     * @param userId 用户ID
     */
    void leaveGroupCall(Long callId, Long userId);

    /**
     * 切换麦克风状态
     *
     * @param callId 通话ID
     * @param userId 用户ID
     * @param muted 是否静音
     */
    void toggleMute(Long callId, Long userId, Boolean muted);

    /**
     * 切换摄像头状态
     *
     * @param callId 通话ID
     * @param userId 用户ID
     * @param cameraOff 是否关闭摄像头
     */
    void toggleCamera(Long callId, Long userId, Boolean cameraOff);

    /**
     * 获取通话信息
     *
     * @param callId 通话ID
     * @return 通话信息
     */
    Object getCallInfo(Long callId);

    /**
     * 获取用户当前通话状态
     *
     * @param userId 用户ID
     * @return 通话信息（无通话返回null）
     */
    Object getUserActiveCall(Long userId);

    /**
     * 获取通话参与者列表
     *
     * @param callId 通话ID
     * @return 参与者列表
     */
    List<?> getCallParticipants(Long callId);

    /**
     * 超时自动结束通话
     *
     * @param callId 通话ID
     */
    void timeoutCall(Long callId);

    /**
     * 获取用户通话历史
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 通话历史列表
     */
    List<?> getCallHistory(Long userId, Integer limit);
}
