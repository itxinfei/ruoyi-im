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
 * 缂囥倗绮嶉幋鎰喅Service娑撴艾濮熺仦鍌氼槱閻?- 娴兼ê瀵查悧鍫熸拱
 * 娴兼ê瀵查崘鍛啇閿涙碍鍧婇崝鐘电处鐎涙ɑ婧€閸掕翰鈧椒绨ㄩ崝鈩冨付閸掕翰鈧焦鈧嗗厴閻╂垶甯堕妴渚€鏁婄拠顖氼槱閻? * 
 * @author ruoyi
 */
@Service
public class ImGroupMemberServiceImpl extends EnhancedBaseServiceImpl<ImGroupMember, ImGroupMemberMapper> implements ImGroupMemberService {
    private static final Logger log = LoggerFactory.getLogger(ImGroupMemberServiceImpl.class);
    
    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缂傛挸鐡ㄩ柨顔煎缂傗偓
    private static final String GROUP_MEMBERS_CACHE_PREFIX = "im:group:members:";
    private static final String USER_GROUPS_CACHE_PREFIX = "im:user:groups:";
    private static final String GROUP_MEMBER_CACHE_PREFIX = "im:group:member:";
    
    // 缂傛挸鐡ㄧ搾鍛閺冨爼妫块敍鍫濆瀻闁界噦绱?
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    // 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?    @Override
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
     * 鐎圭偟骞嘐nhancedBaseServiceImpl娑擃厾娈慶learRelatedCache閺傝纭堕敍灞惧絹娓氭稓鍏㈢紒鍕灇閸涙澹掔€规氨绱︾€涙ɑ绔婚悶鍡涒偓鏄忕帆
     * 
     * @param entity 缂囥倗绮嶉幋鎰喅鐎圭偘缍?
     */
    @Override
    protected void clearRelatedCache(ImGroupMember entity) {
        if (entity != null) {
            // 濞撳懘娅庣€圭偘缍嬬紓鎾崇摠
            clearEntityCache(entity.getId());
            
            // 濞撳懘娅庣紘銈囩矋閹存劕鎲抽崚妤勩€冪紓鎾崇摠
            if (entity.getGroupId() != null) {
                clearGroupMembersCache(entity.getGroupId());
            }
            
            // 濞撳懘娅庨悽銊﹀煕缂囥倗绮嶉崚妤勩€冪紓鎾崇摠
            if (entity.getUserId() != null) {
                clearUserGroupsCache(entity.getUserId());
            }
            
            // 濞撳懘娅庨悧鐟扮暰缂囥倗绮嶉幋鎰喅缂傛挸鐡?
            if (entity.getGroupId() != null && entity.getUserId() != null) {
                clearGroupMemberCache(entity.getGroupId(), entity.getUserId());
            }
        }
    }
    
    /**
     * 濞撳懘娅庣紘銈囩矋閹存劕鎲抽崚妤勩€冪紓鎾崇摠
     * 
     * @param groupId 缂囥倗绮岻D
     */
    private void clearGroupMembersCache(Long groupId) {
        try {
            String cacheKey = GROUP_MEMBERS_CACHE_PREFIX + groupId;
            redisTemplate.delete(cacheKey);
            log.debug("瀹稿弶绔婚梽銈囧參缂佸嫭鍨氶崨妯哄灙鐞涖劎绱︾€? groupId={}", groupId);
        } catch (Exception e) {
            log.warn("濞撳懘娅庣紘銈囩矋閹存劕鎲抽崚妤勩€冪紓鎾崇摠婢惰精瑙? groupId={}, error={}", groupId, e.getMessage());
        }
    }
    
    /**
     * 濞撳懘娅庨悽銊﹀煕缂囥倗绮嶉崚妤勩€冪紓鎾崇摠
     * 
     * @param userId 閻劍鍩汭D
     */
    private void clearUserGroupsCache(Long userId) {
        try {
            String cacheKey = USER_GROUPS_CACHE_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("瀹稿弶绔婚梽銈囨暏閹撮鍏㈢紒鍕灙鐞涖劎绱︾€? userId={}", userId);
        } catch (Exception e) {
            log.warn("濞撳懘娅庨悽銊﹀煕缂囥倗绮嶉崚妤勩€冪紓鎾崇摠婢惰精瑙? userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 濞撳懘娅庨悧鐟扮暰缂囥倗绮嶉幋鎰喅缂傛挸鐡?
     * 
     * @param groupId 缂囥倗绮岻D
     * @param userId 閻劍鍩汭D
     */
    private void clearGroupMemberCache(Long groupId, Long userId) {
        try {
            String cacheKey = GROUP_MEMBER_CACHE_PREFIX + groupId + ":" + userId;
            redisTemplate.delete(cacheKey);
            log.debug("瀹稿弶绔婚梽銈囧鐎规氨鍏㈢紒鍕灇閸涙绱︾€? groupId={}, userId={}", groupId, userId);
        } catch (Exception e) {
            log.warn("濞撳懘娅庨悧鐟扮暰缂囥倗绮嶉幋鎰喅缂傛挸鐡ㄦ径杈Е: groupId={}, userId={}, error={}", groupId, userId, e.getMessage());
        }
    }

    /**
     * 閸掔娀娅庣紘銈囩矋閹存劕鎲虫穱鈩冧紖
     * 
     * @param id 缂囥倗绮嶉幋鎰喅ID
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImGroupMemberById(Long id) {
        return deleteById(id);
    }
    
    /**
     * 閺嶈宓佺紘銈囩矋ID閺屻儴顕楃紘銈囩矋閹存劕鎲抽崚妤勩€?
     * 
     * @param groupId 缂囥倗绮岻D
     * @return 缂囥倗绮嶉幋鎰喅闂嗗棗鎮?
     */
    @Override
    public List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId) {
        return imGroupMemberMapper.selectImGroupMemberListByGroupId(groupId);
    }
    
    /**
     * 閺嶈宓佺紘銈囩矋ID閸滃瞼鏁ら幋绋〥閺屻儴顕楃紘銈囩矋閹存劕鎲?
     * 
     * @param groupId 缂囥倗绮岻D
     * @param userId 閻劍鍩汭D
     * @return 缂囥倗绮嶉幋鎰喅
     */
    @Override
    public ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId) {
        return imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
    }
    
    /**
     * 濞ｈ濮炵紘銈囩矋閹存劕鎲?
     * 
     * @param groupId 缂囥倗绮岻D
     * @param userIds 閻劍鍩汭D閸掓銆?
     * @param role 鐟欐帟澹?
     * @param inviterId 闁偓鐠囪渹姹塈D
     * @return 缂佹挻鐏?
     */
    @Override
    public int addGroupMembers(Long groupId, List<Long> userIds, String role, Long inviterId) {
        long startTime = System.currentTimeMillis();
        String methodName = "addGroupMembers";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateId(groupId, methodName);
            ValidationUtils.validateCollection(userIds, "閻劍鍩汭D閸掓銆?, methodName);
            
            int result = 0;
            for (Long userId : userIds) {
                // 濡閺屻儳鏁ら幋閿嬫Ц閸氾箑鍑＄紒蹇旀Ц缂囥倖鍨氶崨?
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
            
            log.info("濞ｈ濮炵紘銈囩矋閹存劕鎲抽幋鎰: groupId={}, userIds={}, result={}", groupId, userIds, result);
            return result;
            
        } catch (Exception e) {
            log.error("濞ｈ濮炵紘銈囩矋閹存劕鎲冲鍌氱埗: groupId={}, userIds={}, error={}", groupId, userIds, e.getMessage(), e);
            throw new BusinessException("濞ｈ濮炵紘銈囩矋閹存劕鎲虫径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("濞ｈ濮炵紘銈囩矋閹存劕鎲抽懓妤佹: {}ms, groupId={}, userIds={}", duration, groupId, userIds);
        }
    }
    
    /**
     * 缁夊娅庣紘銈囩矋閹存劕鎲?
     * 
     * @param groupId 缂囥倗绮岻D
     * @param userId 閻劍鍩汭D
     * @param operatorId 閹垮秳缍旀禍绡扗
     * @return 缂佹挻鐏?
     */
    @Override
    public int removeGroupMember(Long groupId, Long userId, Long operatorId) {
        long startTime = System.currentTimeMillis();
        String methodName = "removeGroupMember";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateId(groupId, methodName);
            ValidationUtils.validateId(userId, methodName);
            
            ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                int result = deleteById(member.getId());
                log.info("缁夊娅庣紘銈囩矋閹存劕鎲抽幋鎰: groupId={}, userId={}, result={}", groupId, userId, result);
                return result;
            }
            
            log.warn("閺堫亝澹橀崚鎷岊洣缁夊娅庨惃鍕參缂佸嫭鍨氶崨? groupId={}, userId={}", groupId, userId);
            return 0;
            
        } catch (Exception e) {
            log.error("缁夊娅庣紘銈囩矋閹存劕鎲冲鍌氱埗: groupId={}, userId={}, error={}", groupId, userId, e.getMessage(), e);
            throw new BusinessException("缁夊娅庣紘銈囩矋閹存劕鎲虫径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("缁夊娅庣紘銈囩矋閹存劕鎲抽懓妤佹: {}ms, groupId={}, userId={}", duration, groupId, userId);
        }
    }
    
    /**
     * 閺囧瓨鏌婄紘銈囩矋閹存劕鎲崇憴鎺曞
     * 
     * @param groupId 缂囥倗绮岻D
     * @param userId 閻劍鍩汭D
     * @param newRole 閺傛媽顫楅懝?     * @param operatorId 閹垮秳缍旀禍绡扗
     * @return 缂佹挻鐏?
     */
    @Override
    public int updateGroupMemberRole(Long groupId, Long userId, String newRole, Long operatorId) {
        long startTime = System.currentTimeMillis();
        String methodName = "updateGroupMemberRole";
        
        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateId(groupId, methodName);
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(newRole, "鐟欐帟澹?", methodName);
            
            ImGroupMember member = selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                member.setRole(newRole);
                int result = update(member);
                log.info("閺囧瓨鏌婄紘銈囩矋閹存劕鎲崇憴鎺曞閹存劕濮? groupId={}, userId={}, newRole={}, result={}", groupId, userId, newRole, result);
                return result;
            }
            
            log.warn("閺堫亝澹橀崚鎷岊洣閺囧瓨鏌婄憴鎺曞閻ㄥ嫮鍏㈢紒鍕灇閸? groupId={}, userId={}", groupId, userId);
            return 0;
            
        } catch (Exception e) {
            log.error("閺囧瓨鏌婄紘銈囩矋閹存劕鎲崇憴鎺曞瀵倸鐖? groupId={}, userId={}, newRole={}, error={}", groupId, userId, newRole, e.getMessage(), e);
            throw new BusinessException("閺囧瓨鏌婄紘銈囩矋閹存劕鎲崇憴鎺曞婢惰精瑙?", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺囧瓨鏌婄紘銈囩矋閹存劕鎲崇憴鎺曞閼版妞? {}ms, groupId={}, userId={}", duration, groupId, userId);
        }
    }
    
    /**
     * 鐠佸墽鐤嗙紘銈囩矋閹存劕鎲崇粋浣解枅閺冨爼妫?
     * 
     * @param groupId 缂囥倗绮岻D
     * @param userId 閻劍鍩汭D
     * @param muteEndTime 缁備浇鈻堢紒鎾存将閺冨爼妫?
     * @param operatorId 閹垮秳缍旀禍绡扗
     * @return 缂佹挻鐏?
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
