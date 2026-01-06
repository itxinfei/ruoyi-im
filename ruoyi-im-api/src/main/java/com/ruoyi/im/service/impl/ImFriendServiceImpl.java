package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.dto.contact.ImFriendUpdateRequest;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 好友服务实现
 *
 * @author ruoyi
 */
@Service
public class ImFriendServiceImpl implements ImFriendService {

    @Override
    public Long sendFriendRequest(ImFriendAddRequest request, Long userId) {
        // TODO: 实现发送好友申请逻辑
        // 1. 检查是否已经是好友
        // 2. 检查是否已有待处理的申请
        // 3. 创建好友申请记录
        // 4. 发送通知给目标用户
        return 1L;
    }

    @Override
    public List<ImFriendRequest> getReceivedRequests(Long userId) {
        // TODO: 从数据库查询收到的好友申请
        List<ImFriendRequest> list = new ArrayList<>();

        ImFriendRequest request = new ImFriendRequest();
        request.setId(1L);
        request.setFromUserId(2L);
        request.setToUserId(userId);
        request.setMessage("你好，我想加你为好友");
        request.setStatus("PENDING");
        request.setCreateTime(LocalDateTime.now());
        request.setFromUserName("张三");
        request.setFromUserAvatar("/avatar/user2.png");
        list.add(request);

        return list;
    }

    @Override
    public List<ImFriendRequest> getSentRequests(Long userId) {
        // TODO: 从数据库查询发送的好友申请
        List<ImFriendRequest> list = new ArrayList<>();
        return list;
    }

    @Override
    public void handleFriendRequest(Long requestId, Boolean approved, Long userId) {
        // TODO: 实现处理好友申请逻辑
        // 1. 更新申请状态
        // 2. 如果同意，创建双向好友关系
        // 3. 发送通知给申请人
    }

    @Override
    public List<ImFriendVO> getFriendList(Long userId) {
        // TODO: 从数据库查询好友列表
        List<ImFriendVO> list = new ArrayList<>();

        ImFriendVO vo = new ImFriendVO();
        vo.setId(1L);
        vo.setFriendId(2L);
        vo.setFriendName("张三");
        vo.setFriendAvatar("/avatar/user2.png");
        vo.setRemark("我的好友");
        vo.setGroupName("默认分组");
        vo.setStatus("NORMAL");
        vo.setOnline(true);
        vo.setCreateTime(LocalDateTime.now());
        list.add(vo);

        return list;
    }

    @Override
    public List<ImContactGroupVO> getGroupedFriendList(Long userId) {
        // TODO: 从数据库查询分组好友列表
        List<ImContactGroupVO> list = new ArrayList<>();

        ImContactGroupVO groupVO = new ImContactGroupVO();
        groupVO.setGroupName("默认分组");
        groupVO.setCount(1);
        groupVO.setFriends(getFriendList(userId));
        list.add(groupVO);

        return list;
    }

    @Override
    public ImFriendVO getFriendById(Long friendId, Long userId) {
        // TODO: 从数据库查询好友信息
        ImFriendVO vo = new ImFriendVO();
        vo.setFriendId(friendId);
        vo.setFriendName("张三");
        vo.setFriendAvatar("/avatar/user2.png");
        vo.setStatus("NORMAL");
        vo.setOnline(true);
        return vo;
    }

    @Override
    public void updateFriend(Long friendId, ImFriendUpdateRequest request, Long userId) {
        // TODO: 实现更新好友信息逻辑
        // 1. 更新备注和分组
    }

    @Override
    public void deleteFriend(Long friendId, Long userId) {
        // TODO: 实现删除好友逻辑
        // 1. 删除双向好友关系
        // 2. 删除相关会话
    }

    @Override
    public void blockFriend(Long friendId, Boolean blocked, Long userId) {
        // TODO: 实现拉黑/解除拉黑逻辑
        // 1. 更新好友状态为BLOCKED或NORMAL
    }

    @Override
    public List<ImUserVO> searchUsers(String keyword, Long userId) {
        // TODO: 实现搜索用户逻辑
        // 1. 根据用户名或手机号搜索
        // 2. 过滤自己和已经是好友的用户
        List<ImUserVO> list = new ArrayList<>();

        ImUserVO vo = new ImUserVO();
        vo.setId(2L);
        vo.setUsername("张三");
        vo.setNickname("张三");
        vo.setAvatar("/avatar/user2.png");
        list.add(vo);

        return list;
    }
}
