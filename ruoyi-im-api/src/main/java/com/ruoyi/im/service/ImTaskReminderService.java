package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImTask;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务提醒服务接口
 *
 * @author ruoyi
 */
public interface ImTaskReminderService {

    /**
     * 发送任务提醒
     *
     * @param task 任务信息
     */
    void sendReminder(ImTask task);

    /**
     * 批量发送任务提醒
     *
     * @param taskIds 任务ID列表
     */
    void batchSendReminders(List<Long> taskIds);

    /**
     * 获取需要提醒的任务列表
     *
     * @param currentTime 当前时间
     * @return 需要提醒的任务列表
     */
    List<ImTask> getTasksToRemind(LocalDateTime currentTime);

    /**
     * 设置任务提醒
     *
     * @param taskId 任务ID
     * @param remindTime 提醒时间
     * @param remindType 提醒类型
     */
    void setReminder(Long taskId, LocalDateTime remindTime, String remindType);

    /**
     * 取消任务提醒
     *
     * @param taskId 任务ID
     */
    void cancelReminder(Long taskId);

    /**
     * 标记提醒已发送
     *
     * @param taskId 任务ID
     */
    void markReminderSent(Long taskId);

    /**
     * 检查任务是否需要提醒
     *
     * @param task 任务
     * @param currentTime 当前时间
     * @return 是否需要提醒
     */
    boolean needsReminder(ImTask task, LocalDateTime currentTime);

    /**
     * 扫描并发送逾期任务提醒
     * 定时任务调用，扫描即将到期或已逾期的任务发送提醒
     */
    void scanAndSendOverdueReminders();

    /**
     * 扫描并发送任务提醒
     * 定时任务调用，扫描需要提醒的任务并发送
     */
    void scanAndSendReminders();

    /**
     * 发送任务截止提醒
     *
     * @param task 任务信息
     * @param remainingDays 剩余天数
     */
    void sendDueReminder(ImTask task, int remainingDays);

    /**
     * 发送任务逾期提醒
     *
     * @param task 任务信息
     * @param overdueDays 逾期天数
     */
    void sendOverdueReminder(ImTask task, int overdueDays);

    /**
     * 获取用户的提醒任务列表
     *
     * @param userId 用户ID
     * @return 提醒任务列表
     */
    List<ImTask> getUserReminderTasks(Long userId);
}
