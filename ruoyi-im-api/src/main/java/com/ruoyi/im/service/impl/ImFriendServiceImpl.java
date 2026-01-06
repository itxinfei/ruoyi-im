package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.service.ImFriendService;

/**
 * 婵傝棄寮窼ervice娑撴艾濮熺仦鍌氼槱閻? * 
 * @author ruoyi
 */
@Service
public class ImFriendServiceImpl implements ImFriendService
{
    @Autowired
    private ImFriendMapper imFriendMapper;

    /**
     * 閺屻儴顕楁總钘夊几
     * 
     * @param id 婵傝棄寮窱D
     * @return 婵傝棄寮?
     */
    @Override
    public ImFriend selectImFriendById(Long id)
    {
        return imFriendMapper.selectImFriendById(id);
    }

    /**
     * 閺屻儴顕楁總钘夊几閸掓銆?
     * 
     * @param imFriend 婵傝棄寮?
     * @return 婵傝棄寮?
     */
    @Override
    public List<ImFriend> selectImFriendList(ImFriend imFriend)
    {
        return imFriendMapper.selectImFriendList(imFriend);
    }

    /**
     * 閺傛澘顤冩總钘夊几
     * 
     * @param imFriend 婵傝棄寮?
     * @return 缂佹挻鐏?
     */
    @Override
    public int insertImFriend(ImFriend imFriend)
    {
        return imFriendMapper.insertImFriend(imFriend);
    }

    /**
     * 娣囶喗鏁兼總钘夊几
     * 
     * @param imFriend 婵傝棄寮?
     * @return 缂佹挻鐏?
     */
    @Override
    public int updateImFriend(ImFriend imFriend)
    {
        return imFriendMapper.updateImFriend(imFriend);
    }

    /**
     * 閹靛綊鍣洪崚鐘绘珟婵傝棄寮?
     * 
     * @param ids 闂団偓鐟曚礁鍨归梽銈囨畱閺佺増宓両D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImFriendByIds(Long[] ids)
    {
        return imFriendMapper.deleteImFriendByIds(ids);
    }

    /**
     * 閸掔娀娅庢總钘夊几娣団剝浼?
     * 
     * @param id 婵傝棄寮窱D
     * @return 缂佹挻鐏?
     */
    @Override
    public int deleteImFriendById(Long id)
    {
        return imFriendMapper.deleteImFriendById(id);
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕ID閸滃苯銈介崣濠璂閺屻儴顕楁總钘夊几閸忓磭閮?
     * 
     * @param userId 閻劍鍩汭D
     * @param friendId 婵傝棄寮窱D
     * @return 婵傝棄寮搁崗宕囬兇
     */
    @Override
    public ImFriend selectImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        // 閸掓稑缂撻弻銉嚄閺夆€叉
        ImFriend condition = new ImFriend();
        condition.setUserId(userId);
        condition.setFriendId(friendId);
        List<ImFriend> friends = imFriendMapper.selectImFriendList(condition);
        return friends != null && !friends.isEmpty() ? friends.get(0) : null;
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕ID閺屻儴顕楁總钘夊几閸掓銆?
     * 
     * @param userId 閻劍鍩汭D
     * @return 婵傝棄寮搁崚妤勩€?
     */
    @Override
    public List<ImFriend> selectImFriendListByUserId(Long userId) {
        // 閸掓稑缂撻弻銉嚄閺夆€叉
        ImFriend condition = new ImFriend();
        condition.setUserId(userId);
        return imFriendMapper.selectImFriendList(condition);
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕ID閸滃苯銈介崣濠璂閸掔娀娅庢總钘夊几閸忓磭閮?
     * 
     * @param userId 閻劍鍩汭D
     * @param friendId 婵傝棄寮窱D
     * @return 閸掔娀娅庣紒鎾寸亯
     */
    @Override
    public int deleteImFriendByUserIdAndFriendId(Long userId, Long friendId) {
        ImFriend condition = new ImFriend();
        condition.setUserId(userId);
        condition.setFriendId(friendId);
        return imFriendMapper.deleteImFriendByCondition(condition);
    }
}
