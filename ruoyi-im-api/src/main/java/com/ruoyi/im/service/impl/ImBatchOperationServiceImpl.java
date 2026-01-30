package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImBatchOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量操作服务实现
 *
 * 为指定用户批量发送好友请求、加入群组
 *
 * @author ruoyi
 */
@Service
public class ImBatchOperationServiceImpl implements ImBatchOperationService {

    private static final Logger logger = LoggerFactory.getLogger(ImBatchOperationServiceImpl.class);

    private final ImUserMapper imUserMapper;
    private final ImFriendMapper imFriendMapper;
    private final ImGroupMapper imGroupMapper;
    private final ImGroupMemberMapper imGroupMemberMapper;

    /**
     * 构造器注入依赖
     */
    public ImBatchOperationServiceImpl(ImUserMapper imUserMapper,
                                       ImFriendMapper imFriendMapper,
                                       ImGroupMapper imGroupMapper,
                                       ImGroupMemberMapper imGroupMemberMapper) {
        this.imUserMapper = imUserMapper;
        this.imFriendMapper = imFriendMapper;
        this.imGroupMapper = imGroupMapper;
        this.imGroupMemberMapper = imGroupMemberMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeBatchOperationsForUsers(String[] nicknames) {
        Map<String, Object> totalResult = new HashMap<>();
        List<Map<String, Object>> userResults = new ArrayList<>();

        int totalFriendRequests = 0;
        int totalGroupJoins = 0;
        int totalFailed = 0;

        for (String nickname : nicknames) {
            Map<String, Object> result = executeBatchOperationsForUser(nickname);
            userResults.add(result);

            // 累加统计
            Integer successFriends = (Integer) result.get("successFriendCount");
            Integer successGroups = (Integer) result.get("successGroupCount");
            Integer failed = (Integer) result.get("failedCount");

            totalFriendRequests += successFriends != null ? successFriends : 0;
            totalGroupJoins += successGroups != null ? successGroups : 0;
            totalFailed += failed != null ? failed : 0;
        }

        totalResult.put("operation", "批量操作");
        totalResult.put("targetUsers", nicknames);
        totalResult.put("totalSuccessFriendRequests", totalFriendRequests);
        totalResult.put("totalSuccessGroupJoins", totalGroupJoins);
        totalResult.put("totalFailed", totalFailed);
        totalResult.put("userResults", userResults);

        logger.info("批量操作完成: 目标用户={}, 成功好友请求={}, 成功群组加入={}, 失败={}",
                nicknames, totalFriendRequests, totalGroupJoins, totalFailed);

        return totalResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeBatchOperationsForUser(String nickname) {
        Map<String, Object> result = new HashMap<>();

        // 1. 根据昵称查询用户ID
        ImUser targetUser = findUserByNickname(nickname);
        if (targetUser == null) {
            result.put("error", "用户不存在: " + nickname);
            result.put("successFriendCount", 0);
            result.put("successGroupCount", 0);
            result.put("failedCount", 1);
            return result;
        }

        Long userId = targetUser.getId();
        logger.info("开始为用户执行批量操作: userId={}, nickname={}", userId, nickname);

        int friendSuccessCount = 0;
        int groupSuccessCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            // 2. 获取系统中所有未删除的用户（排除目标用户自己）
            List<ImUser> allUsers = getAllActiveUsers(userId);
            logger.info("查询到 {} 个其他用户", allUsers.size());

            // 3. 向所有其他用户发送好友请求（直接建立好友关系，绕过申请流程）
            for (ImUser otherUser : allUsers) {
                try {
                    createFriendRelationship(userId, otherUser.getId());
                    friendSuccessCount++;
                    logger.debug("成功创建好友关系: {} <-> {}", nickname, otherUser.getNickname());
                } catch (Exception e) {
                    logger.warn("创建好友关系失败: {} <-> {}, 原因: {}",
                            nickname, otherUser.getNickname(), e.getMessage());
                    errors.add("好友关系失败-" + otherUser.getNickname() + ": " + e.getMessage());
                    failedCount++;
                }
            }

            // 4. 获取系统中所有群组
            List<ImGroup> allGroups = getAllActiveGroups();
            logger.info("查询到 {} 个群组", allGroups.size());

            // 5. 加入所有群组（作为普通成员）
            for (ImGroup group : allGroups) {
                try {
                    joinGroupAsMember(userId, group.getId());
                    groupSuccessCount++;
                    logger.debug("成功加入群组: userId={}, groupName={}", userId, group.getName());
                } catch (Exception e) {
                    logger.warn("加入群组失败: userId={}, groupName={}, 原因: {}",
                            userId, group.getName(), e.getMessage());
                    errors.add("群组加入失败-" + group.getName() + ": " + e.getMessage());
                    failedCount++;
                }
            }

        } catch (Exception e) {
            logger.error("批量操作执行异常: userId={}, nickname={}", userId, nickname, e);
            errors.add("系统异常: " + e.getMessage());
            failedCount++;
        }

        // 组装结果
        result.put("userId", userId);
        result.put("nickname", nickname);
        result.put("successFriendCount", friendSuccessCount);
        result.put("successGroupCount", groupSuccessCount);
        result.put("failedCount", failedCount);
        result.put("errors", errors);

        logger.info("用户 {} 批量操作完成: 好友关系={}, 群组加入={}, 失败={}",
                nickname, friendSuccessCount, groupSuccessCount, failedCount);

        return result;
    }

    /**
     * 根据昵称查找用户
     */
    private ImUser findUserByNickname(String nickname) {
        // 使用 keyword 搜索
        List<ImUser> users = imUserMapper.selectImUserByKeyword(nickname);
        if (users != null && !users.isEmpty()) {
            // 精确匹配昵称
            for (ImUser user : users) {
                if (nickname.equals(user.getNickname()) || nickname.equals(user.getUsername())) {
                    return user;
                }
            }
            // 如果没有精确匹配，返回第一个模糊匹配结果
            return users.get(0);
        }
        return null;
    }

    /**
     * 获取所有活跃用户（排除指定用户）
     * 使用 Mapper 提供的查询方法
     */
    private List<ImUser> getAllActiveUsers(Long excludeUserId) {
        // 获取所有用户列表，然后过滤
        ImUser queryParam = new ImUser();
        List<ImUser> allUsers = imUserMapper.selectImUserList(queryParam);

        // 过滤掉排除的用户和已删除用户
        List<ImUser> result = new ArrayList<>();
        for (ImUser user : allUsers) {
            if (!user.getId().equals(excludeUserId)) {
                result.add(user);
            }
        }
        return result;
    }

    /**
     * 获取所有活跃群组
     * 使用 Mapper 提供的查询方法
     */
    private List<ImGroup> getAllActiveGroups() {
        // 获取所有群组列表
        ImGroup queryParam = new ImGroup();
        queryParam.setIsDeleted(0);
        return imGroupMapper.selectImGroupList(queryParam);
    }

    /**
     * 创建好友关系（双向）
     */
    private void createFriendRelationship(Long userId1, Long userId2) {
        // 检查是否已存在好友关系
        ImFriend existing1 = imFriendMapper.selectImFriendByUserIdAndFriendId(userId1, userId2);
        if (existing1 != null && existing1.getIsDeleted() == 0) {
            logger.debug("好友关系已存在: {} <-> {}", userId1, userId2);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 如果存在已删除的关系，恢复它
        if (existing1 != null && existing1.getIsDeleted() == 1) {
            existing1.setIsDeleted(0);
            existing1.setDeletedTime(null);
            existing1.setUpdateTime(now);
            imFriendMapper.updateById(existing1);
        } else {
            // 创建新关系 user1 -> user2
            ImFriend friend1 = new ImFriend();
            friend1.setUserId(userId1);
            friend1.setFriendId(userId2);
            friend1.setIsDeleted(0);
            friend1.setCreateTime(now);
            friend1.setUpdateTime(now);
            imFriendMapper.insert(friend1);
        }

        // 创建/恢复反向关系 user2 -> user1
        ImFriend existing2 = imFriendMapper.selectImFriendByUserIdAndFriendId(userId2, userId1);
        if (existing2 != null && existing2.getIsDeleted() == 1) {
            existing2.setIsDeleted(0);
            existing2.setDeletedTime(null);
            existing2.setUpdateTime(now);
            imFriendMapper.updateById(existing2);
        } else if (existing2 == null) {
            ImFriend friend2 = new ImFriend();
            friend2.setUserId(userId2);
            friend2.setFriendId(userId1);
            friend2.setIsDeleted(0);
            friend2.setCreateTime(now);
            friend2.setUpdateTime(now);
            imFriendMapper.insert(friend2);
        }
    }

    /**
     * 加入群组作为普通成员
     */
    private void joinGroupAsMember(Long userId, Long groupId) {
        // 检查是否已经是群组成员
        ImGroupMember existing = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (existing != null && existing.getIsDeleted() == 0) {
            logger.debug("用户 {} 已是群组 {} 的成员", userId, groupId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 如果存在已删除的成员记录，恢复它
        if (existing != null && existing.getIsDeleted() == 1) {
            existing.setIsDeleted(0);
            existing.setDeletedTime(null);
            existing.setUpdateTime(now);
            existing.setRole("MEMBER");
            imGroupMemberMapper.updateById(existing);
        } else {
            // 创建新成员记录
            ImGroupMember member = new ImGroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole("MEMBER");
            member.setIsMuted(0);
            member.setIsDeleted(0);
            member.setCreateTime(now);
            member.setUpdateTime(now);
            imGroupMemberMapper.insert(member);
        }

        // 更新群组成员数量
        ImGroup group = imGroupMapper.selectById(groupId);
        if (group != null) {
            Integer memberCount = imGroupMemberMapper.countMembersByGroupId(groupId);
            group.setMemberCount(memberCount);
            group.setUpdateTime(now);
            imGroupMapper.updateById(group);
        }
    }
}
