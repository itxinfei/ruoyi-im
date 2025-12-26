package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImSensitiveEvent;
import com.ruoyi.im.constant.MessageType;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.IImMessageService;
import com.ruoyi.im.service.IImSensitiveWordService;
import com.ruoyi.im.service.IImSensitiveEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImMessageServiceImpl extends ServiceImpl<ImMessageMapper, ImMessage> implements IImMessageService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);

    @Autowired
    private ImMessageMapper imMessageMapper;
    
    @Autowired
    private IImSensitiveWordService sensitiveWordService;
    
    @Autowired
    private IImSensitiveEventService sensitiveEventService;
    
    public ImMessageServiceImpl() {
        super();
    }
    
    public ImMessageServiceImpl(ImMessageMapper imMessageMapper) {
        super();
        this.imMessageMapper = imMessageMapper;
    }

    /**
     * 发送消息
     * 
     * @param message 消息信息
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean sendMessage(ImMessage message) {
        // 敏感词检测
        if (MessageType.TEXT.equals(message.getType()) && message.getContent() != null) {
            Map<String, Object> detectResult = sensitiveWordService.detectSensitiveWords(message.getContent());
            String sensitiveLevel = (String) detectResult.get("level");
            
            if ("BLOCK".equals(sensitiveLevel)) {
                // BLOCK级别直接拒绝发送
                message.setSensitiveLevel("BLOCK");
                // 记录敏感事件
                recordSensitiveEvent(message, detectResult);
                return false; // 拒绝发送
            } else if ("WARN".equals(sensitiveLevel)) {
                // WARN级别标记但允许发送
                message.setSensitiveLevel("WARN");
                // 记录敏感事件
                recordSensitiveEvent(message, detectResult);
            }
        }
        
        message.setStatus("SENT");
        message.setCreateTime(new Date());
        return save(message);
    }

    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean revokeMessage(Long messageId, Long userId) {
        ImMessage message = getById(messageId);
        if (message == null || !message.getSenderId().equals(userId)) {
            return false;
        }
        
        // 检查是否可以撤回（通常限制在发送后一定时间内）
        if (!canRevokeMessage(messageId, userId)) {
            return false;
        }
        
        Date revokedTime = new Date();
        return imMessageMapper.revokeMessage(messageId, revokedTime) > 0;
    }

    /**
     * 批量撤回消息
     * 
     * @param messageIds 消息ID列表
     * @param userId 操作用户ID
     * @return 撤回数量
     */
    @Override
    @Transactional
    public int revokeMessageBatch(List<Long> messageIds, Long userId) {
        int count = 0;
        for (Long messageId : messageIds) {
            if (revokeMessage(messageId, userId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 查询会话消息列表（分页）
     * 
     * @param conversationId 会话ID
     * @param userId 查询用户ID
     * @param lastMessageId 最后消息ID（用于分页）
     * @param limit 限制数量
     * @return 消息列表
     */
    @Override
    public List<ImMessage> selectConversationMessages(Long conversationId, Long userId, Long lastMessageId, int limit) {
        return imMessageMapper.selectConversationMessages(conversationId, userId, lastMessageId, limit);
    }

    /**
     * 获取消息详情
     * 
     * @param messageId 消息ID
     * @param userId 查询用户ID
     * @return 消息详情
     */
    @Override
    public ImMessage getMessageDetail(Long messageId, Long userId) {
        return imMessageMapper.selectMessageDetail(messageId, userId);
    }

    /**
     * 统计会话消息数量
     * 
     * @param conversationId 会话ID
     * @return 消息数量
     */
    @Override
    public int countConversationMessages(Long conversationId) {
        QueryWrapper<ImMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId);
        return Math.toIntExact(baseMapper.selectCount(queryWrapper));
    }

    /**
     * 统计未读消息数量
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadMessageId 最后已读消息ID
     * @return 未读消息数量
     */
    @Override
    public int countUnreadMessages(Long conversationId, Long userId, Long lastReadMessageId) {
        return imMessageMapper.countUnreadMessages(conversationId, userId, lastReadMessageId);
    }

    /**
     * 获取会话最后一条消息
     * 
     * @param conversationId 会话ID
     * @return 最后消息
     */
    @Override
    public ImMessage getLastMessage(Long conversationId) {
        QueryWrapper<ImMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 搜索消息
     * 
     * @param conversationId 会话ID（可选）
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param messageType 消息类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 消息列表
     */
    @Override
    public List<ImMessage> searchMessages(Long conversationId, Long userId, String keyword, String messageType, Date startTime, Date endTime) {
        return imMessageMapper.searchMessages(conversationId, keyword, 100); // 限制返回100条结果
    }

    /**
     * 查询用户发送的消息
     * 
     * @param senderId 发送者ID
     * @param messageType 消息类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 消息列表
     */
    @Override
    public List<ImMessage> selectUserSentMessages(Long senderId, String messageType, Date startTime, Date endTime) {
        return imMessageMapper.selectUserMessages(senderId, startTime, endTime, 100); // 限制返回100条结果
    }

    /**
     * 批量更新消息状态
     * 
     * @param messageIds 消息ID列表
     * @param status 新状态
     * @return 更新数量
     */
    @Override
    @Transactional
    public int updateMessageStatusBatch(List<Long> messageIds, String status) {
        return imMessageMapper.updateStatusBatch(messageIds, status);
    }

    /**
     * 查询敏感消息
     * 
     * @param sensitiveLevel 敏感级别
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 敏感消息列表
     */
    @Override
    public List<ImMessage> selectSensitiveMessages(String sensitiveLevel, Date startTime, Date endTime, Integer limit) {
        return imMessageMapper.selectSensitiveMessages(sensitiveLevel, startTime, endTime, limit);
    }

    /**
     * 查询文件消息
     * 
     * @param conversationId 会话ID
     * @param limit 限制数量
     * @return 文件消息列表
     */
    @Override
    public List<ImMessage> selectFileMessages(Long conversationId, Integer limit) {
        return imMessageMapper.selectFileMessages(conversationId, limit);
    }

    /**
     * 统计用户发送的消息数量
     * 
     * @param senderId 发送者ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息数量
     */
    @Override
    public int countUserSentMessages(Long senderId, Date startTime, Date endTime) {
        return imMessageMapper.countUserSentMessages(senderId, startTime, endTime);
    }

    /**
     * 清理过期消息
     * 
     * @param days 过期天数
     * @return 清理数量
     */
    @Override
    @Transactional
    public int cleanExpiredMessages(int days) {
        return imMessageMapper.deleteExpiredMessages(days);
    }

    /**
     * 获取消息统计信息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getMessageStatistics(Date startTime, Date endTime) {
        List<Object> statistics = imMessageMapper.selectMessageStatistics(startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        // 处理统计结果
        return result;
    }

    /**
     * 删除会话的所有消息
     * 
     * @param conversationId 会话ID
     * @return 删除数量
     */
    @Override
    @Transactional
    public int deleteConversationAllMessages(Long conversationId) {
        return imMessageMapper.deleteByConversationId(conversationId);
    }

    /**
     * 删除用户发送的所有消息
     * 
     * @param senderId 发送者ID
     * @return 删除数量
     */
    @Override
    @Transactional
    public int deleteUserAllMessages(Long senderId) {
        QueryWrapper<ImMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sender_id", senderId);
        return Math.toIntExact(baseMapper.delete(queryWrapper));
    }

    /**
     * 检查消息是否可以撤回
     * 
     * @param messageId 消息ID
     * @param userId 操作用户ID
     * @return 是否可以撤回
     */
    @Override
    public boolean canRevokeMessage(Long messageId, Long userId) {
        ImMessage message = getById(messageId);
        if (message == null || !message.getSenderId().equals(userId)) {
            return false;
        }
        
        // 检查消息状态
        if ("REVOKED".equals(message.getStatus())) {
            return false;
        }
        
        // 检查时间限制（例如：发送后2分钟内可撤回）
        long timeDiff = System.currentTimeMillis() - message.getCreateTime().getTime();
        return timeDiff <= 2 * 60 * 1000; // 2分钟
    }

    /**
     * 标记消息为已读
     * 
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean markMessageAsRead(Long conversationId, Long userId, Long messageId) {
        // 这里可以实现消息已读状态的更新逻辑
        // 通常会更新会话成员表中的最后已读消息ID
        return true;
    }
    
    /**
     * 记录敏感事件
     */
    private void recordSensitiveEvent(ImMessage message, Map<String, Object> detectResult) {
        try {
            ImSensitiveEvent event = new ImSensitiveEvent();
            event.setUserId(message.getSenderId());
            event.setMessageId(message.getId());
            event.setLevel((String) detectResult.get("level"));
            event.setCreateTime(new Date());
            // 设置wordId，需要从detectResult中获取
            Object wordId = detectResult.get("wordId");
            if (wordId != null) {
                event.setWordId(Long.valueOf(wordId.toString()));
            }
            sensitiveEventService.save(event);
        } catch (Exception e) {
            // 记录敏感事件失败不应影响消息发送
            log.error("记录敏感词事件失败, messageId={}", message.getId(), e);
        }
    }
}