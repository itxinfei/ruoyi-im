package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImSystemNotification;
import java.util.List;

/**
 * IM系统通知Service接口（Admin模块专用）
 *
 * <p>负责处理IM系统通知管理相关的业务逻辑</p>
 * <p>主要功能包括：通知的增删改查、用户通知查询、未读统计、已读标记、批量发送、批量清理等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
public interface ImSystemNotificationService {

    /**
     * 查询通知列表
     *
     * <p>根据条件查询系统通知列表，支持按类型、状态、接收用户等条件筛选</p>
     * <p>用于管理后台通知管理页面</p>
     *
     * @param notification 通知查询条件，包含type、status、receiverId等字段
     * @return 通知列表，如果没有符合条件的通知则返回空列表
     */
    List<ImSystemNotification> selectNotificationList(ImSystemNotification notification);

    /**
     * 获取用户通知列表
     *
     * <p>查询指定用户的通知记录，支持按通知类型筛选</p>
     * <p>返回结果按创建时间倒序排列，最新的通知在前</p>
     *
     * @param userId 用户ID，不能为空
     * @param type 通知类型（SYSTEM-系统通知、MESSAGE-消息通知、FRIEND-好友通知等），为null表示查询所有类型
     * @return 通知列表，按创建时间倒序排列
     */
    List<ImSystemNotification> getUserNotifications(Long userId, String type);

    /**
     * 根据ID获取通知信息
     *
     * <p>通过通知ID查询通知的详细信息，包括通知内容、接收者、状态等</p>
     *
     * @param id 通知ID，不能为空
     * @return 通知对象，如果通知不存在则返回null
     */
    ImSystemNotification getNotificationById(Long id);

    /**
     * 获取用户未读通知数量
     *
     * <p>统计指定用户的未读通知总数</p>
     * <p>用于未读消息提醒、徽章显示等</p>
     *
     * @param userId 用户ID，不能为空
     * @return 未读通知数量
     */
    int getUnreadCount(Long userId);

    /**
     * 发送系统通知
     *
     * <p>向指定用户发送系统通知</p>
     * <p>通知类型包括：SYSTEM-系统通知、MESSAGE-消息通知、FRIEND-好友通知、GROUP-群组通知等</p>
     *
     * @param receiverId 接收者ID，不能为空
     * @param type 通知类型，不能为空
     * @param title 通知标题，不能为空
     * @param content 通知内容，不能为空
     * @param relatedId 关联对象ID（如消息ID、群组ID等），为null表示无关联
     * @param relatedType 关联对象类型（如MESSAGE、GROUP等），为null表示无关联
     * @return 通知ID，发送失败返回null
     */
    Long sendNotification(Long receiverId, String type, String title, String content, Long relatedId, String relatedType);

    /**
     * 批量发送系统通知
     *
     * <p>向多个用户批量发送相同的系统通知</p>
     * <p>用于系统公告、全局通知等场景</p>
     *
     * @param receiverIds 接收者ID列表，不能为空
     * @param type 通知类型，不能为空
     * @param title 通知标题，不能为空
     * @param content 通知内容，不能为空
     */
    void batchSendNotification(List<Long> receiverIds, String type, String title, String content);

    /**
     * 标记通知为已读
     *
     * <p>将指定通知的状态更新为已读</p>
     * <p>验证用户是否有权限操作该通知</p>
     *
     * @param id 通知ID，不能为空
     * @param userId 当前用户ID，用于权限验证，不能为空
     */
    void markAsRead(Long id, Long userId);

    /**
     * 标记用户所有通知为已读
     *
     * <p>将指定用户的所有未读通知批量标记为已读</p>
     * <p>用于"全部已读"功能</p>
     *
     * @param userId 用户ID，不能为空
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * <p>根据ID删除指定的通知记录，删除操作为物理删除</p>
     * <p>验证用户是否有权限操作该通知</p>
     *
     * @param id 通知ID，不能为空
     * @param userId 当前用户ID，用于权限验证，不能为空
     */
    void deleteNotification(Long id, Long userId);

    /**
     * 清空用户所有通知
     *
     * <p>删除指定用户的所有通知记录</p>
     * <p>用于清空通知历史功能</p>
     *
     * @param userId 用户ID，不能为空
     */
    void clearAllNotifications(Long userId);
}
