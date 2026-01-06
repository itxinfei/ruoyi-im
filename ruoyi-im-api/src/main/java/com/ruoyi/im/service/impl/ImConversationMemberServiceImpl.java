package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.service.ImConversationMemberService;

/**
 * 浼氳瘽鎴愬憳Service涓氬姟灞傚鐞? * 
 * @author ruoyi
 */
@Service
public class ImConversationMemberServiceImpl extends BaseServiceImpl<ImConversationMember, ImConversationMemberMapper> implements ImConversationMemberService {
    @Autowired
    private ImConversationMemberMapper imConversationMemberMapper;

    public ImConversationMember selectImConversationMemberById(Long id) {
        return selectById(id);
    }

    public List<ImConversationMember> selectImConversationMemberList(ImConversationMember imConversationMember) {
        return selectList(imConversationMember);
    }

    public int insertImConversationMember(ImConversationMember imConversationMember) {
        return insert(imConversationMember);
    }

    public int updateImConversationMember(ImConversationMember imConversationMember) {
        return update(imConversationMember);
    }

    public int deleteImConversationMemberByIds(Long[] ids) {
        return deleteByIds(ids);
    }

    public int deleteImConversationMemberById(Long id) {
        return deleteById(id);
    }


    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ浼氳瘽鎴愬憳鍒楄〃
     * 
     * @param conversationId 浼氳瘽ID
     * @return 浼氳瘽鎴愬憳闆嗗悎
     */
    @Override
    public List<ImConversationMember> selectImConversationMemberListByConversationId(Long conversationId) {
        return imConversationMemberMapper.selectImConversationMemberListByConversationId(conversationId);
    }
    
    /**
     * 鏍规嵁浼氳瘽ID鍜岀敤鎴稩D鏌ヨ浼氳瘽鎴愬憳
     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @return 浼氳瘽鎴愬憳
     */
    @Override
    public ImConversationMember selectImConversationMemberByConversationIdAndUserId(Long conversationId, Long userId) {
        return imConversationMemberMapper.selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ浼氳瘽鎴愬憳鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 浼氳瘽鎴愬憳闆嗗悎
     */
    @Override
    public List<ImConversationMember> selectImConversationMemberListByUserId(Long userId) {
        return imConversationMemberMapper.selectImConversationMemberListByUserId(userId);
    }
    
    /**
     * 娣诲姞浼氳瘽鎴愬憳
     * 
     * @param conversationId 浼氳瘽ID
     * @param userIds 鐢ㄦ埛ID鍒楄〃
     * @return 缁撴灉
     */
    @Override
    public int addConversationMembers(Long conversationId, List<Long> userIds) {
        int result = 0;
        for (Long userId : userIds) {
            // 妫€鏌ョ敤鎴锋槸鍚﹀凡缁忔槸浼氳瘽鎴愬憳
            ImConversationMember existingMember = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
            if (existingMember == null) {
                ImConversationMember member = new ImConversationMember();
                member.setConversationId(conversationId);
                member.setUserId(userId);
                member.setUnreadCount(0);
                
                result += insertImConversationMember(member);
            }
        }
        return result;
    }
    
    /**
     * 绉婚櫎浼氳瘽鎴愬憳
     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    @Override
    public int removeConversationMember(Long conversationId, Long userId) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            return deleteImConversationMemberById(member.getId());
        }
        return 0;
    }
    
    /**
     * 鏇存柊浼氳瘽鎴愬憳鏈鏁?     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param unreadCount 鏈鏁?     * @return 缁撴灉
     */
    @Override
    public int updateUnreadCount(Long conversationId, Long userId, Integer unreadCount) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setUnreadCount(unreadCount);
            return updateImConversationMember(member);
        }
        return 0;
    }
    
    /**
     * 鏍囪浼氳瘽鎴愬憳娑堟伅宸茶
     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param messageId 娑堟伅ID
     * @return 缁撴灉
     */
    @Override
    public int markMessageAsRead(Long conversationId, Long userId, Long messageId) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setLastReadMessageId(messageId);
            // 灏嗘湭璇绘暟璁剧疆涓?
            member.setUnreadCount(0);
            return updateImConversationMember(member);
        }
        return 0;
    }
    
    /**
     * 璁剧疆浼氳瘽鎴愬憳缃《鐘舵€?     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param isPinned 鏄惁缃《
     * @return 缁撴灉
     */
    @Override
    public int setConversationPinned(Long conversationId, Long userId, Boolean isPinned) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setIsPinned(isPinned);
            return updateImConversationMember(member);
        }
        return 0;
    }
    
    /**
     * 璁剧疆浼氳瘽鎴愬憳鍏嶆墦鎵扮姸鎬?     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param isMuted 鏄惁鍏嶆墦鎵?     * @return 缁撴灉
     */
    @Override
    public int setConversationMuted(Long conversationId, Long userId, Boolean isMuted) {
        ImConversationMember member = selectImConversationMemberByConversationIdAndUserId(conversationId, userId);
        if (member != null) {
            member.setIsMuted(isMuted);
            return updateImConversationMember(member);
        }
        return 0;
    }
}
