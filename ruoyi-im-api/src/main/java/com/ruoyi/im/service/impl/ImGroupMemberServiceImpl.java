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
 * 缇ょ粍鎴愬憳Service涓氬姟灞傚鐞?- 浼樺寲鐗堟湰
 * 浼樺寲鍐呭锛氭坊鍔犵紦瀛樻満鍒躲€佷簨鍔℃帶鍒躲€佹€ц兘鐩戞帶銆侀敊璇鐞? * 
 * @author ruoyi
 */
@Service
public class ImGroupMemberServiceImpl extends EnhancedBaseServiceImpl<ImGroupMember, ImGroupMemberMapper> implements ImGroupMemberService {
    private static final Logger log = LoggerFactory.getLogger(ImGroupMemberServiceImpl.class);
    
    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缂撳瓨閿墠缂€
    private static final String GROUP_MEMBERS_CACHE_PREFIX = "im:group:members:";
    private static final String USER_GROUPS_CACHE_PREFIX = "im:user:groups:";
    private static final String GROUP_MEMBER_CACHE_PREFIX = "im:group:member:";
    
    // 缂撳瓨瓒呮椂鏃堕棿锛堝垎閽燂級
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    // 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?    @Override
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
     * 瀹炵幇EnhancedBaseServiceImpl涓殑clearRelatedCache鏂规硶锛屾彁渚涚兢缁勬垚鍛樼壒瀹氱紦瀛樻竻鐞嗛€昏緫
     * 
     * @param entity 缇ょ粍鎴愬憳瀹炰綋
     */
    @Override
    protected void clearRelatedCache(ImGroupMember entity) {
        if (entity != null) {
            // 娓呴櫎瀹炰綋缂撳瓨
            clearEntityCache(entity.getId());
            
            // 娓呴櫎缇ょ粍鎴愬憳鍒楄〃缂撳瓨
            if (entity.getGroupId() != null) {
                clearGroupMembersCache(entity.getGroupId());
            }
            
            // 娓呴櫎鐢ㄦ埛缇ょ粍鍒楄〃缂撳瓨
            if (entity.getUserId() != null) {
                clearUserGroupsCache(entity.getUserId());
            }
            
            // 娓呴櫎鐗瑰畾缇ょ粍鎴愬憳缂撳瓨
            if (entity.getGroupId() != null && entity.getUserId() != null) {
                clearGroupMemberCache(entity.getGroupId(), entity.getUserId());
            }
        }
    }
    
    /**
     * 娓呴櫎缇ょ粍鎴愬憳鍒楄〃缂撳瓨
     * 
     * @param groupId 缇ょ粍ID
     */
    private void clearGroupMembersCache(Long groupId) {
        try {
            String cacheKey = GROUP_MEMBERS_CACHE_PREFIX + groupId;
            redisTemplate.delete(cacheKey);
            log.debug("宸叉竻闄ょ兢缁勬垚鍛樺垪琛ㄧ紦瀛? groupId={}", groupId);
        } catch (Exception e) {
            log.warn("娓呴櫎缇ょ粍鎴愬憳鍒楄〃缂撳瓨澶辫触: groupId={}, error={}", groupId, e.getMessage());
        }
    }
    
    /**
     * 娓呴櫎鐢ㄦ埛缇ょ粍鍒楄〃缂撳瓨
     * 
     * @param userId 鐢ㄦ埛ID
     */
    private void clearUserGroupsCache(Long userId) {
        try {
            String cacheKey = USER_GROUPS_CACHE_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("宸叉竻闄ょ敤鎴风兢缁勫垪琛ㄧ紦瀛? userId={}", userId);
        } catch (Exception e) {
            log.warn("娓呴櫎鐢ㄦ埛缇ょ粍鍒楄〃缂撳瓨澶辫触: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 娓呴櫎鐗瑰畾缇ょ粍鎴愬憳缂撳瓨
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     */
    private void clearGroupMemberCache(Long groupId, Long userId) {
        try {
            String cacheKey = GROUP_MEMBER_CACHE_PREFIX + groupId + ":" + userId;
            redisTemplate.delete(cacheKey);
            log.debug("宸叉竻闄ょ壒瀹氱兢缁勬垚鍛樼紦瀛? groupId={}, userId={}", groupId, userId);
        } catch (Exception e) {
            log.warn("娓呴櫎鐗瑰畾缇ょ粍鎴愬憳缂撳瓨澶辫触: groupId={}, userId={}, error={}", groupId, userId, e.getMessage());
        }
    }

    /**
     * 鍒犻櫎缇ょ粍鎴愬憳淇℃伅
     * 
     * @param id 缇ょ粍鎴愬憳ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImGroupMemberById(Long id) {
        return deleteById(id);
    }
    
    /**
     * 鏍规嵁缇ょ粍ID鏌ヨ缇ょ粍鎴愬憳鍒楄〃
     * 
     * @param groupId 缇ょ粍ID
     * @return 缇ょ粍鎴愬憳闆嗗悎
     */
    @Override
    public List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId) {
        return imGroupMemberMapper.selectImGroupMemberListByGroupId(groupId);
    }
    
    /**
     * 鏍规嵁缇ょ粍ID鍜岀敤鎴稩D鏌ヨ缇ょ粍鎴愬憳
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @return 缇ょ粍鎴愬憳
     */
    @Override
    public ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId) {
        return imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
    }
    
    /**
     * 娣诲姞缇ょ粍鎴愬憳
     * 
     * @param groupId 缇ょ粍ID
     * @param userIds 鐢ㄦ埛ID鍒楄〃
     * @param role 瑙掕壊
     * @param inviterId 閭€璇蜂汉ID
     * @return 缁撴灉
     */
    @Override
    public int addGroupMembers(Long groupId, List<Long> userIds, String role, Long inviterId) {
        long startTime = System.currentTimeMillis();
        String methodName = "addGroupMembers";
        
        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateId(groupId, methodName);
            ValidationUtils.validateCollection(userIds, "鐢ㄦ埛ID鍒楄〃", methodName);
            
            int result = 0;
            for (Long userId : userIds) {
                // 妫€鏌ョ敤鎴锋槸鍚﹀凡缁忔槸缇ゆ垚鍛?                ImGroupMember existingMember = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
                if (existingMember == null) {
                    ImGroupMember member = new ImGroupMember();
                    member.setGroupId(groupId);
                    member.setUserId(userId);
                    member.setRole(role != null ? role : "MEMBER");
                    member.setInviterId(inviterId);
                    
                    result += insert(member);
                }
            }
            
            log.info("娣诲姞缇ょ粍鎴愬憳鎴愬姛: groupId={}, userIds={}, result={}", groupId, userIds, result);
            return result;
            
        } catch (Exception e) {
            log.error("娣诲姞缇ょ粍鎴愬憳寮傚父: groupId={}, userIds={}, error={}", groupId, userIds, e.getMessage(), e);
            throw new BusinessException("娣诲姞缇ょ粍鎴愬憳澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("娣诲姞缇ょ粍鎴愬憳鑰楁椂: {}ms, groupId={}, userIds={}", duration, groupId, userIds);
        }
    }
    
    /**
     * 绉婚櫎缇ょ粍鎴愬憳
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    @Override
    public int removeGroupMember(Long groupId, Long userId, Long operatorId) {
        long startTime = System.currentTimeMillis();
        String methodName = "removeGroupMember";
        
        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateId(groupId, methodName);
            ValidationUtils.validateId(userId, methodName);
            
            ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                int result = deleteById(member.getId());
                log.info("绉婚櫎缇ょ粍鎴愬憳鎴愬姛: groupId={}, userId={}, result={}", groupId, userId, result);
                return result;
            }
            
            log.warn("鏈壘鍒拌绉婚櫎鐨勭兢缁勬垚鍛? groupId={}, userId={}", groupId, userId);
            return 0;
            
        } catch (Exception e) {
            log.error("绉婚櫎缇ょ粍鎴愬憳寮傚父: groupId={}, userId={}, error={}", groupId, userId, e.getMessage(), e);
            throw new BusinessException("绉婚櫎缇ょ粍鎴愬憳澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("绉婚櫎缇ょ粍鎴愬憳鑰楁椂: {}ms, groupId={}, userId={}", duration, groupId, userId);
        }
    }
    
    /**
     * 鏇存柊缇ょ粍鎴愬憳瑙掕壊
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @param newRole 鏂拌鑹?     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    @Override
    public int updateGroupMemberRole(Long groupId, Long userId, String newRole, Long operatorId) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateGroupMemberRole";
        
        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateId(groupId, methodName);
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(newRole, "瑙掕壊", methodName);
            
            ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                member.setRole(newRole);
                int result = update(member);
                log.info("鏇存柊缇ょ粍鎴愬憳瑙掕壊鎴愬姛: groupId={}, userId={}, newRole={}, result={}", groupId, userId, newRole, result);
                return result;
            }
            
            log.warn("鏈壘鍒拌鏇存柊瑙掕壊鐨勭兢缁勬垚鍛? groupId={}, userId={}", groupId, userId);
            return 0;
            
        } catch (Exception e) {
            log.error("鏇存柊缇ょ粍鎴愬憳瑙掕壊寮傚父: groupId={}, userId={}, newRole={}, error={}", groupId, userId, newRole, e.getMessage(), e);
            throw new BusinessException("鏇存柊缇ょ粍鎴愬憳瑙掕壊澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏇存柊缇ょ粍鎴愬憳瑙掕壊鑰楁椂: {}ms, groupId={}, userId={}", duration, groupId, userId);
        }
    }
    
    /**
     * 璁剧疆缇ょ粍鎴愬憳绂佽█鏃堕棿
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @param muteEndTime 绂佽█缁撴潫鏃堕棿
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
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
