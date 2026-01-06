package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImFriendRequest;
import java.util.List;

/**
 * 濂藉弸鐢宠Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImFriendRequestService extends BaseService<ImFriendRequest> {
    
    @Override
    ImFriendRequest selectById(Long id);
    
    @Override
    List<ImFriendRequest> selectList(ImFriendRequest imFriendRequest);
    
    @Override
    int insert(ImFriendRequest imFriendRequest);
    
    @Override
    int update(ImFriendRequest imFriendRequest);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 鏍规嵁鐢宠浜篒D鍜岃鐢宠浜篒D鏌ヨ濂藉弸鐢宠
     * 
     * @param fromUserId 鐢宠浜篒D
     * @param toUserId 琚敵璇蜂汉ID
     * @return 濂藉弸鐢宠
     */
    public ImFriendRequest selectImFriendRequestByFromAndToUserId(Long fromUserId, Long toUserId);
    
    /**
     * 鏍规嵁鐢宠浜篒D鏌ヨ濂藉弸鐢宠鍒楄〃
     * 
     * @param fromUserId 鐢宠浜篒D
     * @return 濂藉弸鐢宠闆嗗悎
     */
    public List<ImFriendRequest> selectImFriendRequestListByFromUserId(Long fromUserId);
    
    /**
     * 鏍规嵁琚敵璇蜂汉ID鏌ヨ濂藉弸鐢宠鍒楄〃
     * 
     * @param toUserId 琚敵璇蜂汉ID
     * @return 濂藉弸鐢宠闆嗗悎
     */
    public List<ImFriendRequest> selectImFriendRequestListByToUserId(Long toUserId);
    
    /**
     * 鍙戦€佸ソ鍙嬬敵璇?     * 
     * @param fromUserId 鐢宠浜篒D
     * @param toUserId 琚敵璇蜂汉ID
     * @param message 鐢宠娑堟伅
     * @return 缁撴灉
     */
    public int sendFriendRequest(Long fromUserId, Long toUserId, String message);
    
    /**
     * 澶勭悊濂藉弸鐢宠
     * 
     * @param id 鐢宠ID
     * @param operatorId 鎿嶄綔浜篒D
     * @param status 鐘舵€侊紙APPROVED宸插悓鎰?REJECTED宸叉嫆缁濓級
     * @return 缁撴灉
     */
    public int handleFriendRequest(Long id, Long operatorId, String status);
    
    /**
     * 鍚屾剰濂藉弸鐢宠
     * 
     * @param id 鐢宠ID
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    public int approveFriendRequest(Long id, Long operatorId);
    
    /**
     * 鎷掔粷濂藉弸鐢宠
     * 
     * @param id 鐢宠ID
     * @param operatorId 鎿嶄綔浜篒D
     * @return 缁撴灉
     */
    public int rejectFriendRequest(Long id, Long operatorId);
}
