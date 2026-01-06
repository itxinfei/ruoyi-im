package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.mapper.base.BaseMapper;
import java.util.List;

/**
 * 濂藉弸鐢宠Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImFriendRequestMapper extends BaseMapper<ImFriendRequest> {
    /**
     * 鏌ヨ濂藉弸鐢宠
     * 
     * @param id 濂藉弸鐢宠ID
     * @return 濂藉弸鐢宠
     */
    public ImFriendRequest selectImFriendRequestById(Long id);

    /**
     * 鏌ヨ濂藉弸鐢宠鍒楄〃
     * 
     * @param imFriendRequest 濂藉弸鐢宠
     * @return 濂藉弸鐢宠闆嗗悎
     */
    public List<ImFriendRequest> selectImFriendRequestList(ImFriendRequest imFriendRequest);

    /**
     * 鏂板濂藉弸鐢宠
     * 
     * @param imFriendRequest 濂藉弸鐢宠
     * @return 缁撴灉
     */
    public int insertImFriendRequest(ImFriendRequest imFriendRequest);

    /**
     * 淇敼濂藉弸鐢宠
     * 
     * @param imFriendRequest 濂藉弸鐢宠
     * @return 缁撴灉
     */
    public int updateImFriendRequest(ImFriendRequest imFriendRequest);

    /**
     * 鍒犻櫎濂藉弸鐢宠
     * 
     * @param id 濂藉弸鐢宠ID
     * @return 缁撴灉
     */
    public int deleteImFriendRequestById(Long id);

    /**
     * 鎵归噺鍒犻櫎濂藉弸鐢宠
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImFriendRequestByIds(Long[] ids);
    
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
}
