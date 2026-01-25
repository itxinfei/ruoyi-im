package com.ruoyi.im.service;

import com.ruoyi.im.vo.marker.ImMessageMarkerVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息标记服务接口
 *
 * @author ruoyi
 */
public interface ImMessageMarkerService {

    /**
     * 标记消息
     *
     * @param messageId   消息ID
     * @param markerType  标记类型：FLAG标记, TODO待办, IMPORTANT重要
     * @param color       标记颜色
     * @param userId      用户ID
     * @return 标记ID
     */
    Long markMessage(Long messageId, String markerType, String color, Long userId);

    /**
     * 取消标记
     *
     * @param messageId  消息ID
     * @param markerType 标记类型
     * @param userId     用户ID
     */
    void unmarkMessage(Long messageId, String markerType, Long userId);

    /**
     * 设置待办提醒
     *
     * @param messageId   消息ID
     * @param remindTime  提醒时间
     * @param remark      备注
     * @param userId      用户ID
     * @return 标记ID
     */
    Long setTodoReminder(Long messageId, LocalDateTime remindTime, String remark, Long userId);

    /**
     * 完成待办
     *
     * @param markerId 标记ID
     * @param userId    用户ID
     */
    void completeTodo(Long markerId, Long userId);

    /**
     * 重启待办
     *
     * @param markerId 标记ID
     * @param userId    用户ID
     */
    void reopenTodo(Long markerId, Long userId);

    /**
     * 获取用户的标记列表
     *
     * @param userId     用户ID
     * @param markerType 标记类型（可选）
     * @return 标记列表
     */
    List<ImMessageMarkerVO> getUserMarkers(Long userId, String markerType);

    /**
     * 获取消息的标记列表
     *
     * @param messageId 消息ID
     * @return 标记列表
     */
    List<ImMessageMarkerVO> getMessageMarkers(Long messageId);

    /**
     * 获取用户待办数量
     *
     * @param userId 用户ID
     * @return 待办数量
     */
    Integer getUserTodoCount(Long userId);

    /**
     * 处理到期提醒
     *
     * @return 处理的提醒数量
     */
    int processPendingReminders();
}
