package com.ruoyi.im.service.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.utils.ValidationUtils;

/**
 * 会话Service业务层处理 - 优化版本
 * 优化内容：添加缓存机制、事务控制、异步处理、性能监控、错误处理
 * 
 * @author ruoyi
 */
@Service
public class ImConversationServiceImpl extends EnhancedBaseServiceImpl<ImConversation, ImConversationMapper> implements ImConversationService {
    private static final Logger log = LoggerFactory.getLogger(ImConversationServiceImpl.class);
    
    @Autowired
    private ImConversationMapper imConversationMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存键前缀
    private static final String CONVERSATION_BY_TYPE_TARGET_CACHE_PREFIX = "im:conversation:typeTarget:";
    private static final String USER_CONVERSATIONS_CACHE_PREFIX = "im:user:conversations:";
    
    // 缓存超时时间（分钟）
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    // 实现EnhancedBaseServiceImpl的抽象方法
    @Override
    protected String getEntityType() {
        return "conversation";
    }
    
    @Override
    protected Long getEntityId(ImConversation entity) {
        return entity != null ? entity.getId() : null;
    }
    
    @Override
    protected void setCreateTime(ImConversation entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(java.time.LocalDateTime.now());
        }
    }
    
    @Override
    protected void setUpdateTime(ImConversation entity) {
        if (entity != null) {
            entity.setUpdateTime(java.time.LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl中的clearRelatedCache方法，提供会话特定缓存清理逻辑
     * 
     * @param entity 会话实体
     */
    @Override
    protected void clearRelatedCache(ImConversation entity) {
        if (entity != null) {
            // 清除实体缓存
            clearEntityCache(entity.getId());
            
            // 清除按类型和目标ID查找的缓存
            if (entity.getType() != null && entity.getTargetId() != null) {
                clearConversationByTypeTargetCache(entity.getType(), entity.getTargetId());
            }
            
            // 清除用户会话列表缓存
            // 注意：这里可能需要根据实际业务逻辑清除更多相关缓存
        }
    }
    
    /**
     * 清除按类型和目标ID查找的会话缓存
     * 
     * @param type 会话类型
     * @param targetId 目标ID
     */
    private void clearConversationByTypeTargetCache(String type, Long targetId) {
        try {
            String cacheKey = CONVERSATION_BY_TYPE_TARGET_CACHE_PREFIX + type + ":" + targetId;
            redisTemplate.delete(cacheKey);
            log.debug("已清除按类型和目标ID查找的会话缓存: type={}, targetId={}", type, targetId);
        } catch (Exception e) {
            log.warn("清除按类型和目标ID查找的会话缓存失败: type={}, targetId={}, error={}", type, targetId, e.getMessage());
        }
    }
    
    /**
     * 根据用户ID查询会话列表
     * 
     * @param userId 用户ID
     * @param type 会话类型
     * @return 会话集合
     */
    @Override
    public List<ImConversation> selectImConversationListByUserId(Long userId, String type) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImConversationListByUserId";
        
        try {
            // 参数验证
            ValidationUtils.validateId(userId, methodName);
            
            // 生成缓存键
            String cacheKey = generateUserConversationsCacheKey(userId, type);
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImConversation> cachedConversations = (List<ImConversation>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedConversations != null) {
                log.debug("从缓存获取用户会话列表: userId={}, type={}, method={}", userId, type, methodName);
                return cachedConversations;
            }
            
            // 查询数据库
            List<ImConversation> conversations = imConversationMapper.selectImConversationListByUserId(userId, type);
            
            // 缓存结果
            if (conversations != null && !conversations.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, conversations, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("用户会话列表已缓存: userId={}, type={}, count={}, method={}", userId, type, conversations.size(), methodName);
            }
            
            return conversations;
            
        } catch (Exception e) {
            log.error("查询用户会话列表异常: userId={}, type={}, error={}", userId, type, e.getMessage(), e);
            throw new RuntimeException("查询用户会话列表失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("查询用户会话列表耗时: {}ms, userId={}, type={}, method={}", duration, userId, type, methodName);
        }
    }
    
    /**
     * 生成用户会话列表缓存键
     * 
     * @param userId 用户ID
     * @param type 会话类型
     * @return 缓存键
     */
    private String generateUserConversationsCacheKey(Long userId, String type) {
        return USER_CONVERSATIONS_CACHE_PREFIX + userId + ":" + (type != null ? type : "all");
    }
    
    /**
     * 根据会话类型和目标ID查询会话
     * 
     * @param type 会话类型
     * @param targetId 目标ID
     * @return 会话
     */
    @Override
    public ImConversation selectImConversationByTypeAndTargetId(String type, Long targetId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImConversationByTypeAndTargetId";
        
        try {
            // 参数验证
            ValidationUtils.validateString(type, "会话类型", methodName);
            ValidationUtils.validateId(targetId, methodName);
            
            // 生成缓存键
            String cacheKey = CONVERSATION_BY_TYPE_TARGET_CACHE_PREFIX + type + ":" + targetId;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            ImConversation cachedConversation = (ImConversation) redisTemplate.opsForValue().get(cacheKey);
            if (cachedConversation != null) {
                log.debug("从缓存获取按类型和目标ID查找的会话: type={}, targetId={}, method={}", type, targetId, methodName);
                return cachedConversation;
            }
            
            // 查询数据库
            ImConversation conversation = imConversationMapper.selectImConversationByTypeAndTargetId(type, targetId);
            
            // 缓存结果
            if (conversation != null) {
                redisTemplate.opsForValue().set(cacheKey, conversation, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("按类型和目标ID查找的会话已缓存: type={}, targetId={}, method={}", type, targetId, methodName);
            }
            
            return conversation;
            
        } catch (Exception e) {
            log.error("按类型和目标ID查询会话异常: type={}, targetId={}, error={}", type, targetId, e.getMessage(), e);
            throw new RuntimeException("按类型和目标ID查询会话失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("按类型和目标ID查询会话耗时: {}ms, type={}, targetId={}, method={}", duration, type, targetId, methodName);
        }
    }
    
    /**
     * 创建私聊会话
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @return 会话
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImConversation createPrivateConversation(Long userId, Long friendUserId) {
        long startTime = System.currentTimeMillis();
        String methodName = "createPrivateConversation";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            validateId(friendUserId, methodName);
            
            // 确保用户ID和好友ID不同
            if (userId.equals(friendUserId)) {
                throw new RuntimeException(methodName + "参数无效: 用户不能与自己创建私聊会话");
            }
            
            // 检查是否已存在私聊会话（使用较小的ID作为目标ID以保证一致性）
            Long targetId = Math.min(userId, friendUserId);
            ImConversation existingConversation = selectImConversationByTypeAndTargetId("PRIVATE", targetId);
            if (existingConversation != null) {
                log.debug("私聊会话已存在: userId={}, friendUserId={}, targetId={}, method={}", userId, friendUserId, targetId, methodName);
                return existingConversation;
            }
            
            // 创建新的私聊会话
            ImConversation conversation = new ImConversation();
            conversation.setType("PRIVATE");
            conversation.setTargetId(targetId);
            // 设置会话名称和头像（如果有）
            
            // 异步处理会话名称和头像（不阻塞主流程）
            executeAsync(() -> {
                try {
                    // 这里可以添加获取会话名称和头像的逻辑
                    // 例如：从用户信息中获取昵称组合等
                    log.debug("异步处理会话信息: conversationId={}", conversation.getId());
                } catch (Exception e) {
                    log.warn("异步处理会话信息失败: {}", e.getMessage());
                }
            });
            
            // 插入会话
            int result = insert(conversation);
            
            if (result > 0) {
                log.info("创建私聊会话成功: userId={}, friendUserId={}, targetId={}, conversationId={}, result={}", 
                         userId, friendUserId, targetId, conversation.getId(), result);
            } else {
                throw new RuntimeException("创建私聊会话失败");
            }
            
            return conversation;
            
        } catch (Exception e) {
            log.error("创建私聊会话异常: userId={}, friendUserId={}, error={}", userId, friendUserId, e.getMessage(), e);
            throw new RuntimeException("创建私聊会话失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("创建私聊会话耗时: {}ms, userId={}, friendUserId={}, method={}", duration, userId, friendUserId, methodName);
        }
    }
    
    /**
     * 创建群聊会话
     * 
     * @param groupId 群组ID
     * @return 会话
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImConversation createGroupConversation(Long groupId) {
        long startTime = System.currentTimeMillis();
        String methodName = "createGroupConversation";
        
        try {
            // 参数验证
            validateId(groupId, methodName);
            
            // 检查是否已存在群聊会话
            ImConversation existingConversation = selectImConversationByTypeAndTargetId("GROUP", groupId);
            if (existingConversation != null) {
                log.debug("群聊会话已存在: groupId={}, method={}", groupId, methodName);
                return existingConversation;
            }
            
            // 创建新的群聊会话
            ImConversation conversation = new ImConversation();
            conversation.setType("GROUP");
            conversation.setTargetId(groupId);
            // 设置会话名称和头像（如果有）
            
            // 异步处理会话名称和头像（不阻塞主流程）
            executeAsync(() -> {
                try {
                    // 这里可以添加获取群组名称和头像的逻辑
                    log.debug("异步处理群组会话信息: conversationId={}", conversation.getId());
                } catch (Exception e) {
                    log.warn("异步处理群组会话信息失败: {}", e.getMessage());
                }
            });
            
            // 插入会话
            int result = insert(conversation);
            
            if (result > 0) {
                log.info("创建群聊会话成功: groupId={}, conversationId={}, result={}", 
                         groupId, conversation.getId(), result);
            } else {
                throw new RuntimeException("创建群聊会话失败");
            }
            
            return conversation;
            
        } catch (Exception e) {
            log.error("创建群聊会话异常: groupId={}, error={}", groupId, e.getMessage(), e);
            throw new RuntimeException("创建群聊会话失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("创建群聊会话耗时: {}ms, groupId={}, method={}", duration, groupId, methodName);
        }
    }
    
    /**
     * 更新会话最后消息ID
     * 
     * @param conversationId 会话ID
     * @param lastMessageId 最后消息ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateConversationLastMessage(Long conversationId, Long lastMessageId) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateConversationLastMessage";
        
        try {
            // 参数验证
            validateId(conversationId, methodName);
            validateId(lastMessageId, methodName);
            
            // 查询会话
            ImConversation conversation = selectById(conversationId);
            if (conversation == null) {
                log.warn("会话不存在: conversationId={}, method={}", conversationId, methodName);
                return 0;
            }
            
            // 检查是否需要更新
            if (conversation.getLastMessageId() != null && conversation.getLastMessageId().equals(lastMessageId)) {
                log.debug("会话最后消息ID已是最新，无需更新: conversationId={}, lastMessageId={}, method={}", 
                         conversationId, lastMessageId, methodName);
                return 0;
            }
            
            // 更新会话最后消息ID
            conversation.setLastMessageId(lastMessageId);
            
            // 使用EnhancedBaseServiceImpl的update方法，该方法会自动更新缓存
            int result = update(conversation);
            
            if (result > 0) {
                log.info("更新会话最后消息ID成功: conversationId={}, lastMessageId={}, result={}, method={}", 
                         conversationId, lastMessageId, result, methodName);
                
                // 异步处理额外任务（如果需要）
                executeAsync(() -> {
                    try {
                        // 这里可以添加其他相关处理，例如：
                        // - 发送WebSocket消息通知客户端
                        // - 更新会话的未读消息计数
                        // - 记录操作日志等
                        log.debug("异步处理会话最后消息ID更新完成: conversationId={}", conversationId);
                    } catch (Exception e) {
                        log.warn("异步处理会话最后消息ID更新失败: conversationId={}, error={}", conversationId, e.getMessage());
                    }
                });
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("更新会话最后消息ID异常: conversationId={}, lastMessageId={}, error={}", 
                     conversationId, lastMessageId, e.getMessage(), e);
            throw new RuntimeException("更新会话最后消息ID失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新会话最后消息ID耗时: {}ms, conversationId={}, method={}", duration, conversationId, methodName);
        }
    }
}