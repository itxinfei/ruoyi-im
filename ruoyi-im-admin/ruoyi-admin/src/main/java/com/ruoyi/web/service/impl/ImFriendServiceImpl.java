package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImFriend;
import com.ruoyi.web.mapper.ImFriendMapper;
import com.ruoyi.web.service.ImFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 删除指定好友关系（同时删除双向关系）
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteFriendByUserAndFriend(Long userId, Long friendId) {
        int result = 0;
        // 删除 A->B 的好友关系
        Map<String, Object> params1 = new HashMap<>();
        params1.put("userId", userId);
        params1.put("friendId", friendId);
        result += imFriendMapper.deleteFriendByUserAndFriend(params1);

        // 删除 B->A 的好友关系（如果存在）
        Map<String, Object> params2 = new HashMap<>();
        params2.put("userId", friendId);
        params2.put("friendId", userId);
        result += imFriendMapper.deleteFriendByUserAndFriend(params2);

        return result;
    }

    /**
     * 查询两个用户之间是否存在好友关系
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 好友关系，不存在返回null
     */
    @Override
    public ImFriend selectImFriendByUsers(Long userId1, Long userId2) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId1);
        params.put("friendId", userId2);
        return imFriendMapper.selectFriendByUsers(params);
    }

    /**
     * 获取好友统计数据
     *
     * @return 统计数据，包含totalCount、pendingCount、groupCount、blockedCount
     */
    @Override
    public Map<String, Object> getFriendStatistics() {
        Map<String, Object> result = new HashMap<>();
        // 好友关系总数
        result.put("totalCount", imFriendMapper.countTotalFriends());
        // 待处理好友请求数
        result.put("pendingCount", imFriendMapper.countPendingRequests());
        // 好友分组数（去重统计）
        List<String> groups = imFriendMapper.selectDistinctGroups();
        result.put("groupCount", groups != null ? groups.size() : 0);
        // 拉黑好友数（暂未实现，返回0）
        result.put("blockedCount", 0);
        return result;
    }
}
