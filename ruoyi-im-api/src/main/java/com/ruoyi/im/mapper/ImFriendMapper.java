package com.ruoyi.im.mapper;

import java.util.List;
import com.ruoyi.im.domain.ImFriend;

/**
 * 濂藉弸Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImFriendMapper 
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
     * 鍒犻櫎濂藉弸
     * 
     * @param id 濂藉弸ID
     * @return 缁撴灉
     */
    public int deleteImFriendById(Long id);

    /**
     * 鎵归噺鍒犻櫎濂藉弸
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImFriendByIds(Long[] ids);
    
    /**
     * 鏍规嵁鏉′欢鍒犻櫎濂藉弸
     * 
     * @param imFriend 鏉′欢
     * @return 缁撴灉
     */
    public int deleteImFriendByCondition(ImFriend imFriend);
}
