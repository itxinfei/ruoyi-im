package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.im.domain.ImMessageAck;
import com.ruoyi.im.mapper.ImMessageAckMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.ImMessageAckService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.vo.message.MessageAckVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息ACK确认服务实现类
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class ImMessageAckServiceImpl implements ImMessageAckService {

    private final ImMessageAckMapper ackMapper;
    private final ImWebSocketBroadcastService broadcastService;
    private final ImMessageMapper messageMapper;

    public ImMessageAckServiceImpl(ImMessageAckMapper ackMapper,
            ImWebSocketBroadcastService broadcastService,
            ImMessageMapper messageMapper) {
        this.ackMapper = ackMapper;
        this.broadcastService = broadcastService;
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordDeliverAck(Long messageId, String clientMsgId, Long userId) {
        if (messageId == null) {
            return;
        }

        ImMessageAck ack = new ImMessageAck();
        ack.setMessageId(messageId);
        ack.setUserId(userId);
        ack.setAckType(ImMessageAck.ACK_TYPE_DELIVER);
        ack.setClientMsgId(clientMsgId);
        ack.setAckTimestamp(System.currentTimeMillis());
        ack.setCreateTime(LocalDateTime.now());
        ack.setUpdateTime(LocalDateTime.now());

        ackMapper.insert(ack);
        log.debug("记录送达ACK: messageId={}, clientMsgId={}", messageId, clientMsgId);

        // 广播给用户的其他设备
        Map<String, Object> ackMessage = new HashMap<>();
        ackMessage.put("type", "message_ack");
        ackMessage.put("messageId", messageId);
        ackMessage.put("clientMsgId", clientMsgId);
        ackMessage.put("ackType", ImMessageAck.ACK_TYPE_DELIVER);
        ackMessage.put("timestamp", System.currentTimeMillis());
        broadcastService.broadcastToUserExcept(userId, ackMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordReceiveAck(Long messageId, Long userId, String deviceId) {
        if (messageId == null || userId == null) {
            return;
        }

        // 检查是否已存在接收确认
        int existing = ackMapper.countAck(messageId, userId, ImMessageAck.ACK_TYPE_RECEIVE);
        if (existing > 0) {
            log.debug("消息接收ACK已存在，跳过: messageId={}, userId={}", messageId, userId);
            return;
        }

        ImMessageAck ack = new ImMessageAck();
        ack.setMessageId(messageId);
        ack.setUserId(userId);
        ack.setDeviceId(deviceId);
        ack.setAckType(ImMessageAck.ACK_TYPE_RECEIVE);
        ack.setAckTimestamp(System.currentTimeMillis());
        ack.setCreateTime(LocalDateTime.now());
        ack.setUpdateTime(LocalDateTime.now());

        ackMapper.insert(ack);
        log.info("记录接收ACK: messageId={}, userId={}, deviceId={}", messageId, userId, deviceId);

        // 1. 广播给接收者的其他设备
        Map<String, Object> ackMessage = new HashMap<>();
        ackMessage.put("type", "message_ack");
        ackMessage.put("messageId", messageId);
        ackMessage.put("ackType", ImMessageAck.ACK_TYPE_RECEIVE);
        ackMessage.put("timestamp", System.currentTimeMillis());
        broadcastService.broadcastToUserExcept(userId, ackMessage);

        // 2. 广播给发送者（通过会话广播）
        com.ruoyi.im.domain.ImMessage message = messageMapper.selectImMessageById(messageId);
        if (message != null) {
            broadcastMessageAck(message.getConversationId(), messageId, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordReadAck(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return;
        }

        // 检查是否已存在已读确认
        int existing = ackMapper.countAck(messageId, userId, ImMessageAck.ACK_TYPE_READ);
        if (existing > 0) {
            return;
        }

        ImMessageAck ack = new ImMessageAck();
        ack.setMessageId(messageId);
        ack.setUserId(userId);
        ack.setAckType(ImMessageAck.ACK_TYPE_READ);
        ack.setAckTimestamp(System.currentTimeMillis());
        ack.setCreateTime(LocalDateTime.now());
        ack.setUpdateTime(LocalDateTime.now());

        ackMapper.insert(ack);
        log.info("记录已读ACK: messageId={}, userId={}", messageId, userId);

        // 1. 广播给用户的其他设备
        Map<String, Object> ackMessage = new HashMap<>();
        ackMessage.put("type", "message_ack");
        ackMessage.put("messageId", messageId);
        ackMessage.put("ackType", ImMessageAck.ACK_TYPE_READ);
        ackMessage.put("timestamp", System.currentTimeMillis());
        broadcastService.broadcastToUserExcept(userId, ackMessage);

        // 2. 广播给发送者
        com.ruoyi.im.domain.ImMessage message = messageMapper.selectImMessageById(messageId);
        if (message != null) {
            broadcastMessageAck(message.getConversationId(), messageId, userId);
        }
    }

    @Override
    public boolean isReceived(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            return false;
        }
        int count = ackMapper.countAck(messageId, userId, ImMessageAck.ACK_TYPE_RECEIVE);
        return count > 0;
    }

    @Override
    public List<MessageAckVO> getMessageAcks(Long messageId) {
        if (messageId == null) {
            return Collections.emptyList();
        }

        List<ImMessageAck> acks = ackMapper.selectReceiveAcksByMessageId(messageId);

        return acks.stream().map(ack -> {
            MessageAckVO vo = new MessageAckVO();
            vo.setMessageId(ack.getMessageId());
            vo.setAckType(ack.getAckType());
            vo.setTimestamp(ack.getAckTimestamp());
            vo.setSuccess(true);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void broadcastMessageAck(Long conversationId, Long messageId, Long excludeUserId) {
        if (conversationId == null || messageId == null) {
            return;
        }

        Map<String, Object> ackMsg = new HashMap<>();
        ackMsg.put("type", "message_ack"); // 统一使用 message_ack 类型
        ackMsg.put("conversationId", conversationId);
        ackMsg.put("messageId", messageId);
        ackMsg.put("ackType", getAckType(messageId, excludeUserId)); // 自动获取 ACK 类型
        ackMsg.put("timestamp", System.currentTimeMillis());

        broadcastService.broadcastToConversationExcept(conversationId, excludeUserId, ackMsg);
        log.debug("广播消息ACK: conversationId={}, messageId={}", conversationId, messageId);
    }

    /**
     * 获取指定用户对消息的最新 ACK 类型
     */
    private String getAckType(Long messageId, Long userId) {
        int readCount = ackMapper.countAck(messageId, userId, ImMessageAck.ACK_TYPE_READ);
        if (readCount > 0) {
            return ImMessageAck.ACK_TYPE_READ;
        }
        int receiveCount = ackMapper.countAck(messageId, userId, ImMessageAck.ACK_TYPE_RECEIVE);
        if (receiveCount > 0) {
            return ImMessageAck.ACK_TYPE_RECEIVE;
        }
        return ImMessageAck.ACK_TYPE_DELIVER;
    }

    @Override
    public void processClientAck(Long userId, Long messageId, String ackType, String deviceId) {
        if ("receive".equals(ackType)) {
            recordReceiveAck(messageId, userId, deviceId);
        } else if ("read".equals(ackType)) {
            recordReadAck(messageId, userId);
        }
    }

    /**
     * 定时任务：清理7天前的ACK记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldAckRecords() {
        try {
            // MyBatis-Plus 方式删除
            LocalDateTime expireTime = LocalDateTime.now().minusDays(7);
            int deleted = ackMapper.delete(
                    Wrappers.<ImMessageAck>lambdaQuery()
                            .lt(ImMessageAck::getCreateTime, expireTime));

            if (deleted > 0) {
                log.info("清理过期ACK记录: count={}", deleted);
            }
        } catch (Exception e) {
            log.error("清理ACK记录失败", e);
        }
    }
}
