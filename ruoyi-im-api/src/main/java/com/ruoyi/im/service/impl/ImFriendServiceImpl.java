package com.ruoyi.im.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.utils.ValidationUtils;
import com.ruoyi.im.utils.CacheUtils;

/**
 * 好友Service业务层处理 - 优化版本
 * 优化内容：添加缓存机制、事务控制、性能监控、错误处理
 * 
 * @author ruoyi
 */
@Service
public class ImFriendServiceImpl extends EnhancedBaseServiceImpl<ImFriend, ImFriendMapper> implements ImFriendService {
    private static final Logger log = LoggerFactory.getLogger(ImFriendServiceImpl.class);
    
    @Autowired
    private ImFriendMapper imFriendMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存键前缀
    private static final String USER_FRIENDS_CACHE_PREFIX = "im:user:friends:";
    private static final String FRIEND_RELATION_CACHE_PREFIX = "im:friend:relation:";
    
    // 缓存超时时间（分钟）
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    // 实现EnhancedBaseServiceImpl的抽象方法
    @Override
    protected String getEntityType() {
        return "friend";
    }
    
    @Override
    protected Long getEntityId(ImFriend entity) {
        return entity != null ? entity.getId() : null;
    }
    
    @Override
    protected void setCreateTime(ImFriend entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(java.time.LocalDateTime.now());
        }
    }
    
    @Override
    protected void setUpdateTime(ImFriend entity) {
        if (entity != null) {
            entity.setUpdateTime(java.time.LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl中的clearRelatedCache方法，提供好友特定缓存清理逻辑
     * 
     * @param entity 好友实体
     */
    @Override
    protected void clearRelatedCache(ImFriend entity) {
        if (entity != null) {
            // 清除实体缓存
            clearEntityCache(entity.getId());
            
            // 清除用户好友列表缓存
            if (entity.getUserId() != null) {
                clearUserFriendsCache(entity.getUserId());
            }
            
            // 清除好友关系缓存
            if (entity.getFriendUserId() != null) {
                clearFriendRelationCache(entity.getUserId(), entity.getFriendUserId());
            }
        }
    }
    
    /**
     * 清除用户好友列表缓存
     * 
     * @param userId 用户ID
     */
    private void clearUserFriendsCache(Long userId) {
        try {
            String cacheKey = USER_FRIENDS_CACHE_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("已清除用户好友列表缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户好友列表缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 清除好友关系缓存
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     */
    private void clearFriendRelationCache(Long userId, Long friendUserId) {
        try {
            String cacheKey = FRIEND_RELATION_CACHE_PREFIX + userId + ":" + friendUserId;
            redisTemplate.delete(cacheKey);
            log.debug("已清除好友关系缓存: userId={}, friendUserId={}", userId, friendUserId);
        } catch (Exception e) {
            log.warn("清除好友关系缓存失败: userId={}, friendUserId={}, error={}", userId, friendUserId, e.getMessage());
        }
    }

    /**
     * 删除好友信息
     * 
     * @param id 好友ID
     * @return 结果
     */
    @Override
    public int deleteImFriendById(Long id) {
        return deleteById(id);
    }
    
    /**
     * 根据用户ID查询好友列表
     * 
     * @param userId 用户ID
     * @return 好友集合
     */
    @Override
    public List<ImFriend> selectImFriendListByUserId(Long userId) {
        return imFriendMapper.selectImFriendListByUserId(userId);
    }
    
    /**
     * 根据用户ID和好友用户ID查询好友关系
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @return 好友关系
     */
    @Override
    public ImFriend selectImFriendByUserIdAndFriendUserId(Long userId, Long friendUserId) {
        return imFriendMapper.selectImFriendByUserIdAndFriendUserId(userId, friendUserId);
    }
    
    /**
     * 添加好友
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @param alias 别名
     * @param remark 备注
     * @return 结果
     */
    @Override
    public int addFriend(Long userId, Long friendUserId, String alias, String remark) {
        long startTime = System.currentTimeMillis();
        String methodName = "addFriend";
        
        try {
            // 参数验证
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateId(friendUserId, methodName);
            
            // 检查是否已经是好友
            ImFriend existingFriend = selectImFriendByUserIdAndFriendUserId(userId, friendUserId);
            if (existingFriend != null) {
                // 已经是好友，不需要重复添加
                log.debug("用户已经是好友: userId={}, friendUserId={}", userId, friendUserId);
                return 0;
            }
            
            // 创建好友关系（双向）
            ImFriend friend1 = new ImFriend();
            friend1.setUserId(userId);
            friend1.setFriendUserId(friendUserId);
            friend1.setAlias(alias);
            friend1.setRemark(remark);
            friend1.setStatus("ACTIVE");
            
            ImFriend friend2 = new ImFriend();
            friend2.setUserId(friendUserId);
            friend2.setFriendUserId(userId);
            friend2.setAlias(null); // 对方不设置别名
            friend2.setRemark(null); // 对方不设置备注
            friend2.setStatus("ACTIVE");
            
            int result = insert(friend1);
            if (result > 0) {
                result += insert(friend2);
            }
            
            log.info("添加好友成功: userId={}, friendUserId={}, result={}", userId, friendUserId, result);
            return result;
            
        } catch (Exception e) {
            log.error("添加好友异常: userId={}, friendUserId={}, error={}", userId, friendUserId, e.getMessage(), e);
            throw new BusinessException("添加好友失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("添加好友耗时: {}ms, userId={}, friendUserId={}", duration, userId, friendUserId);
        }
    }
    
    /**
     * 删除好友
     * 
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @return 结果
     */
    @Override
    public int deleteFriend(Long userId, Long friendUserId) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteFriend";
        
        try {
            // 参数验证
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateId(friendUserId, methodName);
            
            int result = 0;
            
            // 删除用户的好友关系
            ImFriend friend1 = selectImFriendByUserIdAndFriendUserId(userId, friendUserId);
            if (friend1 != null) {
                result += deleteById(friend1.getId());
            }
            
            // 删除对方的好友关系
            ImFriend friend2 = selectImFriendByUserIdAndFriendUserId(friendUserId, userId);
            if (friend2 != null) {
                result += deleteById(friend2.getId());
            }
            
            log.info("删除好友成功: userId={}, friendUserId={}, result={}", userId, friendUserId, result);
            return result;
            
        } catch (Exception e) {
            log.error("删除好友异常: userId={}, friendUserId={}, error={}", userId, friendUserId, e.getMessage(), e);
            throw new BusinessException("删除好友失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("删除好友耗时: {}ms, userId={}, friendUserId={}", duration, userId, friendUserId);
        }
    }
}