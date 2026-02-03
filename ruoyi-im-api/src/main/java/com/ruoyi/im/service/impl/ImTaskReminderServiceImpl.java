package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constant.ErrorCode;
import com.ruoyi.im.domain.ImTask;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImTaskMapper;
import com.ruoyi.im.service.ImTaskReminderService;
import com.ruoyi.im.service.ImSystemNotificationService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 任务提醒服务实现类
 *
 * @author ruoyi
 */
@Service
public class ImTaskReminderServiceImpl implements ImTaskReminderService {

    private static final Logger log = LoggerFactory.getLogger(ImTaskReminderServiceImpl.class);

    /** 提醒时间窗口（分钟），在提醒时间前后这段时间内的任务都会被检查 */
    private static final int REMINDER_WINDOW_MINUTES = 5;

    /** 提前提醒天数配置 */
    private static final int[] REMIND_DAYS_BEFORE_DUE = {7, 3, 1, 0};

    @Resource
    private ImTaskMapper taskMapper;

    @Resource
    private final ImSystemNotificationService notificationService;

    @Resource
    private ImWebSocketBroadcastService broadcastService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendReminder(ImTask task) {
        if (task == null) {
            return;
        }

        try {
            String message = buildReminderMessage(task);

            // 通过WebSocket发送实时通知
            if (task.getAssigneeId() != null) {
                ImWebSocketEndpoint.sendNotification(task.getAssigneeId(), "TASK_REMINDER",
                    "任务提醒: " + task.getTitle(), message);
            }

            // 创建系统通知
            if (task.getAssigneeId() != null) {
                notificationService.createNotification(
                    task.getAssigneeId(),
                    "任务提醒",
                    message,
                    "TASK",
                    task.getId()
                );
            }

            // 标记提醒已发送
            markReminderSent(task.getId());

            log.info("发送任务提醒成功: taskId={}, title={}", task.getId(), task.getTitle());
        } catch (Exception e) {
            log.error("发送任务提醒失败: taskId={}, error={}", task.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void batchSendReminders(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<ImTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ImTask::getId, taskIds)
                .ne(ImTask::getStatus, "COMPLETED")
                .ne(ImTask::getStatus, "CANCELLED");

        List<ImTask> tasks = taskMapper.selectList(wrapper);
        for (ImTask task : tasks) {
            sendReminder(task);
        }

        log.info("批量发送任务提醒: count={}", tasks.size());
    }

    @Override
    public List<ImTask> getTasksToRemind(LocalDateTime currentTime) {
        LocalDateTime windowStart = currentTime.minusMinutes(REMINDER_WINDOW_MINUTES);
        LocalDateTime windowEnd = currentTime.plusMinutes(REMINDER_WINDOW_MINUTES);

        LambdaQueryWrapper<ImTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(ImTask::getRemindTime)
                .ge(ImTask::getRemindTime, windowStart)
                .le(ImTask::getRemindTime, windowEnd)
                .ne(ImTask::getStatus, "COMPLETED")
                .ne(ImTask::getStatus, "CANCELLED")
                .orderByAsc(ImTask::getRemindTime);

        return taskMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setReminder(Long taskId, LocalDateTime remindTime, String remindType) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.dataNotFound();
        }

        task.setRemindTime(remindTime);
        if (remindType != null) {
            task.setRemindType(remindType);
        }

        taskMapper.updateById(task);
        log.info("设置任务提醒: taskId={}, remindTime={}", taskId, remindTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReminder(Long taskId) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.dataNotFound();
        }

        task.setRemindTime(null);
        task.setRemindType(null);
        taskMapper.updateById(task);

        log.info("取消任务提醒: taskId={}", taskId);
    }

    @Override
    public void markReminderSent(Long taskId) {
        // 这里可以选择添加一个提醒记录表来跟踪提醒历史
        // 简单实现：可以通过修改任务记录，添加一个"最后提醒时间"字段
        log.debug("标记提醒已发送: taskId={}", taskId);
    }

    @Override
    public boolean needsReminder(ImTask task, LocalDateTime currentTime) {
        if (task == null || task.getRemindTime() == null) {
            return false;
        }

        // 检查任务状态
        if ("COMPLETED".equals(task.getStatus()) || "CANCELLED".equals(task.getStatus())) {
            return false;
        }

        // 检查提醒时间是否在时间窗口内
        LocalDateTime remindTime = task.getRemindTime();
        LocalDateTime windowStart = currentTime.minusMinutes(REMINDER_WINDOW_MINUTES);
        LocalDateTime windowEnd = currentTime.plusMinutes(REMINDER_WINDOW_MINUTES);

        return !remindTime.isBefore(windowStart) && !remindTime.isAfter(windowEnd);
    }

    @Override
    @Scheduled(cron = "0 */5 * * * *") // 每5分钟执行一次
    public void scanAndSendReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<ImTask> tasksToRemind = getTasksToRemind(now);

            log.info("扫描任务提醒: 当前时间={}, 需要提醒的任务数={}", now, tasksToRemind.size());

            for (ImTask task : tasksToRemind) {
                sendReminder(task);
            }

            // 同时扫描逾期和即将到期的任务
            scanDueAndOverdueTasks();
        } catch (Exception e) {
            log.error("扫描任务提醒失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 扫描并处理到期和逾期任务
     */
    private void scanDueAndOverdueTasks() {
        LocalDate today = LocalDate.now();

        // 扫描即将到期的任务
        for (int days : REMIND_DAYS_BEFORE_DUE) {
            LocalDate targetDate = today.plusDays(days);

            LambdaQueryWrapper<ImTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNotNull(ImTask::getDueDate)
                    .eq(ImTask::getDueDate, targetDate)
                    .ne(ImTask::getStatus, "COMPLETED")
                    .ne(ImTask::getStatus, "CANCELLED")
                    .isNotNull(ImTask::getAssigneeId);

            List<ImTask> dueTasks = taskMapper.selectList(wrapper);
            for (ImTask task : dueTasks) {
                sendDueReminder(task, days);
            }
        }

        // 扫描已逾期的任务
        LambdaQueryWrapper<ImTask> overdueWrapper = new LambdaQueryWrapper<>();
        overdueWrapper.isNotNull(ImTask::getDueDate)
                .lt(ImTask::getDueDate, today)
                .ne(ImTask::getStatus, "COMPLETED")
                .ne(ImTask::getStatus, "CANCELLED")
                .isNotNull(ImTask::getAssigneeId);

        List<ImTask> overdueTasks = taskMapper.selectList(overdueWrapper);
        for (ImTask task : overdueTasks) {
            long overdueDays = ChronoUnit.DAYS.between(task.getDueDate(), today);
            sendOverdueReminder(task, (int) overdueDays);
        }
    }

    @Override
    @Scheduled(cron = "0 0 9 * * *") // 每天早上9点执行
    public void scanAndSendOverdueReminders() {
        try {
            scanAndSendReminders();
        } catch (Exception e) {
            log.error("发送逾期任务提醒失败: error={}", e.getMessage(), e);
        }
    }

    @Override
    public void sendDueReminder(ImTask task, int remainingDays) {
        if (task == null || task.getAssigneeId() == null) {
            return;
        }

        String message;
        if (remainingDays == 0) {
            message = String.format("【今日到期】您的任务「%s」今天到期，请及时处理", task.getTitle());
        } else {
            message = String.format("【任务提醒】您的任务「%s」将在%d天后到期，请提前安排", task.getTitle(), remainingDays);
        }

        // 发送通知
        ImWebSocketEndpoint.sendNotification(task.getAssigneeId(), "TASK_DUE_REMINDER",
            "任务到期提醒", message);

        notificationService.createNotification(
            task.getAssigneeId(),
            "任务到期提醒",
            message,
            "TASK",
            task.getId()
        );

        log.info("发送任务到期提醒: taskId={}, remainingDays={}", task.getId(), remainingDays);
    }

    @Override
    public void sendOverdueReminder(ImTask task, int overdueDays) {
        if (task == null || task.getAssigneeId() == null) {
            return;
        }

        String message;
        if (overdueDays == 0) {
            message = String.format("【已逾期】您的任务「%s」已于今日到期，请尽快处理", task.getTitle());
        } else {
            message = String.format("【已逾期】您的任务「%s」已逾期%d天，请尽快处理", task.getTitle(), overdueDays);
        }

        // 发送通知
        ImWebSocketEndpoint.sendNotification(task.getAssigneeId(), "TASK_OVERDUE_REMINDER",
            "任务逾期提醒", message);

        notificationService.createNotification(
            task.getAssigneeId(),
            "任务逾期提醒",
            message,
            "TASK",
            task.getId()
        );

        log.info("发送任务逾期提醒: taskId={}, overdueDays={}", task.getId(), overdueDays);
    }

    @Override
    public List<ImTask> getUserReminderTasks(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusDays(30); // 未来30天

        LambdaQueryWrapper<ImTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ImTask::getAssigneeId, userId)
                    .or()
                    .like(ImTask::getFollowers, "%" + userId + "%"))
                .isNotNull(ImTask::getRemindTime)
                .ge(ImTask::getRemindTime, now)
                .le(ImTask::getRemindTime, windowEnd)
                .ne(ImTask::getStatus, "COMPLETED")
                .ne(ImTask::getStatus, "CANCELLED")
                .orderByAsc(ImTask::getRemindTime);

        return taskMapper.selectList(wrapper);
    }

    /**
     * 构建提醒消息内容
     */
    private String buildReminderMessage(ImTask task) {
        StringBuilder message = new StringBuilder();

        message.append("您有任务需要处理：");
        message.append(task.getTitle());

        if (task.getDueDate() != null) {
            message.append("，截止日期：").append(task.getDueDate());
        }

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            message.append("\n").append(task.getDescription());
        }

        return message.toString();
    }
}
