package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImGroupMember;
import java.util.List;

/**
 * 缇ょ粍鎴愬憳Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImGroupMemberService extends BaseService<ImGroupMember> {
    
    @Override
    ImGroupMember selectById(Long id);
    
    @Override
    List<ImGroupMember> selectList(ImGroupMember imGroupMember);
    
    @Override
    int insert(ImGroupMember imGroupMember);
    
    @Override
    int update(ImGroupMember imGroupMember);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
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
     * 娣诲姞缇ょ粍鎴愬憳
     * 
     * @param groupId 缇ょ粍ID
     * @param userIds 鐢ㄦ埛ID鍒楄〃
     * @param role 瑙掕壊
     * @param inviterId 閭€璇蜂汉ID
     * @return 缁撴灉
     */
    public int addGroupMembers(Long groupId, List<Long> userIds, String role, Long inviterId);
    
    /**
     * 绉婚櫎缇ょ粍鎴愬憳
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    public int removeGroupMember(Long groupId, Long userId, Long operatorId);
    
    /**
     * 鏇存柊缇ょ粍鎴愬憳瑙掕壊
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @param newRole 鏂拌鑹?     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    public int updateGroupMemberRole(Long groupId, Long userId, String newRole, Long operatorId);
    
    /**
     * 璁剧疆缇ょ粍鎴愬憳绂佽█鏃堕棿
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @param muteEndTime 绂佽█缁撴潫鏃堕棿
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    public int setGroupMemberMuteTime(Long groupId, Long userId, java.time.LocalDateTime muteEndTime, Long operatorId);
    
    /**
     * 鏍规嵁缇ょ粍ID鍒犻櫎缇ょ粍鎴愬憳
     * 
     * @param groupId 缇ょ粍ID
     * @return 缁撴灉
     */
    public int deleteByGroupId(Long groupId);
    
    /**
     * 鏍规嵁缇ょ粍ID鏌ヨ鎵€鏈夌兢缁勬垚鍛?     * 
     * @param groupId 缇ょ粍ID
     * @return 缇ょ粍鎴愬憳鍒楄〃
     */
    public java.util.List<ImGroupMember> selectByGroupId(Long groupId);
    
    /**
     * 鏍规嵁缇ょ粍ID鍜岀敤鎴稩D鏌ヨ缇ょ粍鎴愬憳
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @return 缇ょ粍鎴愬憳
     */
    public ImGroupMember selectByGroupIdAndUserId(Long groupId, Long userId);
    
    /**
     * 鏍规嵁缇ょ粍ID鍜岀敤鎴稩D鍒犻櫎缇ょ粍鎴愬憳
     * 
     * @param groupId 缇ょ粍ID
     * @param userId 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    public int deleteByGroupIdAndUserId(Long groupId, Long userId);
    
    /**
     * 鏍规嵁缇ょ粍ID缁熻鎴愬憳鏁伴噺
     * 
     * @param groupId 缇ょ粍ID
     * @return 鎴愬憳鏁伴噺
     */
    public int countByGroupId(Long groupId);
}
