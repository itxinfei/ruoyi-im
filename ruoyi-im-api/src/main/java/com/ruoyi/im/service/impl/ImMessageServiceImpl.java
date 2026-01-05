package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.utils.ValidationUtils;

/**
 * 消息Service业务层处理 - 优化版本
 * 优化内容：添加缓存机制、分页查询优化、批量操作、事务控制、性能监控
 * 
 * @author ruoyi
 */
@Service
public class ImMessageServiceImpl extends EnhancedBaseServiceImpl<ImMessage, ImMessageMapper> implements ImMessageService {
    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);
    
    @Autowired
    private ImMessageMapper imMessageMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存键前缀
    private static final String MESSAGE_CACHE_PREFIX = "im:message:";
    private static final String CONVERSATION_MESSAGES_PREFIX = "im:conversation:";
    
    // 缓存超时时间（分钟）
    private static final int CACHE_TIMEOUT_MINUTES = 10;
    
    @PostConstruct
    public void init() {
        log.info("ImMessageService初始化完成，开始启用缓存和性能监控");
    }
    
    /**
     * 根据会话ID查询消息列表 - 优化版本
     * 优化内容：分页支持、缓存机制、参数验证、性能监控
     * 
     * @param conversationId 会话ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 消息集合
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId, Integer pageNum, Integer pageSize) {
        long startTime = System.currentTimeMillis();
        log.debug("开始查询会话消息: conversationId={}, pageNum={}, pageSize={}", conversationId, pageNum, pageSize);
        
        try {
            // 参数验证
            ValidationUtils.validateConversationId(conversationId, "selectImMessageListByConversationId");
            ValidationUtils.validatePaginationParams(pageNum, pageSize, "selectImMessageListByConversationId");
            
            // 计算偏移量
            int offset = (pageNum - 1) * pageSize;
            
            // 检查缓存
            String cacheKey = CONVERSATION_MESSAGES_PREFIX + conversationId + ":" + pageNum + ":" + pageSize;
            @SuppressWarnings("unchecked")
            List<ImMessage> cachedMessages = (List<ImMessage>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedMessages != null) {
                log.debug("从缓存获取会话消息: conversationId={}, count={}", conversationId, cachedMessages.size());
                return cachedMessages;
            }
            
            // 查询数据库
            List<ImMessage> messages = imMessageMapper.selectImMessageListByConversationIdAndPagination(conversationId, offset, pageSize);
            
            // 缓存结果
            if (messages != null && !messages.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, messages, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("会话消息已缓存: conversationId={}, count={}", conversationId, messages.size());
            }
            
            return messages;
            
        } catch (Exception e) {
            log.error("查询会话消息异常: conversationId={}, error={}", conversationId, e.getMessage(), e);
            throw new BusinessException("消息查询失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("查询会话消息耗时: {}ms, conversationId={}, pageNum={}, pageSize={}", 
                     duration, conversationId, pageNum, pageSize);
        }
    }
    

    
    /**
     * 清除会话消息缓存
     * 
     * @param conversationId 会话ID
     */
    private void clearConversationMessagesCache(Long conversationId) {
        try {
            String pattern = CONVERSATION_MESSAGES_PREFIX + conversationId + ":*";
            redisTemplate.delete(pattern);
            log.debug("已清除会话消息缓存: conversationId={}", conversationId);
        } catch (Exception e) {
            log.warn("清除会话消息缓存失败: conversationId={}, error={}", conversationId, e.getMessage());
        }
    }
    
    /**
     * 根据会话ID查询消息列表（兼容旧版本）
     * 
     * @param conversationId 会话ID
     * @return 消息集合
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationId(Long conversationId) {
        return selectImMessageListByConversationId(conversationId, 1, 50);
    }
    
    /**
     * 根据会话ID和时间范围查询消息列表
     * 
     * @param conversationId 会话ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息集合
     */
    @Override
    public List<ImMessage> selectImMessageListByConversationIdAndTimeRange(Long conversationId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        return imMessageMapper.selectImMessageListByConversationIdAndTimeRange(conversationId, startTime, endTime);
    }
    
    /**
     * 发送消息 - 优化版本
     * 优化内容：事务控制、参数验证、异步处理、性能监控、缓存更新
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long conversationId, Long senderId, String type, String content) {
        long startTime = System.currentTimeMillis();
        log.info("开始发送消息: conversationId={}, senderId={}, type={}", conversationId, senderId, type);
        
        try {
            // 参数验证
            ValidationUtils.validateConversationId(conversationId, "sendMessage");
            ValidationUtils.validateUserId(senderId, "sendMessage");
            ValidationUtils.validateString(type, "消息类型", "sendMessage");
            ValidationUtils.validateString(content, "消息内容", "sendMessage");
            
            // 验证消息类型
            if (!isValidMessageType(type)) {
                throw new BusinessException("无效的消息类型: " + type);
            }
            
            // 验证内容长度
            if (content.length() > 5000) {
                throw new BusinessException("消息内容长度不能超过5000个字符");
            }
            
            ImMessage message = new ImMessage();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setType(type);
            message.setContent(content);
            message.setStatus("SENT");
            message.setCreateTime(java.time.LocalDateTime.now());
            
            int result = insert(message);
            if (result <= 0) {
                log.error("消息发送失败: conversationId={}, senderId={}", conversationId, senderId);
                throw new BusinessException("消息发送失败");
            }
            
            // 清除会话消息缓存
            clearConversationMessagesCache(conversationId);
            
            // 异步处理消息推送和持久化
            CompletableFuture.runAsync(() -> {
                try {
                    // 消息推送
                    processMessagePush(message);
                    log.debug("消息推送完成: messageId={}", message.getId());
                } catch (Exception e) {
                    log.error("消息推送异常: messageId={}, error={}", message.getId(), e.getMessage(), e);
                }
            });
            
            log.info("消息发送成功: messageId={}, conversationId={}, 耗时={}ms", 
                     message.getId(), conversationId, System.currentTimeMillis() - startTime);
            
            return message.getId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送消息异常: conversationId={}, senderId={}, error={}", conversationId, senderId, e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("消息发送失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("发送消息总耗时: {}ms, conversationId={}, senderId={}", duration, conversationId, senderId);
        }
    }
    

    
    /**
     * 验证消息类型
     * 
     * @param type 消息类型
     * @return 是否有效
     */
    private boolean isValidMessageType(String type) {
        return "TEXT".equals(type) || 
               "IMAGE".equals(type) || 
               "FILE".equals(type) || 
               "VOICE".equals(type) || 
               "VIDEO".equals(type) ||
               "LOCATION".equals(type) ||
               "SYSTEM".equals(type);
    }
    
    /**
     * 处理消息推送
     * 
     * @param message 消息对象
     */
    private void processMessagePush(ImMessage message) {
        try {
            // 这里可以实现WebSocket推送、消息队列等
            // 示例：发送到消息队列进行异步处理
            
            // 记录消息推送日志
            log.debug("处理消息推送: messageId={}, conversationId={}, type={}", 
                     message.getId(), message.getConversationId(), message.getType());
            
            // 可以在这里集成消息推送服务
            // messagePushService.pushMessage(message);
            
        } catch (Exception e) {
            log.error("处理消息推送异常: messageId={}, error={}", message.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 发送私聊消息
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    @Override
    public Long sendPrivateMessage(Long senderId, Long receiverId, String type, String content) {
        // 创建或获取私聊会话
        // 这里使用较小的ID作为目标ID来标识私聊会话
        Long targetId = Math.min(senderId, receiverId);
        // 注意：实际实现中，需要先检查或创建相应的私聊会话
        // 暂时返回null，实际使用时需要先创建会话
        return null;
    }
    
    /**
     * 发送群聊消息
     * 
     * @param senderId 发送者ID
     * @param groupId 群组ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    @Override
    public Long sendGroupMessage(Long senderId, Long groupId, String type, String content) {
        // 创建或获取群聊会话
        // 这里使用群组ID作为目标ID
        // 注意：实际实现中，需要先检查或创建相应的群聊会话
        // 暂时返回null，实际使用时需要先创建会话
        return null;
    }
    
    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int revokeMessage(Long messageId, Long operatorId) {
        ImMessage message = selectById(messageId);
        if (message != null) {
            // 检查操作权限（发送者或管理员才能撤回）
            if (!message.getSenderId().equals(operatorId)) {
                // 非发送者无法撤回
                return 0;
            }
            
            message.setStatus("REVOKED");
            message.setRevokedTime(java.time.LocalDateTime.now());
            return update(message);
        }
        return 0;
    }
    
    /**
     * 更新消息状态
     * 
     * @param messageId 消息ID
     * @param status 新状态
     * @return 结果
     */
    @Override
    public int updateMessageStatus(Long messageId, String status) {
        ImMessage message = selectById(messageId);
        if (message != null) {
            message.setStatus(status);
            return update(message);
        }
        return 0;
    }
    
    /**
     * 发送回复消息
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param replyToMessageId 回复的消息ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    @Override
    public Long sendReplyMessage(Long conversationId, Long senderId, Long replyToMessageId, String type, String content) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        message.setReplyToMessageId(replyToMessageId);
        message.setStatus("SENT");
        
        int result = insert(message);
        if (result > 0) {
            return message.getId();
        }
        return null;
    }
    
    /**
     * 发送转发消息
     * 
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param forwardFromMessageId 转发的消息ID
     * @param type 消息类型
     * @param content 消息内容
     * @return 消息ID
     */
    @Override
    public Long sendForwardMessage(Long conversationId, Long senderId, Long forwardFromMessageId, String type, String content) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        message.setForwardFromMessageId(forwardFromMessageId);
        message.setStatus("SENT");
        
        int result = insert(message);
        if (result > 0) {
            return message.getId();
        }
        return null;
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @return 实体类型名称
     */
    @Override
    protected String getEntityType() {
        return "message";
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 消息实体
     * @return 消息ID
     */
    @Override
    protected Long getEntityId(ImMessage entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 消息实体
     */
    @Override
    protected void setCreateTime(ImMessage entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 消息实体
     */
    @Override
    protected void setUpdateTime(ImMessage entity) {
        if (entity != null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl中的clearRelatedCache方法，提供消息特定缓存清理逻辑
     * 
     * @param entity 消息实体
     */
    @Override
    protected void clearRelatedCache(ImMessage entity) {
        if (entity != null) {
            // 清除实体缓存
            clearEntityCache(entity.getId());
            
            // 清除会话消息缓存
            if (entity.getConversationId() != null) {
                clearConversationMessagesCache(entity.getConversationId());
            }
        }
    }
}