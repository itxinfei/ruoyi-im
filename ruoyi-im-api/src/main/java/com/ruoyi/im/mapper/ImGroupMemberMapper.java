package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImGroupMember;
import java.util.List;

/**
 * 缇ょ粍鎴愬憳Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImGroupMemberMapper {
    /**
     * 鏌ヨ缇ょ粍鎴愬憳
     * 
     * @param id 缇ょ粍鎴愬憳ID
     * @return 缇ょ粍鎴愬憳
     */
    public ImGroupMember selectImGroupMemberById(Long id);

    /**
     * 鏌ヨ缇ょ粍鎴愬憳鍒楄〃
     * 
     * @param imGroupMember 缇ょ粍鎴愬憳
     * @return 缇ょ粍鎴愬憳闆嗗悎
     */
    public List<ImGroupMember> selectImGroupMemberList(ImGroupMember imGroupMember);

    /**
     * 鏂板缇ょ粍鎴愬憳
     * 
     * @param imGroupMember 缇ょ粍鎴愬憳
     * @return 缁撴灉
     */
    public int insertImGroupMember(ImGroupMember imGroupMember);

    /**
     * 淇敼缇ょ粍鎴愬憳
     * 
     * @param imGroupMember 缇ょ粍鎴愬憳
     * @return 缁撴灉
     */
    public int updateImGroupMember(ImGroupMember imGroupMember);

    /**
     * 鍒犻櫎缇ょ粍鎴愬憳
     * 
     * @param id 缇ょ粍鎴愬憳ID
     * @return 缁撴灉
     */
    public int deleteImGroupMemberById(Long id);

    /**
     * 鎵归噺鍒犻櫎缇ょ粍鎴愬憳
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImGroupMemberByIds(Long[] ids);
    
    /**
     * 鏍规嵁缇ょ粍ID鏌ヨ缇ょ粍鎴愬憳鍒楄〃
     * 
     * @param groupId 缇ょ粍ID
     * @return 缇ょ粍鎴愬憳闆嗗悎
     */
    public List<ImGroupMember> selectImGroupMemberListByGroupId(Long groupId);
    
    /**
     * 鏍规嵁缇ょ粍ID鍜岀敤鎴稩D鏌ヨ缇ょ粍鎴愬憳
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @return 缇ょ粍鎴愬憳
     */
    public ImGroupMember selectImGroupMemberByGroupIdAndUserId(Long groupId, Long userId);
    
    /**
     * 鎵归噺鍒犻櫎缇ょ粍鎴愬憳锛堟牴鎹兢缁処D锛?     * 
     * @param groupIds 缇ょ粍ID鏁扮粍
     * @return 缁撴灉
     */
    public int deleteImGroupMemberByGroupIds(Long[] groupIds);
}
