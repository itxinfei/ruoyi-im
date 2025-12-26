package com.ruoyi.im.service;

import com.alibaba.fastjson.JSONObject;

/**
 * WebSocket服务接口
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
public interface IImWebSocketService 
{
    /**
     * 更新用户在线状态
     * 
     * @param userId 用户ID
     * @param status 状态（online/offline）
     * @return 结果
     */
    public int updateUserOnlineStatus(Long userId, String status);

    /**
     * 更新用户最后活跃时间
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int updateUserLastActiveTime(Long userId);

    /**
     * 处理聊天消息
     * 
     * @param messageObj 消息对象
     * @param userId 发送者ID
     */
    public void handleChatMessage(JSONObject messageObj, Long userId);

    /**
     * 处理正在输入消息
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     */
    public void handleTypingMessage(Long conversationId, Long userId);

    /**
     * 处理消息已读
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    public void handleReadMessage(Long messageId, Long userId);

    /**
     * 发送系统通知
     * 
     * @param userId 用户ID
     * @param notification 通知内容
     */
    public void sendSystemNotification(Long userId, JSONObject notification);

    /**
     * 发送好友请求通知
     * 
     * @param userId 目标用户ID
     * @param fromUserId 发送者ID
     * @param requestId 请求ID
     */
    public void sendFriendRequestNotification(Long userId, Long fromUserId, Long requestId);

    /**
     * 发送群组邀请通知
     * 
     * @param userId 目标用户ID
     * @param groupId 群组ID
     * @param inviterId 邀请者ID
     */
    public void sendGroupInviteNotification(Long userId, Long groupId, Long inviterId);

    /**
     * 广播消息给会话中的所有成员
     * 
     * @param conversationId 会话ID
     * @param message 消息内容
     * @param excludeUserId 排除的用户ID（可选）
     */
    public void broadcastToConversation(Long conversationId, JSONObject message, Long excludeUserId);

    /**
     * 广播系统公告
     * 
     * @param announcement 公告内容
     */
    public void broadcastSystemAnnouncement(JSONObject announcement);

    /**
     * 发送消息状态更新
     * 
     * @param messageId 消息ID
     * @param status 状态（sent/delivered/read）
     * @param userId 用户ID
     */
    public void sendMessageStatusUpdate(Long messageId, String status, Long userId);

    /**
     * 发送用户状态变更通知
     * 
     * @param userId 用户ID
     * @param status 新状态
     * @param friendIds 好友ID列表
     */
    public void notifyUserStatusChange(Long userId, String status, java.util.List<Long> friendIds);

    /**
     * 发送会话更新通知
     * 
     * @param conversationId 会话ID
     * @param updateType 更新类型
     * @param data 更新数据
     */
    public void sendConversationUpdate(Long conversationId, String updateType, JSONObject data);

    /**
     * 发送文件传输进度
     * 
     * @param userId 用户ID
     * @param fileId 文件ID
     * @param progress 进度百分比
     */
    public void sendFileTransferProgress(Long userId, String fileId, Integer progress);

    /**
     * 发送语音通话邀请
     * 
     * @param fromUserId 发起者ID
     * @param toUserId 接收者ID
     * @param callId 通话ID
     * @param callType 通话类型（voice/video）
     */
    public void sendCallInvitation(Long fromUserId, Long toUserId, String callId, String callType);

    /**
     * 发送通话状态更新
     * 
     * @param callId 通话ID
     * @param status 状态（ringing/accepted/rejected/ended）
     * @param participants 参与者列表
     */
    public void sendCallStatusUpdate(String callId, String status, java.util.List<Long> participants);

    /**
     * 发送屏幕共享邀请
     * 
     * @param fromUserId 发起者ID
     * @param toUserIds 接收者ID列表
     * @param sessionId 会话ID
     */
    public void sendScreenShareInvitation(Long fromUserId, java.util.List<Long> toUserIds, String sessionId);

    /**
     * 发送实时协作更新
     * 
     * @param documentId 文档ID
     * @param operation 操作内容
     * @param userId 操作者ID
     */
    public void sendCollaborationUpdate(String documentId, JSONObject operation, Long userId);
}