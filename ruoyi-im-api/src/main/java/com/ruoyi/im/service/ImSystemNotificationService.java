package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImSystemNotification;

import java.util.List;

/**
 * 系统通知服务接口
 *
 * @author ruoyi
 */
public interface ImSystemNotificationService {

    /**
     * 发送通知
     *
     * @param receiverId 接收者ID
     * @param type 通知类型
     * @param title 标题
     * @param content 内容
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     * @return 通知ID
     */
    Long sendNotification(Long receiverId, String type, String title, String content, Long relatedId, String relatedType);

    /**
     * 批量发送通知
     *
     * @param receiverIds 接收者ID列表
     * @param type 通知类型
     * @param title 标题
     * @param content 内容
     */
    void batchSendNotification(List<Long> receiverIds, String type, String title, String content);

    /**
     * 获取用户通知列表
     *
     * @param userId 用户ID
     * @param type 类型（可选）
     * @return 通知列表
     */
    List<ImSystemNotification> getUserNotifications(Long userId, String type);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId 用户ID（可选，为null时表示管理员操作）
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * 获取通知详情
     *
     * @param notificationId 通知ID
     * @return 通知详情
     */
    ImSystemNotification getNotificationById(Long notificationId);

    /**
     * 清空用户所有通知
     *
     * @param userId 用户ID
     */
    void clearAllNotifications(Long userId);

    // ==================== 管理后台专用方法 ====================

    /**
     * 查询通知列表（管理员可查看所有通知）
     *
     * @param notification 查询条件
     * @return 通知列表
     */
    List<ImSystemNotification> selectNotificationList(ImSystemNotification notification);

    /**
     * 新增通知
     *
     * @param notification 通知信息
     * @return 结果
     */
    int insertNotification(ImSystemNotification notification);

    /**
     * 修改通知
     *
     * @param notification 通知信息
     * @return 结果
     */
    int updateNotification(ImSystemNotification notification);

    /**
     * 批量删除通知
     *
     * @param ids 通知ID数组
     * @return 结果
     */
    int deleteNotificationByIds(Long[] ids);
}
