package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImScheduledMessage;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.dto.message.ScheduledMessageCreateRequest;
import com.ruoyi.im.mapper.ImScheduledMessageMapper;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImScheduledMessageService;
import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.vo.message.ScheduledMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时消息服务实现
 *
 * @author ruoyi
 */
@Service
public class ImScheduledMessageServiceImpl implements ImScheduledMessageService {

    private static final Logger log = LoggerFactory.getLogger(ImScheduledMessageServiceImpl.class);

    /** 最小定时发送缓冲时间（分钟） */
    private static final int MIN_SCHEDULE_BUFFER_MINUTES = 1;

    private final ImScheduledMessageMapper scheduledMessageMapper;
    private final ImMessageService messageService;
    private final ObjectMapper objectMapper;

    public ImScheduledMessageServiceImpl(ImScheduledMessageMapper scheduledMessageMapper,
                                          ImMessageService messageService,
                                          ObjectMapper objectMapper) {
        this.scheduledMessageMapper = scheduledMessageMapper;
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScheduledMessageVO createScheduledMessage(ScheduledMessageCreateRequest request, Long userId) {
        // 验证定时时间必须在当前时间之后
        if (request.getScheduledTime() != null && request.getScheduledTime().isBefore(LocalDateTime.now().plusMinutes(MIN_SCHEDULE_BUFFER_MINUTES))) {
            throw new IllegalArgumentException("定时发送时间必须至少在当前时间" + MIN_SCHEDULE_BUFFER_MINUTES + "分钟之后");
        }

        ImScheduledMessage scheduledMessage = new ImScheduledMessage();
        scheduledMessage.setUserId(userId);
        scheduledMessage.setConversationId(request.getConversationId());
        scheduledMessage.setMessageType(request.getMessageType());
        scheduledMessage.setContent(request.getContent());
        scheduledMessage.setFileUrl(request.getFileUrl());
        scheduledMessage.setFileName(request.getFileName());
        scheduledMessage.setReplyToMessageId(request.getReplyToMessageId());
        scheduledMessage.setScheduledTime(request.getScheduledTime());
        scheduledMessage.setStatus(ImScheduledMessage.STATUS_PENDING);
        scheduledMessage.setRetryCount(0);

        scheduledMessageMapper.insert(scheduledMessage);
        log.info("创建定时消息: id={}, userId={}, scheduledTime={}",
                scheduledMessage.getId(), userId, request.getScheduledTime());

        return toVO(scheduledMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelScheduledMessage(Long id, Long userId) {
        LambdaQueryWrapper<ImScheduledMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImScheduledMessage::getId, id)
                .eq(ImScheduledMessage::getUserId, userId)
                .eq(ImScheduledMessage::getStatus, ImScheduledMessage.STATUS_PENDING);

        ImScheduledMessage scheduledMessage = scheduledMessageMapper.selectOne(wrapper);
        if (scheduledMessage == null) {
            throw new IllegalArgumentException("定时消息不存在或已处理");
        }

        scheduledMessage.setStatus(ImScheduledMessage.STATUS_CANCELLED);
        scheduledMessageMapper.updateById(scheduledMessage);
        log.info("取消定时消息: id={}, userId={}", id, userId);
    }

    @Override
    public List<ScheduledMessageVO> getUserScheduledMessages(Long userId) {
        List<ImScheduledMessage> messages = scheduledMessageMapper.selectByUserId(userId);
        return messages.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processScheduledMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<ImScheduledMessage> pendingMessages = scheduledMessageMapper.selectPendingMessages(now);

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.info("开始处理定时消息: count={}", pendingMessages.size());

        for (ImScheduledMessage scheduledMsg : pendingMessages) {
            try {
                // 构建消息发送请求
                ImMessageSendRequest request = new ImMessageSendRequest();
                request.setConversationId(scheduledMsg.getConversationId());
                request.setType(scheduledMsg.getMessageType());
                request.setContent(scheduledMsg.getContent() != null ? scheduledMsg.getContent() : "");
                // 文件信息通过extra字段传递
                if (scheduledMsg.getFileUrl() != null) {
                    Map<String, String> extraData = new HashMap<>();
                    extraData.put("fileUrl", scheduledMsg.getFileUrl());
                    extraData.put("fileName", scheduledMsg.getFileName());
                    try {
                        request.setExtra(objectMapper.writeValueAsString(extraData));
                    } catch (Exception e) {
                        log.error("构建extra字段失败: scheduledId={}", scheduledMsg.getId(), e);
                    }
                }
                if (scheduledMsg.getReplyToMessageId() != null) {
                    request.setReplyToMessageId(scheduledMsg.getReplyToMessageId());
                }

                // 发送消息
                ImMessageVO messageVO = messageService.sendMessage(request, scheduledMsg.getUserId());

                // 更新定时消息状态
                scheduledMsg.setStatus(ImScheduledMessage.STATUS_SENT);
                scheduledMsg.setSentMessageId(messageVO.getId());
                scheduledMsg.setSentTime(LocalDateTime.now());
                scheduledMessageMapper.updateById(scheduledMsg);

                log.info("定时消息发送成功: scheduledId={}, messageId={}",
                        scheduledMsg.getId(), messageVO.getId());

            } catch (Exception e) {
                log.error("定时消息发送失败: scheduledId={}", scheduledMsg.getId(), e);

                // 更新失败状态
                scheduledMsg.setStatus(ImScheduledMessage.STATUS_FAILED);
                scheduledMsg.setErrorMsg(e.getMessage());
                scheduledMsg.setRetryCount(scheduledMsg.getRetryCount() + 1);
                scheduledMessageMapper.updateById(scheduledMsg);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScheduledMessage(Long id, Long userId) {
        LambdaQueryWrapper<ImScheduledMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImScheduledMessage::getId, id)
                .eq(ImScheduledMessage::getUserId, userId);

        ImScheduledMessage scheduledMessage = scheduledMessageMapper.selectOne(wrapper);
        if (scheduledMessage == null) {
            throw new IllegalArgumentException("定时消息不存在");
        }

        // 只允许删除已发送或已取消的消息
        if (ImScheduledMessage.STATUS_PENDING.equals(scheduledMessage.getStatus())) {
            throw new IllegalArgumentException("待发送的消息请先取消再删除");
        }

        scheduledMessageMapper.deleteById(id);
        log.info("删除定时消息: id={}, userId={}", id, userId);
    }

    /**
     * 定时任务：每10秒检查一次待发送的定时消息
     */
    @Scheduled(fixedDelay = 10000)
    public void scheduledTask() {
        processScheduledMessages();
    }

    private ScheduledMessageVO toVO(ImScheduledMessage entity) {
        ScheduledMessageVO vo = new ScheduledMessageVO();
        vo.setId(entity.getId());
        vo.setConversationId(entity.getConversationId());
        vo.setMessageType(entity.getMessageType());
        vo.setContent(entity.getContent());
        vo.setFileUrl(entity.getFileUrl());
        vo.setFileName(entity.getFileName());
        vo.setScheduledTime(entity.getScheduledTime());
        vo.setStatus(entity.getStatus());
        vo.setSentMessageId(entity.getSentMessageId());
        vo.setSentTime(entity.getSentTime());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
