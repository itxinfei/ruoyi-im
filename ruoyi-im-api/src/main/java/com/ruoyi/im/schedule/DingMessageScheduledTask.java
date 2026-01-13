package com.ruoyi.im.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImDingMessage;
import com.ruoyi.im.domain.ImDingReceipt;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImDingMessageMapper;
import com.ruoyi.im.mapper.ImDingReceiptMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DING消息定时任务
 * 处理定时发送和强提醒功能
 *
 * @author ruoyi
 */
@Component
public class DingMessageScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(DingMessageScheduledTask.class);

    @Autowired
    private ImDingMessageMapper dingMessageMapper;

    @Autowired
    private ImDingReceiptMapper dingReceiptMapper;

    @Autowired
    private ImUserMapper userMapper;

    /**
     * 处理定时DING消息发送
     * 每分钟执行一次，检查是否有到期的定时DING需要发送
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processScheduledDingMessages() {
        try {
            // 查询状态为DRAFT且定时发送时间已到的DING消息
            LambdaQueryWrapper<ImDingMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImDingMessage::getStatus, "DRAFT")
                    .isNotNull(ImDingMessage::getScheduleTime)
                    .le(ImDingMessage::getScheduleTime, LocalDateTime.now());

            List<ImDingMessage> pendingDings = dingMessageMapper.selectList(wrapper);

            for (ImDingMessage ding : pendingDings) {
                sendScheduledDing(ding);
            }

            if (!pendingDings.isEmpty()) {
                log.info("定时DING消息处理完成，处理数量: {}", pendingDings.size());
            }

        } catch (Exception e) {
            log.error("处理定时DING消息失败", e);
        }
    }

    /**
     * 处理强提醒功能
     * 每5分钟执行一次，检查需要发送提醒的DING消息
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void processDingReminders() {
        try {
            // 查询需要发送提醒的DING消息
            // 条件：状态为SENT、需要回执、设置了提醒间隔、未达到最大提醒次数
            LambdaQueryWrapper<ImDingMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImDingMessage::getStatus, "SENT")
                    .eq(ImDingMessage::getReceiptRequired, 1)
                    .gt(ImDingMessage::getRemindInterval, 0)
                    .gt(ImDingMessage::getMaxRemindCount, 0)
                    .lt(ImDingMessage::getRemindedCount, ImDingMessage::getMaxRemindCount)
                    .isNotNull(ImDingMessage::getSendTime);

            List<ImDingMessage> dings = dingMessageMapper.selectList(wrapper);
            LocalDateTime now = LocalDateTime.now();

            for (ImDingMessage ding : dings) {
                // 计算是否到达提醒时间
                if (shouldSendReminder(ding, now)) {
                    sendReminder(ding);
                }
            }

        } catch (Exception e) {
            log.error("处理DING强提醒失败", e);
        }
    }

    /**
     * 发送定时DING消息
     */
    private void sendScheduledDing(ImDingMessage ding) {
        try {
            // 获取所有接收者
            List<ImDingReceipt> receipts = dingReceiptMapper.selectList(
                    new LambdaQueryWrapper<ImDingReceipt>()
                            .eq(ImDingReceipt::getDingId, ding.getId())
            );

            // 更新状态为已发送
            ding.setStatus("SENT");
            ding.setSendTime(LocalDateTime.now());
            dingMessageMapper.updateById(ding);

            // 推送通知给所有接收者
            for (ImDingReceipt receipt : receipts) {
                sendDingNotification(receipt.getReceiverId(), ding, ding.getSenderId());
            }

            log.info("定时DING消息已发送: dingId={}", ding.getId());

        } catch (Exception e) {
            log.error("发送定时DING消息失败: dingId={}", ding.getId(), e);
            ding.setStatus("FAILED");
            dingMessageMapper.updateById(ding);
        }
    }

    /**
     * 判断是否应该发送提醒
     */
    private boolean shouldSendReminder(ImDingMessage ding, LocalDateTime now) {
        if (ding.getSendTime() == null || ding.getRemindInterval() == null) {
            return false;
        }

        // 计算下次应该提醒的时间
        long minutesSinceSend = ChronoUnit.MINUTES.between(ding.getSendTime(), now);
        long minutesSinceLastReminder = minutesSinceSend / ding.getRemindInterval();
        int expectedRemindedCount = (int) minutesSinceLastReminder;

        // 如果期望的提醒次数大于实际提醒次数，说明需要发送提醒
        return expectedRemindedCount > ding.getRemindedCount();
    }

    /**
     * 发送强提醒通知
     */
    private void sendReminder(ImDingMessage ding) {
        try {
            // 获取未确认的接收者
            List<ImDingReceipt> unconfirmedReceipts = dingReceiptMapper.selectList(
                    new LambdaQueryWrapper<ImDingReceipt>()
                            .eq(ImDingReceipt::getDingId, ding.getId())
                            .eq(ImDingReceipt::getConfirmed, 0)
            );

            if (unconfirmedReceipts.isEmpty()) {
                // 所有人都已确认，不需要提醒
                return;
            }

            // 更新提醒次数
            ding.setRemindedCount(ding.getRemindedCount() + 1);
            dingMessageMapper.updateById(ding);

            // 推送提醒通知
            for (ImDingReceipt receipt : unconfirmedReceipts) {
                sendReminderNotification(receipt.getReceiverId(), ding);
            }

            log.info("DING强提醒已发送: dingId={}, remindCount={}", ding.getId(), ding.getRemindedCount());

        } catch (Exception e) {
            log.error("发送DING强提醒失败: dingId={}", ding.getId(), e);
        }
    }

    /**
     * 发送DING消息的WebSocket通知
     */
    private void sendDingNotification(Long receiverId, ImDingMessage ding, Long senderId) {
        try {
            ImUser sender = userMapper.selectImUserById(senderId);
            String senderName = sender != null ? (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : "未知用户";
            String senderAvatar = sender != null ? sender.getAvatar() : "";

            Map<String, Object> dingMessage = new HashMap<>();
            dingMessage.put("type", "ding");
            dingMessage.put("action", "new_ding");

            Map<String, Object> payload = new HashMap<>();
            payload.put("dingId", ding.getId());
            payload.put("content", ding.getContent());
            payload.put("dingType", ding.getDingType());
            payload.put("isUrgent", ding.getIsUrgent());
            payload.put("receiptRequired", ding.getReceiptRequired());
            payload.put("sendTime", ding.getSendTime() != null ? ding.getSendTime().toString() : LocalDateTime.now().toString());
            payload.put("senderId", senderId);
            payload.put("senderName", senderName);
            payload.put("senderAvatar", senderAvatar);

            dingMessage.put("payload", payload);
            dingMessage.put("timestamp", System.currentTimeMillis());

            String messageJson = convertToJson(dingMessage);

            Map<Long, Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            Session targetSession = onlineUsers.get(receiverId);

            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getBasicRemote().sendText(messageJson);
                log.debug("DING消息已推送给用户: receiverId={}", receiverId);
            }

        } catch (Exception e) {
            log.error("发送DING消息WebSocket通知失败: receiverId={}", receiverId, e);
        }
    }

    /**
     * 发送强提醒通知
     */
    private void sendReminderNotification(Long receiverId, ImDingMessage ding) {
        try {
            ImUser sender = userMapper.selectImUserById(ding.getSenderId());
            String senderName = sender != null ? (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : "未知用户";

            Map<String, Object> reminderMessage = new HashMap<>();
            reminderMessage.put("type", "ding");
            reminderMessage.put("action", "ding_reminder");

            Map<String, Object> payload = new HashMap<>();
            payload.put("dingId", ding.getId());
            payload.put("content", ding.getContent());
            payload.put("senderName", senderName);
            payload.put("remindCount", ding.getRemindedCount());
            payload.put("maxRemindCount", ding.getMaxRemindCount());

            reminderMessage.put("payload", payload);
            reminderMessage.put("timestamp", System.currentTimeMillis());

            String messageJson = convertToJson(reminderMessage);

            Map<Long, Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            Session targetSession = onlineUsers.get(receiverId);

            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getBasicRemote().sendText(messageJson);
                log.debug("DING强提醒已推送给用户: receiverId={}, remindCount={}", receiverId, ding.getRemindedCount());
            }

        } catch (Exception e) {
            log.error("发送DING强提醒通知失败: receiverId={}", receiverId, e);
        }
    }

    /**
     * 简单的JSON转换（实际项目中应使用ObjectMapper）
     */
    private String convertToJson(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("{}");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
