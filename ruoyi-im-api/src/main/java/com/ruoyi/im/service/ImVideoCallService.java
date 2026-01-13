package com.ruoyi.im.service;

import java.util.List;

/**
 * 视频通话服务接口
 *
 * @author ruoyi
 */
public interface ImVideoCallService {

    /**
     * 发起通话
     *
     * @param callerId 发起者ID
     * @param calleeId 接收者ID
     * @param conversationId 会话ID
     * @param callType 通话类型 VIDEO/VOICE
     * @return 通话ID
     */
    Long initiateCall(Long callerId, Long calleeId, Long conversationId, String callType);

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
