package com.ruoyi.im.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.service.ImGroupMemberService;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.utils.ValidationUtils;

/**
 * 群组成员Service业务层处理 - 优化版本
 * 优化内容：添加缓存机制、事务控制、性能监控、错误处理
 * 
 * @author ruoyi
 */
@Service
public class ImGroupMemberServiceImpl extends EnhancedBaseServiceImpl<ImGroupMember, ImGroupMemberMapper> implements ImGroupMemberService {
    private static final Logger log = LoggerFactory.getLogger(ImGroupMemberServiceImpl.class);
    
    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存键前缀
    private static final String GROUP_MEMBERS_CACHE_PREFIX = "im:group:members:";
    private static final String USER_GROUPS_CACHE_PREFIX = "im:user:groups:";
    private static final String GROUP_MEMBER_CACHE_PREFIX = "im:group:member:";
    
    // 缓存超时时间（分钟）
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    // 实现EnhancedBaseServiceImpl的抽象方法
    @Override
    protected String getEntityType() {
        return "groupMember";
    }
    
    @Override
    protected Long getEntityId(ImGroupMember entity) {
        return entity != null ? entity.getId() : null;
    }
    
    @Override
    protected void setCreateTime(ImGroupMember entity) {
        if (entity != null && entity.getJoinedTime() == null) {
            entity.setJoinedTime(java.time.LocalDateTime.now());
        }
    }
    
    @Override
    protected void setUpdateTime(ImGroupMember entity) {
        if (entity != null) {
            entity.setUpdateTime(java.time.LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl中的clearRelatedCache方法，提供群组成员特定缓存清理逻辑
     * 
     * @param entity 群组成员实体
     */
    @Override
    protected void clearRelatedCache(ImGroupMember entity) {
        if (entity != null) {
            // 清除实体缓存
            clearEntityCache(entity.getId());
            
            // 清除群组成员列表缓存
            if (entity.getGroupId() != null) {
                clearGroupMembersCache(entity.getGroupId());
            }
            
            // 清除用户群组列表缓存
            if (entity.getUserId() != null) {
                clearUserGroupsCache(entity.getUserId());
            }
            
            // 清除特定群组成员缓存
            if (entity.getGroupId() != null && entity.getUserId() != null) {
                clearGroupMemberCache(entity.getGroupId(), entity.getUserId());
            }
        }
    }
    
    /**
     * 清除群组成员列表缓存
     * 
     * @param groupId 群组ID
     */
    private void clearGroupMembersCache(Long groupId) {
        try {
            String cacheKey = GROUP_MEMBERS_CACHE_PREFIX + groupId;
            redisTemplate.delete(cacheKey);
            log.debug("已清除群组成员列表缓存: groupId={}", groupId);
        } catch (Exception e) {
            log.warn("清除群组成员列表缓存失败: groupId={}, error={}", groupId, e.getMessage());
        }
    }
    
    /**
     * 清除用户群组列表缓存
     * 
     * @param userId 用户ID
     */
    private void clearUserGroupsCache(Long userId) {
        try {
            String cacheKey = USER_GROUPS_CACHE_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("已清除用户群组列表缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户群组列表缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 清除特定群组成员缓存
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     */
    private void clearGroupMemberCache(Long groupId, Long userId) {
        try {
            String cacheKey = GROUP_MEMBER_CACHE_PREFIX + groupId + ":" + userId;
            redisTemplate.delete(cacheKey);
            log.debug("已清除特定群组成员缓存: groupId={}, userId={}", groupId, userId);
        } catch (Exception e) {
            log.warn("清除特定群组成员缓存失败: groupId={}, userId={}, error={}", groupId, userId, e.getMessage());
        }
    }

    /**
     * 删除群组成员信息
     * 
     * @param id 群组成员ID
     * @return 结果
     */
    @Override
    public int deleteImGroupMemberById(Long id) {
        return deleteById(id);
    }
    
    /**
     * 根据群组ID查询群组成员列表
     * 
     * @param groupId 群组ID
     * @return 群组成员集合
     */
    @Override
    public List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId) {
        return imGroupMemberMapper.selectImGroupMemberListByGroupId(groupId);
    }
    
    /**
     * 根据群组ID和用户ID查询群组成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群组成员
     */
    @Override
    public ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId) {
        return imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
    }
    
    /**
     * 添加群组成员
     * 
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @param role 角色
     * @param inviterId 邀请人ID
     * @return 结果
     */
    @Override
    public int addGroupMembers(Long groupId, List<Long> userIds, String role, Long inviterId) {
        long startTime = System.currentTimeMillis();
        String methodName = "addGroupMembers";
        
        try {
            // 参数验证
            ValidationUtils.validateId(groupId, methodName);
            ValidationUtils.validateCollection(userIds, "用户ID列表", methodName);
            
            int result = 0;
            for (Long userId : userIds) {
                // 检查用户是否已经是群成员
                ImGroupMember existingMember = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
                if (existingMember == null) {
                    ImGroupMember member = new ImGroupMember();
                    member.setGroupId(groupId);
                    member.setUserId(userId);
                    member.setRole(role != null ? role : "MEMBER");
                    member.setInviterId(inviterId);
                    
                    result += insert(member);
                }
            }
            
            log.info("添加群组成员成功: groupId={}, userIds={}, result={}", groupId, userIds, result);
            return result;
            
        } catch (Exception e) {
            log.error("添加群组成员异常: groupId={}, userIds={}, error={}", groupId, userIds, e.getMessage(), e);
            throw new BusinessException("添加群组成员失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("添加群组成员耗时: {}ms, groupId={}, userIds={}", duration, groupId, userIds);
        }
    }
    
    /**
     * 移除群组成员
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int removeGroupMember(Long groupId, Long userId, Long operatorId) {
        long startTime = System.currentTimeMillis();
        String methodName = "removeGroupMember";
        
        try {
            // 参数验证
            ValidationUtils.validateId(groupId, methodName);
            ValidationUtils.validateId(userId, methodName);
            
            ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                int result = deleteById(member.getId());
                log.info("移除群组成员成功: groupId={}, userId={}, result={}", groupId, userId, result);
                return result;
            }
            
            log.warn("未找到要移除的群组成员: groupId={}, userId={}", groupId, userId);
            return 0;
            
        } catch (Exception e) {
            log.error("移除群组成员异常: groupId={}, userId={}, error={}", groupId, userId, e.getMessage(), e);
            throw new BusinessException("移除群组成员失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("移除群组成员耗时: {}ms, groupId={}, userId={}", duration, groupId, userId);
        }
    }
    
    /**
     * 更新群组成员角色
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param newRole 新角色
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int updateGroupMemberRole(Long groupId, Long userId, String newRole, Long operatorId) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateGroupMemberRole";
        
        try {
            // 参数验证
            ValidationUtils.validateId(groupId, methodName);
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(newRole, "角色", methodName);
            
            ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                member.setRole(newRole);
                int result = update(member);
                log.info("更新群组成员角色成功: groupId={}, userId={}, newRole={}, result={}", groupId, userId, newRole, result);
                return result;
            }
            
            log.warn("未找到要更新角色的群组成员: groupId={}, userId={}", groupId, userId);
            return 0;
            
        } catch (Exception e) {
            log.error("更新群组成员角色异常: groupId={}, userId={}, newRole={}, error={}", groupId, userId, newRole, e.getMessage(), e);
            throw new BusinessException("更新群组成员角色失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新群组成员角色耗时: {}ms, groupId={}, userId={}", duration, groupId, userId);
        }
    }
    
    /**
     * 设置群组成员禁言时间
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param muteEndTime 禁言结束时间
     * @param operatorId 操作人ID
     * @return 结果
     */
    @Override
    public int setGroupMemberMuteTime(Long groupId, Long userId, java.time.LocalDateTime muteEndTime, Long operatorId) {
        ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member != null) {
            member.setMuteEndTime(muteEndTime);
            return updateImGroupMember(member);
        }
        return 0;
    }
}