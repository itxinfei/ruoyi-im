package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.service.ImFriendGroupService;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 好友分组与标签服务实现
 */
@Service
public class ImFriendGroupServiceImpl implements ImFriendGroupService {

    private final ImFriendMapper friendMapper;
    private final ImFriendService friendService;

    public ImFriendGroupServiceImpl(ImFriendMapper friendMapper, ImFriendService friendService) {
        this.friendMapper = friendMapper;
        this.friendService = friendService;
    }

    @Override
    public List<ImContactGroupVO> getGroupedFriendList(Long userId) {
        List<ImFriendVO> allFriends = friendService.getFriendList(userId);
        Map<String, List<ImFriendVO>> grouped = allFriends.stream()
                .collect(Collectors.groupingBy(f -> f.getGroupName() != null ? f.getGroupName() : "默认分组"));

        return grouped.entrySet().stream().map(entry -> {
            ImContactGroupVO vo = new ImContactGroupVO();
            vo.setGroupName(entry.getKey());
            vo.setContacts(entry.getValue());
            vo.setCount(entry.getValue().size());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> getGroupNames(Long userId) {
        // 简单实现，从好友列表中提取所有唯一分组名
        return friendMapper.selectImFriendListByUserId(userId).stream()
                .map(ImFriend::getGroupName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameGroup(Long userId, String oldName, String newName) {
        List<ImFriend> friends = friendMapper.selectImFriendListByUserId(userId);
        friends.stream().filter(f -> oldName.equals(f.getGroupName())).forEach(f -> {
            f.setGroupName(newName);
            friendMapper.updateById(f);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long userId, String groupName) {
        renameGroup(userId, groupName, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFriendsToGroup(Long userId, List<Long> friendIds, String groupName) {
        if (friendIds == null) return;
        friendIds.forEach(fid -> {
            ImFriend f = friendMapper.selectImFriendByUserIdAndFriendId(userId, fid);
            if (f != null) {
                f.setGroupName(groupName);
                friendMapper.updateById(f);
            }
        });
    }

    @Override
    public List<String> getUserTags(Long userId) { return Collections.emptyList(); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFriendTags(Long userId, Long friendId, List<String> tags) {
        ImFriend f = friendMapper.selectImFriendByUserIdAndFriendId(userId, friendId);
        if (f != null) {
            f.setTags(tags != null ? String.join(",", tags) : null);
            friendMapper.updateById(f);
        }
    }

    @Override
    public List<Long> getFriendIdsByTag(Long userId, String tag) {
        return friendMapper.selectImFriendListByUserId(userId).stream()
                .filter(f -> f.getTags() != null && Arrays.asList(f.getTags().split(",")).contains(tag))
                .map(ImFriend::getFriendId)
                .collect(Collectors.toList());
    }
}
