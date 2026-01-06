package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImConversationMember;
import java.util.List;

/**
 * 浼氳瘽鎴愬憳Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImConversationMemberService extends BaseService<ImConversationMember> {
    
    @Override
    ImConversationMember selectById(Long id);
    
    @Override
    List<ImConversationMember> selectList(ImConversationMember imConversationMember);
    
    @Override
    int insert(ImConversationMember imConversationMember);
    
    @Override
    int update(ImConversationMember imConversationMember);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 鏍规嵁浼氳瘽ID鏌ヨ浼氳瘽鎴愬憳鍒楄〃
     * 
     * @param conversationId 浼氳瘽ID
     * @return 浼氳瘽鎴愬憳闆嗗悎
     */
    public List<ImConversationMember> selectImConversationMemberListByConversationId(Long conversationId);
    
    /**
     * 鏍规嵁浼氳瘽ID鍜岀敤鎴稩D鏌ヨ浼氳瘽鎴愬憳
     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @return 浼氳瘽鎴愬憳
     */
    public ImConversationMember selectImConversationMemberByConversationIdAndUserId(Long conversationId, Long userId);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ浼氳瘽鎴愬憳鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 浼氳瘽鎴愬憳闆嗗悎
     */
    public List<ImConversationMember> selectImConversationMemberListByUserId(Long userId);
    
    /**
     * 娣诲姞浼氳瘽鎴愬憳
     * 
     * @param conversationId 浼氳瘽ID
     * @param userIds 鐢ㄦ埛ID鍒楄〃
     * @return 缁撴灉
     */
    public int addConversationMembers(Long conversationId, List<Long> userIds);
    
    /**
     * 绉婚櫎浼氳瘽鎴愬憳
     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    public int removeConversationMember(Long conversationId, Long userId);
    
    /**
     * 鏇存柊浼氳瘽鎴愬憳鏈鏁?     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param unreadCount 鏈鏁?     * @return 缁撴灉
     */
    public int updateUnreadCount(Long conversationId, Long userId, Integer unreadCount);
    
    /**
     * 鏍囪浼氳瘽鎴愬憳娑堟伅宸茶
     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param messageId 娑堟伅ID
     * @return 缁撴灉
     */
    public int markMessageAsRead(Long conversationId, Long userId, Long messageId);
    
    /**
     * 璁剧疆浼氳瘽鎴愬憳缃《鐘舵€?     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param isPinned 鏄惁缃《
     * @return 缁撴灉
     */
    public int setConversationPinned(Long conversationId, Long userId, Boolean isPinned);
    
    /**
     * 璁剧疆浼氳瘽鎴愬憳鍏嶆墦鎵扮姸鎬?     * 
     * @param conversationId 浼氳瘽ID
     * @param userId 鐢ㄦ埛ID
     * @param isMuted 鏄惁鍏嶆墦鎵?     * @return 缁撴灉
     */
    public int setConversationMuted(Long conversationId, Long userId, Boolean isMuted);
}
