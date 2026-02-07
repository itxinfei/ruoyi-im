package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.vo.message.MessageAckVO;

import java.util.List;

/**
 * 消息ACK确认服务接口
 *
 * 管理消息的送达、接收、已读确认
 *
 * @author ruoyi
 */
public interface ImMessageAckService {

    /**
     * 记录消息送达确认
     * 消息成功保存到数据库后记录
     *
     * @param messageId 消息ID
     * @param clientMsgId 客户端消息ID
     * @param userId 用户ID
     */
    void recordDeliverAck(Long messageId, String clientMsgId, Long userId);

    /**
     * 记录消息接收确认
     * 客户端通过WebSocket确认收到消息
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    void recordReceiveAck(Long messageId, Long userId, String deviceId);

    /**
     * 记录消息已读确认
     * 用户阅读消息后记录
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void recordReadAck(Long messageId, Long userId);

    /**
     * 检查消息是否已被接收
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否已接收
     */
    boolean isReceived(Long messageId, Long userId);

    /**
     * 获取消息的接收确认列表
     * 查询哪些设备已接收该消息
     *
     * @param messageId 消息ID
     * @return 接收确认列表
     */
    List<MessageAckVO> getMessageAcks(Long messageId);

    /**
     * 广播消息ACK状态
     * 当消息状态变更时通知其他用户
     *
     * @param conversationId 会话ID
     * @param messageId 消息ID
     * @param excludeUserId 排除的用户ID（发送者）
     */
    void broadcastMessageAck(Long conversationId, Long messageId, Long excludeUserId);

    /**
     * 处理客户端发送的ACK
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     * @param ackType ACK类型（receive/read）
     * @param deviceId 设备ID
     */
    void processClientAck(Long userId, Long messageId, String ackType, String deviceId);
}
