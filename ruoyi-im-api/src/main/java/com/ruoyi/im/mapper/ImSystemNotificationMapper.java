package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImSystemNotification;

import java.util.List;

/**
 * 系统通知Mapper接口
 *
 * @author ruoyi
 */
public interface ImSystemNotificationMapper {

    /**
     * 查询系统通知
     *
     * @param id 通知ID
     * @return 系统通知
     */
    ImSystemNotification selectImSystemNotificationById(Long id);

    /**
     * 查询系统通知列表
     *
     * @param imSystemNotification 系统通知
     * @return 系统通知集合
     */
    List<ImSystemNotification> selectImSystemNotificationList(ImSystemNotification imSystemNotification);

    /**
     * 查询用户的通知列表
     *
     * @param receiverId 接收者ID
     * @return 系统通知集合
     */
    List<ImSystemNotification> selectNotificationsByReceiverId(Long receiverId);

    /**
     * 查询用户的未读通知数量
     *
     * @param receiverId 接收者ID
     * @return 数量
     */
    int countUnreadByReceiverId(Long receiverId);

    /**
     * 根据类型查询用户通知
     *
     * @param receiverId 接收者ID
     * @param type 通知类型
     * @return 系统通知集合
     */
    List<ImSystemNotification> selectNotificationsByType(Long receiverId, String type);

    /**
     * 新增系统通知
     *
     * @param imSystemNotification 系统通知
     * @return 结果
     */
    int insertImSystemNotification(ImSystemNotification imSystemNotification);

    /**
     * 批量新增系统通知
     *
     * @param notifications 系统通知集合
     * @return 结果
     */
    int batchInsertNotifications(List<ImSystemNotification> notifications);

    /**
     * 修改系统通知
     *
     * @param imSystemNotification 系统通知
     * @return 结果
     */
    int updateImSystemNotification(ImSystemNotification imSystemNotification);

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 结果
     */
    int markAsRead(Long id);

    /**
     * 批量标记通知为已读
     *
     * @param ids 通知ID集合
     * @return 结果
     */
    int batchMarkAsRead(Long[] ids);

    /**
     * 标记用户所有通知为已读
     *
     * @param receiverId 接收者ID
     * @return 结果
     */
    int markAllAsRead(Long receiverId);

    /**
     * 删除系统通知
     *
     * @param id 通知ID
     * @return 结果
     */
    int deleteImSystemNotificationById(Long id);

    /**
     * 批量删除系统通知
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImSystemNotificationByIds(Long[] ids);
}
