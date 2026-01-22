package com.ruoyi.web.service;

import com.ruoyi.web.domain.dto.MessageSendDTO;
import com.ruoyi.web.domain.ImMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息重试服务
 * 处理消息发送失败时的重试逻辑
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Service
public class MessageRetryService {

    private static final Logger logger = LoggerFactory.getLogger(MessageRetryService.class);
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_BASE = 1000; // 1秒基础延迟
    private static final String RETRY_KEY_PREFIX = "message:retry:";
    private static final String BLACKLIST_KEY_PREFIX = "message:blacklist:";
    private static final long BLACKLIST_EXPIRE_TIME = 300; // 5分钟黑名单时间
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private IImMessageService imMessageService;
    
    /**
     * 带重试机制的消息发送
     */
    public ImMessage sendWithRetry(MessageSendDTO dto) {
        String retryKey = RETRY_KEY_PREFIX + generateMessageKey(dto);
        
        // 检查是否在黑名单中
        if (isInBlacklist(dto)) {
            logger.warn("消息内容包含敏感词，拒绝发送: {}", dto.getContent());
            throw new RuntimeException("消息包含敏感内容，无法发送");
        }
        
        AtomicInteger retryCount = new AtomicInteger(0);
        
        while (retryCount.get() < MAX_RETRY_ATTEMPTS) {
            try {
                // 清除之前的重试记录
                redisTemplate.delete(retryKey);
                
                ImMessage message = imMessageService.sendMessage(dto);
                
                // 发送成功，清除重试计数
                redisTemplate.delete(retryKey);
                logger.info("消息发送成功，消息ID: {}, 重试次数: {}", message.getId(), retryCount.get());
                
                return message;
                
            } catch (Exception e) {
                retryCount.incrementAndGet();
                logger.warn("消息发送失败，第{}次重试: {}", retryCount.get(), e.getMessage());
                
                // 记录重试信息
                redisTemplate.opsForValue().set(retryKey, retryCount.get(), 60, TimeUnit.SECONDS);
                
                if (retryCount.get() >= MAX_RETRY_ATTEMPTS) {
                    // 超过最大重试次数，加入黑名单
                    addToBlacklist(dto);
                    logger.error("消息发送失败，超过最大重试次数，加入黑名单: {}", dto.getContent());
                    throw new RuntimeException("消息发送失败，已超过最大重试次数");
                }
                
                // 等待后重试
                try {
                    long delay = calculateRetryDelay(retryCount.get());
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试等待被中断");
                }
            }
        }
        
        throw new RuntimeException("消息发送失败，未知原因");
    }
    
    /**
     * 检查消息是否在黑名单中
     */
    private boolean isInBlacklist(MessageSendDTO dto) {
        String blacklistKey = BLACKLIST_KEY_PREFIX + generateMessageKey(dto);
        return redisTemplate.hasKey(blacklistKey);
    }
    
    /**
     * 将消息加入黑名单
     */
    private void addToBlacklist(MessageSendDTO dto) {
        String blacklistKey = BLACKLIST_KEY_PREFIX + generateMessageKey(dto);
        redisTemplate.opsForValue().set(blacklistKey, System.currentTimeMillis(), BLACKLIST_EXPIRE_TIME, TimeUnit.SECONDS);
    }
    
    /**
     * 生成消息唯一标识
     */
    private String generateMessageKey(MessageSendDTO dto) {
        // 使用会话ID、发送者ID、内容前100字符生成唯一key
        String content = dto.getContent() != null && dto.getContent().length() > 100 ? 
                dto.getContent().substring(0, 100) : dto.getContent();
        return dto.getConversationId() + ":" + dto.getSenderId() + ":" + content.hashCode();
    }
    
    /**
     * 计算重试延迟时间（指数退避）
     */
    private long calculateRetryDelay(int retryCount) {
        // 指数退避: 1秒, 2秒, 4秒
        return RETRY_DELAY_BASE * (long) Math.pow(2, retryCount - 1);
    }
    
    /**
     * 获取消息重试状态
     */
    public MessageRetryStatus getRetryStatus(MessageSendDTO dto) {
        String retryKey = RETRY_KEY_PREFIX + generateMessageKey(dto);
        Integer retryCount = (Integer) redisTemplate.opsForValue().get(retryKey);
        
        boolean isBlacklisted = isInBlacklist(dto);
        boolean isRetrying = retryCount != null && retryCount > 0;
        
        return new MessageRetryStatus(isRetrying, retryCount != null ? retryCount : 0, isBlacklisted);
    }
    
    /**
     * 清除消息重试状态
     */
    public void clearRetryStatus(MessageSendDTO dto) {
        String retryKey = RETRY_KEY_PREFIX + generateMessageKey(dto);
        redisTemplate.delete(retryKey);
    }
    
    /**
     * 重置消息黑名单状态
     */
    public void resetBlacklistStatus(MessageSendDTO dto) {
        String blacklistKey = BLACKLIST_KEY_PREFIX + generateMessageKey(dto);
        redisTemplate.delete(blacklistKey);
    }
    
    /**
     * 消息重试状态类
     */
    public static class MessageRetryStatus {
        private boolean isRetrying;
        private int retryCount;
        private boolean isBlacklisted;
        
        public MessageRetryStatus(boolean isRetrying, int retryCount, boolean isBlacklisted) {
            this.isRetrying = isRetrying;
            this.retryCount = retryCount;
            this.isBlacklisted = isBlacklisted;
        }
        
        public boolean isRetrying() {
            return isRetrying;
        }
        
        public int getRetryCount() {
            return retryCount;
        }
        
        public boolean isBlacklisted() {
            return isBlacklisted;
        }
        
        public boolean canRetry() {
            return !isBlacklisted && retryCount < MAX_RETRY_ATTEMPTS;
        }
    }
}