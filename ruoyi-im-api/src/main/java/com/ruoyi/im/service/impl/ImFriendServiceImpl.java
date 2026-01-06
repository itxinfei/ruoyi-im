package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.service.ImFriendService;

/**
 * 濂藉弸Service涓氬姟灞傚鐞? * 
 * @author ruoyi
 */
@Service
public class ImFriendServiceImpl implements ImFriendService
{
    @Autowired
    private ImFriendMapper imFriendMapper;

    /**
     * 鏌ヨ濂藉弸
     * 
     * @param id 濂藉弸ID
     * @return 濂藉弸
     */
    @Override
    public ImFriend selectImFriendById(Long id)
    {
        return imFriendMapper.selectImFriendById(id);
    }

    /**
     * 鏌ヨ濂藉弸鍒楄〃
     * 
     * @param imFriend 濂藉弸
     * @return 濂藉弸
     */
    @Override
    public List<ImFriend> selectImFriendList(ImFriend imFriend)
    {
        return imFriendMapper.selectImFriendList(imFriend);
    }

    /**
     * 鏂板濂藉弸
     * 
     * @param imFriend 濂藉弸
     * @return 缁撴灉
     */
    @Override
    public int insertImFriend(ImFriend imFriend)
    {
        return imFriendMapper.insertImFriend(imFriend);
    }

    /**
     * 淇敼濂藉弸
     * 
     * @param imFriend 濂藉弸
     * @return 缁撴灉
     */
    @Override
    public int updateImFriend(ImFriend imFriend)
    {
        return imFriendMapper.updateImFriend(imFriend);
    }

    /**
     * 鎵归噺鍒犻櫎濂藉弸
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImFriendByIds(Long[] ids)
    {
        return imFriendMapper.deleteImFriendByIds(ids);
    }

    /**
     * 鍒犻櫎濂藉弸淇℃伅
     * 
     * @param id 濂藉弸ID
     * @return 缁撴灉
     */
    @Override
    public int deleteImFriendById(Long id)
    {
        return imFriendMapper.deleteImFriendById(id);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鍜屽ソ鍙婭D鏌ヨ濂藉弸鍏崇郴
     * 
     * @param userId 鐢ㄦ埛ID
     * @param friendId 濂藉弸ID
     * @return 濂藉弸鍏崇郴
     */
    @Override
    public ImFriend selectImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        // 鍒涘缓鏌ヨ鏉′欢
        ImFriend condition = new ImFriend();
        condition.setUserId(userId);
        condition.setFriendId(friendId);
        List<ImFriend> friends = imFriendMapper.selectImFriendList(condition);
        return friends != null && !friends.isEmpty() ? friends.get(0) : null;
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ濂藉弸鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 濂藉弸鍒楄〃
     */
    @Override
    public List<ImFriend> selectImFriendListByUserId(Long userId) {
        // 鍒涘缓鏌ヨ鏉′欢
        ImFriend condition = new ImFriend();
        condition.setUserId(userId);
        return imFriendMapper.selectImFriendList(condition);
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鍜屽ソ鍙婭D鍒犻櫎濂藉弸鍏崇郴
     * 
     * @param userId 鐢ㄦ埛ID
     * @param friendId 濂藉弸ID
     * @return 鍒犻櫎缁撴灉
     */
    @Override
    public int deleteImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        ImFriend condition = new ImFriend();
        condition.setUserId(userId);
        condition.setFriendId(friendId);
        return imFriendMapper.deleteImFriendByCondition(condition);
    }
}
