package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.mapper.base.BaseMapper;
import java.util.List;

/**
 * 浼氳瘽鎴愬憳Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImConversationMemberMapper extends BaseMapper<ImConversationMember> {
    /**
     * 鏌ヨ浼氳瘽鎴愬憳
     * 
     * @param id 浼氳瘽鎴愬憳ID
     * @return 浼氳瘽鎴愬憳
     */
    public ImConversationMember selectImConversationMemberById(Long id);

    /**
     * 鏌ヨ浼氳瘽鎴愬憳鍒楄〃
     * 
     * @param imConversationMember 浼氳瘽鎴愬憳
     * @return 浼氳瘽鎴愬憳闆嗗悎
     */
    public List<ImConversationMember> selectImConversationMemberList(ImConversationMember imConversationMember);

    /**
     * 鏂板浼氳瘽鎴愬憳
     * 
     * @param imConversationMember 浼氳瘽鎴愬憳
     * @return 缁撴灉
     */
    public int insertImConversationMember(ImConversationMember imConversationMember);

    /**
     * 淇敼浼氳瘽鎴愬憳
     * 
     * @param imConversationMember 浼氳瘽鎴愬憳
     * @return 缁撴灉
     */
    public int updateImConversationMember(ImConversationMember imConversationMember);

    /**
     * 鍒犻櫎浼氳瘽鎴愬憳
     * 
     * @param id 浼氳瘽鎴愬憳ID
     * @return 缁撴灉
     */
    public int deleteImConversationMemberById(Long id);

    /**
     * 鎵归噺鍒犻櫎浼氳瘽鎴愬憳
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImConversationMemberByIds(Long[] ids);
    
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
     * 鎵归噺鍒犻櫎浼氳瘽鎴愬憳锛堟牴鎹細璇滻D锛?     * 
     * @param conversationIds 浼氳瘽ID鏁扮粍
     * @return 缁撴灉
     */
    public int deleteImConversationMemberByConversationIds(Long[] conversationIds);
}
