package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImFriend;
import com.ruoyi.web.mapper.ImFriendMapper;
import com.ruoyi.web.service.ImFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 好友关系Service实现
 *
 * @author ruoyi
 */
@Service
public class ImFriendServiceImpl implements ImFriendService {

    @Autowired
    private ImFriendMapper imFriendMapper;

    @Override
    public ImFriend selectImFriendById(Long id) {
        return imFriendMapper.selectImFriendById(id);
    }

    @Override
    public List<ImFriend> selectImFriendList(ImFriend imFriend) {
        return imFriendMapper.selectImFriendList(imFriend);
    }

    @Override
    public List<ImFriend> selectFriendsByUserId(Long userId) {
        return imFriendMapper.selectFriendsByUserId(userId);
    }

    @Override
    public Map<String, List<ImFriend>> selectFriendsGrouped(Long userId) {
        List<ImFriend> friends = imFriendMapper.selectFriendsByUserId(userId);
        Map<String, List<ImFriend>> groupedMap = new LinkedHashMap<>();

        for (ImFriend friend : friends) {
            String groupName = friend.getGroupName();
            if (groupName == null || groupName.trim().isEmpty()) {
                groupName = "默认分组";
            }
            groupedMap.computeIfAbsent(groupName, k -> new ArrayList<>()).add(friend);
        }

        return groupedMap;
    }

    @Override
    public List<String> selectFriendGroups(Long userId) {
        return imFriendMapper.selectFriendGroups(userId);
    }

    @Override
    public int insertImFriend(ImFriend imFriend) {
        return imFriendMapper.insertImFriend(imFriend);
    }

    @Override
    public int updateImFriend(ImFriend imFriend) {
        return imFriendMapper.updateImFriend(imFriend);
    }

    @Override
    public int deleteImFriendByIds(Long[] ids) {
        return imFriendMapper.deleteImFriendByIds(ids);
    }

    @Override
    public int deleteFriendByUserAndFriend(Long userId, Long friendId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("friendId", friendId);
        return imFriendMapper.deleteFriendByUserAndFriend(params);
    }

    @Override
    public Map<String, Object> getFriendStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", imFriendMapper.countTotalFriends());
        result.put("todayAdded", imFriendMapper.countTodayAddedFriends());
        result.put("pendingRequests", imFriendMapper.countPendingRequests());
        return result;
    }
}
