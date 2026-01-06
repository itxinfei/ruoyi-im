package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImFriend;

import java.util.List;

/**
 * 濂藉弸Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImFriendService 
{
    /**
     * 鏌ヨ濂藉弸
     * 
     * @param id 濂藉弸ID
     * @return 濂藉弸
     */
    public ImFriend selectImFriendById(Long id);

    /**
     * 鏌ヨ濂藉弸鍒楄〃
     * 
     * @param imFriend 濂藉弸
     * @return 濂藉弸闆嗗悎
     */
    public List<ImFriend> selectImFriendList(ImFriend imFriend);

    /**
     * 鏂板濂藉弸
     * 
     * @param imFriend 濂藉弸
     * @return 缁撴灉
     */
    public int insertImFriend(ImFriend imFriend);

    /**
     * 淇敼濂藉弸
     * 
     * @param imFriend 濂藉弸
     * @return 缁撴灉
     */
    public int updateImFriend(ImFriend imFriend);

    /**
     * 鎵归噺鍒犻櫎濂藉弸
     * 
     * @param ids 闇€瑕佸垹闄ょ殑濂藉弸ID
     * @return 缁撴灉
     */
    public int deleteImFriendByIds(Long[] ids);

    /**
     * 鍒犻櫎濂藉弸淇℃伅
     * 
     * @param id 濂藉弸ID
     * @return 缁撴灉
     */
    public int deleteImFriendById(Long id);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鍜屽ソ鍙婭D鏌ヨ濂藉弸鍏崇郴
     * 
     * @param userId 鐢ㄦ埛ID
     * @param friendId 濂藉弸ID
     * @return 濂藉弸鍏崇郴
     */
    default ImFriend selectImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        return selectImFriendByUserIdAndFriendId(userId, friendId);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ濂藉弸鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 濂藉弸鍒楄〃
     */
    default List<ImFriend> selectImFriendListByUserId(Long userId) {
        return selectImFriendListByUserId(userId);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鍜屽ソ鍙婭D鍒犻櫎濂藉弸鍏崇郴
     * 
     * @param userId 鐢ㄦ埛ID
     * @param friendId 濂藉弸ID
     * @return 鍒犻櫎缁撴灉
     */
    default int deleteImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        return deleteImFriendByUserIdAndFriendId(userId, friendId);
    }
}
