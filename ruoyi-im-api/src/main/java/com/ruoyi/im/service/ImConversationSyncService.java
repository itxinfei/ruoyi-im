package com.ruoyi.im.service;

import com.ruoyi.im.vo.sync.ConversationSyncEvent;
import com.ruoyi.im.vo.sync.ConversationSyncResponse;

import java.util.List;

/**
 * 会话同步服务接口
 *
 * 管理用户多端之间的会话设置同步
 *
 * @author ruoyi
 */
public interface ImConversationSyncService {

    /**
     * 获取会话同步数据
     * 获取指定同步点之后的会话变更
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param lastSyncTime 上次同步时间戳（毫秒）
     * @return 同步响应
     */
    ConversationSyncResponse syncConversations(Long userId, String deviceId, Long lastSyncTime);

    /**
     * 更新会话同步点
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param lastSyncTime 最后同步时间
     */
    void updateSyncPoint(Long userId, String deviceId, Long lastSyncTime);

    /**
     * 广播会话设置变更
     * 当用户修改会话设置时，同步到其他设备
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @param eventType 事件类型：pin/unpin/mute/unmute/delete/archive/unarchive
     * @param eventData 事件数据
     * @param excludeDeviceId 排除的设备ID
     */
    void broadcastConversationEvent(Long userId, Long conversationId, String eventType,
                                   Object eventData, String excludeDeviceId);

    /**
     * 处理会话设置变更
     * 当收到其他设备广播的设置变更时应用
     *
     * @param userId 用户ID
     * @param event 同步事件
     */
    void handleConversationEvent(Long userId, ConversationSyncEvent event);

    /**
     * 获取用户的所有同步点
     *
     * @param userId 用户ID
     * @return 同步点列表
     */
    List<com.ruoyi.im.domain.ImConversationSyncPoint> getUserSyncPoints(Long userId);

    /**
     * 重置设备同步点
     * 删除设备的同步点，下次同步从头开始
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    void resetSyncPoint(Long userId, String deviceId);
}
