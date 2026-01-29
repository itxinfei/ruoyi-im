package com.ruoyi.im.service.impl;

import com.ruoyi.im.constants.StatusConstants;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.dto.contact.ImFriendUpdateRequest;
import com.ruoyi.im.dto.conversation.ImPrivateConversationCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.mapper.ImFriendRequestMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 好友服务实现
 *
 * @author ruoyi
 */
@Service
public class ImFriendServiceImpl implements ImFriendService {

    private static final Logger log = LoggerFactory.getLogger(ImFriendServiceImpl.class);

    private final ImFriendMapper imFriendMapper;
    private final ImFriendRequestMapper imFriendRequestMapper;
    private final ImUserMapper imUserMapper;
    private final ImConversationService imConversationService;
    private final com.ruoyi.im.util.ImDistributedLock distributedLock;
    private final com.ruoyi.im.util.ImRedisUtil imRedisUtil;

    /**
     * 构造器注入依赖
     */
    public ImFriendServiceImpl(ImFriendMapper imFriendMapper,
                                ImFriendRequestMapper imFriendRequestMapper,
                                ImUserMapper imUserMapper,
                                ImConversationService imConversationService,
                                com.ruoyi.im.util.ImDistributedLock distributedLock,
                                com.ruoyi.im.util.ImRedisUtil imRedisUtil) {
        this.imFriendMapper = imFriendMapper;
        this.imFriendRequestMapper = imFriendRequestMapper;
        this.imUserMapper = imUserMapper;
        this.imConversationService = imConversationService;
        this.distributedLock = distributedLock;
        this.imRedisUtil = imRedisUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendFriendRequest(ImFriendAddRequest request, Long userId) {
        // 检查是否添加自己为好友
        if (userId.equals(request.getTargetUserId())) {
            throw new BusinessException("不能添加自己为好友");
        }

        // 查询目标用户是否存在
        ImUser toUser = imUserMapper.selectImUserById(request.getTargetUserId());
        if (toUser == null) {
            throw new BusinessException("目标用户不存在");
        }

        // 检查是否已经是好友
        ImFriend query = new ImFriend();
        query.setUserId(userId);
        query.setFriendId(request.getTargetUserId());
        List<ImFriend> existingFriends = imFriendMapper.selectImFriendList(query);
        if (existingFriends != null && !existingFriends.isEmpty()) {
            ImFriend existingFriend = existingFriends.get(0);
            // 使用isDeleted字段判断是否已删除
            if (existingFriend.getIsDeleted() != null && existingFriend.getIsDeleted() == 1) {
                // 恢复已删除的好友关系
                existingFriend.setIsDeleted(0);
                existingFriend.setDeletedTime(null);
                existingFriend.setGroupName(request.getGroupName());
                existingFriend.setUpdateTime(LocalDateTime.now());
                imFriendMapper.updateImFriend(existingFriend);

                // 清除缓存
                clearFriendListCache(userId);

                return existingFriend.getId();
            } else {
                throw new BusinessException("已经是好友关系");
            }
        }

        // 创建好友申请
        ImFriendRequest friendRequest = new ImFriendRequest();
        friendRequest.setFromUserId(userId);
        friendRequest.setToUserId(request.getTargetUserId());
        friendRequest.setMessage(request.getMessage());
        friendRequest.setStatus(StatusConstants.FriendRequest.PENDING);
        friendRequest.setCreateTime(LocalDateTime.now());
        imFriendRequestMapper.insertImFriendRequest(friendRequest);

        return friendRequest.getId();
    }

    @Override
    public List<ImFriendRequest> getSentRequests(Long userId) {
        ImFriendRequest query = new ImFriendRequest();
        query.setFromUserId(userId);
        List<ImFriendRequest> requestList = imFriendRequestMapper.selectImFriendRequestList(query);

        List<ImFriendRequest> pendingList = new ArrayList<>();
        for (ImFriendRequest request : requestList) {
            if (StatusConstants.FriendRequest.PENDING.equals(request.getStatus())) {
                // 查询目标用户信息
                ImUser toUser = imUserMapper.selectImUserById(request.getToUserId());
                if (toUser != null) {
                    request.setFromUserName(toUser.getNickname() != null ? toUser.getNickname() : toUser.getUsername());
                    request.setFromUserAvatar(toUser.getAvatar());
                }
                pendingList.add(request);
            }
        }

        return pendingList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFriend(Long friendId, ImFriendUpdateRequest request, Long userId) {
        ImFriend friend = imFriendMapper.selectImFriendById(friendId);
        if (friend == null) {
            throw new BusinessException("好友关系不存在");
        }

        // 只能修改自己的好友关系
        if (!friend.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        // 只能修改备注、分组
        if (request.getRemark() != null) {
            friend.setRemark(request.getRemark());
        }
        if (request.getGroupName() != null) {
            friend.setGroupName(request.getGroupName());
        }
        friend.setUpdateTime(LocalDateTime.now());
        imFriendMapper.updateImFriend(friend);

        // 清除缓存
        clearFriendListCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long friendId, Long userId) {
        ImFriend friend = imFriendMapper.selectImFriendById(friendId);
        if (friend == null) {
            throw new BusinessException("好友关系不存在");
        }

        // 只能删除自己的好友关系
        if (!friend.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        // 软删除：设置is_deleted标记
        friend.setIsDeleted(1);
        friend.setDeletedTime(LocalDateTime.now());
        friend.setUpdateTime(LocalDateTime.now());
        imFriendMapper.updateImFriend(friend);

        // 清除缓存
        clearFriendListCache(userId);
    }

    @Override
    public List<ImFriendVO> getFriendList(Long userId) {
        String cacheKey = "contact:list:" + userId;

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<ImFriendVO> cachedList = (List<ImFriendVO>) imRedisUtil.get(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        ImFriend query = new ImFriend();
        query.setUserId(userId);
        List<ImFriend> friendList = imFriendMapper.selectImFriendList(query);

        // 使用Map进行去重，同一个friendId只保留一条记录
        // 如果有重复的friendId，保留ID最小的记录（最早创建的）
        Map<Long, ImFriend> uniqueFriendMap = new HashMap<>();
        for (ImFriend friend : friendList) {
            // SQL已过滤is_deleted=0的记录，这里保留双重检查确保安全
            if (friend.getIsDeleted() != null && friend.getIsDeleted() == 1) {
                continue;
            }

            Long friendId = friend.getFriendId();
            ImFriend existing = uniqueFriendMap.get(friendId);
            if (existing == null) {
                uniqueFriendMap.put(friendId, friend);
            } else {
                // 如果已存在，保留ID较小的记录（通常是更早创建的）
                if (friend.getId() < existing.getId()) {
                    uniqueFriendMap.put(friendId, friend);
                    log.warn("发现重复好友记录，保留较早的记录: userId={}, friendId={}, 保留ID={}, 跳过ID={}",
                            userId, friendId, friend.getId(), existing.getId());
                } else {
                    log.warn("发现重复好友记录，保留较早的记录: userId={}, friendId={}, 保留ID={}, 跳过ID={}",
                            userId, friendId, existing.getId(), friend.getId());
                }
            }
        }

        // 批量查询好友用户信息，避免N+1查询问题
        List<ImFriendVO> voList = new ArrayList<>();
        if (!uniqueFriendMap.isEmpty()) {
            // 获取所有好友ID
            List<Long> friendIds = new ArrayList<>(uniqueFriendMap.keySet());
            // 批量查询用户信息（需要在ImUserMapper中添加selectImUserByIds方法）
            Map<Long, ImUser> userMap = batchGetUsers(friendIds);

            for (ImFriend friend : uniqueFriendMap.values()) {
                ImFriendVO vo = new ImFriendVO();
                BeanUtils.copyProperties(friend, vo);

                // 从批量查询结果中获取用户信息
                ImUser friendUser = userMap.get(friend.getFriendId());
                if (friendUser != null) {
                    vo.setFriendName(
                            friendUser.getNickname() != null ? friendUser.getNickname() : friendUser.getUsername());
                    vo.setFriendAvatar(friendUser.getAvatar());
                    vo.setUsername(friendUser.getUsername());
                    vo.setEmail(friendUser.getEmail());
                    vo.setPhone(friendUser.getMobile());
                    vo.setSignature(friendUser.getSignature());
                    vo.setDepartment(friendUser.getDepartment());
                    vo.setPosition(friendUser.getPosition());

                    // 如果用户状态是ACTIVE，可以认为是在线的（这只是一个简化判断）
                    // 实际在线状态应从Redis获取
                    vo.setOnline(imRedisUtil.isOnlineUser(friend.getFriendId()));
                } else {
                    vo.setOnline(false);
                }
                voList.add(vo);
            }
        }

        // 存入缓存，过期时间30分钟
        imRedisUtil.set(cacheKey, voList, 30, java.util.concurrent.TimeUnit.MINUTES);

        return voList;
    }

    private void clearFriendListCache(Long userId) {
        String cacheKey = "contact:list:" + userId;
        imRedisUtil.delete(cacheKey);
    }

    /**
     * 批量获取用户信息，避免N+1查询问题
     * 
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户信息的映射
     */
    /**
     * 批量获取用户信息，避免N+1查询问题
     * 
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户信息的映射
     */
    private Map<Long, ImUser> batchGetUsers(List<Long> userIds) {
        Map<Long, ImUser> userMap = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return userMap;
        }

        try {
            List<ImUser> users = imUserMapper.selectImUserListByIds(userIds);
            if (users != null) {
                for (ImUser user : users) {
                    userMap.put(user.getId(), user);
                }
            }
            log.debug("批量查询用户信息: 查询{}个用户，成功获取{}个", userIds.size(), userMap.size());
        } catch (Exception e) {
            log.error("批量查询用户信息失败: {}", e.getMessage(), e);
            // 本地降级：逐个查询
            for (Long userId : userIds) {
                ImUser user = imUserMapper.selectImUserById(userId);
                if (user != null) {
                    userMap.put(userId, user);
                }
            }
        }

        return userMap;
    }

    @Override
    public List<ImFriendRequest> getReceivedRequests(Long userId) {
        List<ImFriendRequest> requestList = imFriendRequestMapper.selectImFriendRequestListByToUserId(userId);

        List<ImFriendRequest> pendingList = new ArrayList<>();
        for (ImFriendRequest request : requestList) {
            if (StatusConstants.FriendRequest.PENDING.equals(request.getStatus())) {
                ImUser fromUser = imUserMapper.selectImUserById(request.getFromUserId());
                if (fromUser != null) {
                    request.setFromUserName(
                            fromUser.getNickname() != null ? fromUser.getNickname() : fromUser.getUsername());
                    request.setFromUserAvatar(fromUser.getAvatar());
                }
                pendingList.add(request);
            }
        }

        return pendingList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFriendRequest(Long requestId, Boolean approved, Long userId) {
        ImFriendRequest request = imFriendRequestMapper.selectImFriendRequestById(requestId);
        if (request == null) {
            throw new BusinessException("好友申请不存在");
        }

        if (!request.getToUserId().equals(userId)) {
            throw new BusinessException("无权限操作该申请");
        }

        if (approved) {
            final Long fromUserId = request.getFromUserId();
            final Long toUserId = request.getToUserId();

            // 使用分布式锁防止并发创建好友关系导致的数据不一致
            distributedLock.executeWithLock(
                    com.ruoyi.im.util.ImDistributedLock.LockKeys.friendRelationKey(fromUserId, toUserId),
                    15,
                    () -> doCreateFriendRelationship(fromUserId, toUserId, request));
        } else {
            // 拒绝
            request.setStatus(StatusConstants.FriendRequest.REJECTED);
            request.setHandledTime(LocalDateTime.now());
            imFriendRequestMapper.updateImFriendRequest(request);
        }
    }

    /**
     * 实际执行创建好友关系的逻辑
     */
    private Boolean doCreateFriendRelationship(Long fromUserId, Long toUserId, ImFriendRequest friendRequest) {
        LocalDateTime now = LocalDateTime.now();

        // ========== 为接收方（当前用户）创建好友记录 ==========
        ImFriend checkReceiver = new ImFriend();
        checkReceiver.setUserId(toUserId);
        checkReceiver.setFriendId(fromUserId);
        List<ImFriend> existingReceiver = imFriendMapper.selectImFriendList(checkReceiver);

        if (existingReceiver != null && !existingReceiver.isEmpty()) {
            // 已存在，恢复软删除的记录（如果有）
            ImFriend existing = existingReceiver.get(0);
            if (existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
                existing.setIsDeleted(0);
                existing.setDeletedTime(null);
                existing.setGroupName("默认分组");
                existing.setUpdateTime(now);
                imFriendMapper.updateImFriend(existing);
                log.info("恢复已删除的好友关系: userId={}, friendId={}", toUserId, fromUserId);
            } else {
                log.warn("好友关系已存在，跳过创建: userId={}, friendId={}", toUserId, fromUserId);
            }
        } else {
            // 不存在，创建新记录
            ImFriend friendForReceiver = new ImFriend();
            friendForReceiver.setUserId(toUserId);
            friendForReceiver.setFriendId(fromUserId);
            friendForReceiver.setRemark("");
            friendForReceiver.setGroupName("默认分组");
            friendForReceiver.setCreateTime(now);
            friendForReceiver.setUpdateTime(now);
            imFriendMapper.insertImFriend(friendForReceiver);
        }

        // ========== 为发送方（申请者）创建好友记录 ==========
        ImFriend checkSender = new ImFriend();
        checkSender.setUserId(fromUserId);
        checkSender.setFriendId(toUserId);
        List<ImFriend> existingSender = imFriendMapper.selectImFriendList(checkSender);

        if (existingSender != null && !existingSender.isEmpty()) {
            // 已存在，恢复软删除的记录（如果有）
            ImFriend existing = existingSender.get(0);
            if (existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
                existing.setIsDeleted(0);
                existing.setDeletedTime(null);
                existing.setGroupName("默认分组");
                existing.setUpdateTime(now);
                imFriendMapper.updateImFriend(existing);
                log.info("恢复已删除的好友关系: userId={}, friendId={}", fromUserId, toUserId);
            } else {
                log.warn("好友关系已存在，跳过创建: userId={}, friendId={}", fromUserId, toUserId);
            }
        } else {
            // 不存在，创建新记录
            ImFriend friendForSender = new ImFriend();
            friendForSender.setUserId(fromUserId);
            friendForSender.setFriendId(toUserId);
            friendForSender.setRemark("");
            friendForSender.setGroupName("默认分组");
            friendForSender.setCreateTime(now);
            friendForSender.setUpdateTime(now);
            imFriendMapper.insertImFriend(friendForSender);
        }

        // 清除双方缓存
        clearFriendListCache(fromUserId);
        clearFriendListCache(toUserId);

        // 创建双向私聊会话（双方都能看到会话）
        createPrivateSession(toUserId, fromUserId);
        createPrivateSession(fromUserId, toUserId);

        // 更新申请状态
        friendRequest.setStatus("APPROVED");
        friendRequest.setHandledTime(now);
        imFriendRequestMapper.updateImFriendRequest(friendRequest);
        return true;
    }

    @Override
    public List<ImContactGroupVO> getGroupedFriendList(Long userId) {
        // 复用缓存的getFriendList
        List<ImFriendVO> friendList = getFriendList(userId);

        // 按分组组织去重后的好友
        Map<String, List<ImFriendVO>> groupMap = new HashMap<>();
        for (ImFriendVO friend : friendList) {
            String groupName = friend.getGroupName();
            if (groupName == null) {
                groupName = "默认分组";
            }
            if (!groupMap.containsKey(groupName)) {
                groupMap.put(groupName, new ArrayList<>());
            }
            groupMap.get(groupName).add(friend);
        }

        List<ImContactGroupVO> result = new ArrayList<>();
        for (Map.Entry<String, List<ImFriendVO>> entry : groupMap.entrySet()) {
            ImContactGroupVO vo = new ImContactGroupVO();
            vo.setGroupName(entry.getKey());
            vo.setFriends(entry.getValue());
            vo.setCount(entry.getValue().size());
            result.add(vo);
        }

        return result;
    }

    @Override
    public ImFriendVO getFriendById(Long friendId, Long userId) {
        ImFriend friend = imFriendMapper.selectImFriendById(friendId);
        if (friend == null) {
            throw new BusinessException("好友关系不存在");
        }

        if (!friend.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }

        ImFriendVO vo = new ImFriendVO();
        BeanUtils.copyProperties(friend, vo);

        // 查询好友用户信息
        ImUser friendUser = imUserMapper.selectImUserById(friend.getFriendId());
        if (friendUser != null) {
            vo.setFriendName(friendUser.getNickname() != null ? friendUser.getNickname() : friendUser.getUsername());
            vo.setFriendAvatar(friendUser.getAvatar());
            vo.setDepartment(friendUser.getDepartment());
            vo.setPosition(friendUser.getPosition());
            // 根据用户状态判断是否在线
            vo.setOnline(imRedisUtil.isOnlineUser(friend.getFriendId()));
        } else {
            vo.setOnline(false);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blockFriend(Long friendId, Boolean blocked, Long userId) {
        ImFriend friend = imFriendMapper.selectImFriendById(friendId);
        if (friend == null) {
            throw new BusinessException("好友关系不存在");
        }

        if (!friend.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        // 注意：当前数据库表没有单独的拉黑字段，拉黑功能暂时通过删除好友实现
        // 如需完整的拉黑功能，需要在im_friend表添加is_blocked字段
        if (blocked) {
            // 拉黑 = 删除好友
            friend.setIsDeleted(1);
            friend.setDeletedTime(LocalDateTime.now());
        }
        friend.setUpdateTime(LocalDateTime.now());
        imFriendMapper.updateImFriend(friend);

        // 清除缓存
        clearFriendListCache(userId);
    }

    @Override
    public List<ImUserVO> searchUsers(String keyword, Long userId) {
        // 根据关键词搜索用户
        List<ImUser> userList = imUserMapper.selectImUserByKeyword(keyword);

        List<ImUserVO> result = new ArrayList<>();
        for (ImUser user : userList) {
            // 排除自己和已经是好友的用户
            if (user.getId().equals(userId)) {
                continue;
            }

            // 检查是否已经是好友（SQL已过滤is_deleted=0）
            ImFriend query = new ImFriend();
            query.setUserId(userId);
            query.setFriendId(user.getId());
            List<ImFriend> existingFriends = imFriendMapper.selectImFriendList(query);

            // 如果已经是好友，则跳过
            boolean isFriend = existingFriends != null && !existingFriends.isEmpty();
            if (isFriend) {
                continue;
            }

            ImUserVO vo = new ImUserVO();
            BeanUtils.copyProperties(user, vo);
            // 可以添加额外的状态标识
            result.add(vo);
        }

        return result;
    }

    /**
     * 创建私聊会话
     */
    private void createPrivateSession(Long userId, Long peerId) {
        ImPrivateConversationCreateRequest request = new ImPrivateConversationCreateRequest();
        request.setPeerUserId(peerId);
        imConversationService.createPrivateConversation(userId, request);
    }

    @Override
    public List<String> getGroupNames(Long userId) {
        // 复用 getFriendList 缓存
        List<ImFriendVO> friendList = getFriendList(userId);

        return friendList.stream()
                .map(ImFriendVO::getGroupName)
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameGroup(Long userId, String oldName, String newName) {
        if (oldName == null || oldName.isEmpty()) {
            throw new BusinessException("分组名称不能为空");
        }
        if (newName == null || newName.isEmpty()) {
            throw new BusinessException("新分组名称不能为空");
        }

        ImFriend query = new ImFriend();
        query.setUserId(userId);
        query.setGroupName(oldName);
        List<ImFriend> friendList = imFriendMapper.selectImFriendList(query);

        if (friendList.isEmpty()) {
            throw new BusinessException("分组不存在");
        }

        for (ImFriend friend : friendList) {
            friend.setGroupName(newName);
            friend.setUpdateTime(LocalDateTime.now());
            imFriendMapper.updateImFriend(friend);
        }

        // 清除缓存
        clearFriendListCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long userId, String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            throw new BusinessException("分组名称不能为空");
        }

        ImFriend query = new ImFriend();
        query.setUserId(userId);
        query.setGroupName(groupName);
        List<ImFriend> friendList = imFriendMapper.selectImFriendList(query);

        for (ImFriend friend : friendList) {
            friend.setGroupName(null);
            friend.setUpdateTime(LocalDateTime.now());
            imFriendMapper.updateImFriend(friend);
        }

        // 清除缓存
        clearFriendListCache(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFriendsToGroup(Long userId, List<Long> friendIds, String groupName) {
        if (friendIds == null || friendIds.isEmpty()) {
            throw new BusinessException("好友ID列表不能为空");
        }

        for (Long friendId : friendIds) {
            ImFriend friend = imFriendMapper.selectImFriendById(friendId);
            if (friend == null) {
                continue;
            }

            // 只能修改自己的好友关系
            if (!friend.getUserId().equals(userId)) {
                continue;
            }

            friend.setGroupName(groupName);
            friend.setUpdateTime(LocalDateTime.now());
            imFriendMapper.updateImFriend(friend);
        }

        // 清除缓存
        clearFriendListCache(userId);
    }
}
