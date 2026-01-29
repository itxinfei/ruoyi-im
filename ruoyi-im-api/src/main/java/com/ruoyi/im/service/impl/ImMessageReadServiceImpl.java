package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageRead;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImMessageReadMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImMessageReadService;
import com.ruoyi.im.constants.StatusConstants;
import com.ruoyi.im.util.MessageEncryptionUtil;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import com.ruoyi.im.vo.message.ImMessageReadDetailVO;
import com.ruoyi.im.vo.message.ImMessageReadStatusVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息已读回执服务实现
 *
 * @author ruoyi
 */
@Service
public class ImMessageReadServiceImpl implements ImMessageReadService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageReadServiceImpl.class);

    @Autowired
    private ImMessageReadMapper messageReadMapper;

    @Autowired
    private ImMessageMapper messageMapper;

    @Autowired
    private ImConversationMapper conversationMapper;

    @Autowired
    private ImConversationMemberMapper conversationMemberMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Autowired
    private MessageEncryptionUtil encryptionUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markMessageAsRead(Long conversationId, Long messageId, Long userId) {
        // 验证消息是否存在
        ImMessage message = messageMapper.selectImMessageById(messageId);
        if (message == null) {
            log.warn("消息不存在: messageId={}", messageId);
            return;
        }

        // 验证用户是否是该会话的成员
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            log.warn("用户不是会话成员: conversationId={}, userId={}", conversationId, userId);
            return;
        }

        // 不能标记自己发送的消息为已读
        if (message.getSenderId().equals(userId)) {
            return;
        }

        // 检查是否已经标记过已读
        ImMessageRead existing = messageReadMapper.selectByMessageIdAndUserId(messageId, userId);
        if (existing != null) {
            log.debug("消息已标记为已读: messageId={}, userId={}", messageId, userId);
            return;
        }

        // 创建已读记录
        ImMessageRead messageRead = new ImMessageRead();
        messageRead.setMessageId(messageId);
        messageRead.setUserId(userId);
        messageRead.setConversationId(conversationId);
        messageRead.setReadTime(LocalDateTime.now());
        messageRead.setReadType("MANUAL"); // 手动标记
        messageRead.setDeviceType("WEB");
        messageRead.setCreateTime(LocalDateTime.now());

        messageReadMapper.insertImMessageRead(messageRead);

        // 更新会话成员的未读数
        Integer currentUnread = member.getUnreadCount();
        if (currentUnread != null && currentUnread > 0) {
            conversationMemberMapper.decrementUnreadCount(conversationId, userId, 1);
        }

        // 广播已读状态给发送者
        broadcastReadStatus(messageId, userId, message.getSenderId());

        log.info("标记消息已读: messageId={}, userId={}, senderId={}", messageId, userId, message.getSenderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markMessagesAsRead(Long conversationId, List<Long> messageIds, Long userId) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        for (Long messageId : messageIds) {
            try {
                markMessageAsRead(conversationId, messageId, userId);
            } catch (Exception e) {
                log.error("标记消息已读失败: messageId={}, userId={}", messageId, userId, e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markConversationAsRead(Long conversationId, Long upToMessageId, Long userId) {
        // 验证会话是否存在
        ImConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }

        // 验证用户是否是该会话的成员
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("用户不是会话成员");
        }

        // 如果未指定消息ID，获取会话最新消息ID
        Long actualUpToMessageId = upToMessageId;
        if (actualUpToMessageId == null) {
            LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<ImMessage>()
                    .eq(ImMessage::getConversationId, conversationId)
                    .orderByDesc(ImMessage::getId)
                    .last("1");
            ImMessage latestMessage = messageMapper.selectOne(queryWrapper);
            if (latestMessage != null) {
                actualUpToMessageId = latestMessage.getId();
            }
        }

        // 获取需要标记为已读的消息列表
        // 查询该会话中最后已读消息ID之后、直到upToMessageId的所有消息
        List<ImMessage> messagesToMark = getMessagesToMarkAsRead(conversationId, userId, actualUpToMessageId);

        for (ImMessage message : messagesToMark) {
            try {
                markMessageAsRead(conversationId, message.getId(), userId);
            } catch (Exception e) {
                log.error("标记消息已读失败: messageId={}, userId={}", message.getId(), userId, e);
            }
        }

        // 清零未读数
        conversationMemberMapper.updateUnreadCount(conversationId, userId, 0);
        conversationMemberMapper.updateLastReadMessageId(conversationId, userId, upToMessageId);

        log.info("标记会话所有消息已读: conversationId={}, userId={}, count={}",
            conversationId, userId, messagesToMark.size());
    }

    @Override
    public ImMessageReadStatusVO getMessageReadStatus(Long messageId, Long senderId) {
        // 验证权限
        ImMessage message = messageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        if (!message.getSenderId().equals(senderId)) {
            throw new BusinessException("只有发送者可以查看已读状态");
        }

        ImMessageReadStatusVO status = new ImMessageReadStatusVO();
        status.setMessageId(messageId);
        status.setConversationId(message.getConversationId());
        status.setCanViewReadStatus(true);

        // 获取会话信息以确定总人数
        ImConversation conversation = conversationMapper.selectById(message.getConversationId());
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }

        int totalCount;
        if (StatusConstants.ConversationType.GROUP.equals(conversation.getType())) {
            // 群聊：获取群组成员数
            List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversation.getId());
            totalCount = members.size();
        } else {
            // 私聊：总共2人
            totalCount = 2;
        }

        // 获取已读记录
        List<ImMessageRead> readRecords = messageReadMapper.selectImMessageReadList(
            new ImMessageRead() {{ setMessageId(messageId); }}
        );
        int readCount = readRecords.size();

        status.setTotalCount(totalCount);
        status.setReadCount(readCount);
        status.setUnreadCount(totalCount - readCount);

        if (totalCount > 0) {
            status.setReadPercent((readCount * 100) / totalCount);
        } else {
            status.setReadPercent(0);
        }

        status.setAllRead(readCount >= totalCount);

        // 获取最近已读用户名称
        if (!readRecords.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (ImMessageRead read : readRecords) {
                if (count >= 3) break;
                ImUser user = userMapper.selectImUserById(read.getUserId());
                if (user != null) {
                    if (count > 0) sb.append("、");
                    sb.append(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    count++;
                }
            }
            if (readRecords.size() > 3) {
                sb.append("等").append(readRecords.size()).append("人");
            }
            status.setRecentReadUsers(sb.toString());
        }

        // 获取已读用户头像列表（最多5个）
        List<com.ruoyi.im.vo.message.ImMessageReadStatusVO.ReadUserAvatar> avatars = new ArrayList<>();
        int avatarCount = Math.min(5, readRecords.size());
        for (int i = 0; i < avatarCount; i++) {
            ImMessageRead read = readRecords.get(i);
            ImUser user = userMapper.selectImUserById(read.getUserId());
            if (user != null) {
                com.ruoyi.im.vo.message.ImMessageReadStatusVO.ReadUserAvatar avatar =
                    new com.ruoyi.im.vo.message.ImMessageReadStatusVO.ReadUserAvatar();
                avatar.setUserId(user.getId());
                avatar.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                avatar.setAvatar(user.getAvatar());
                avatar.setReadTime(read.getReadTime());
                avatars.add(avatar);
            }
        }
        status.setReadUserAvatars(avatars);

        return status;
    }

    @Override
    public ImMessageReadDetailVO getMessageReadDetail(Long messageId, Long senderId) {
        // 验证权限
        ImMessage message = messageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        if (!message.getSenderId().equals(senderId)) {
            throw new BusinessException("只有发送者可以查看已读详情");
        }

        ImMessageReadDetailVO detail = new ImMessageReadDetailVO();
        detail.setMessageId(messageId);
        detail.setConversationId(message.getConversationId());
        detail.setSendTime(message.getCreateTime());

        // 解密消息内容预览
        try {
            String content = encryptionUtil.decryptMessage(message.getContent());
            if (content != null && content.length() > 50) {
                detail.setMessagePreview(content.substring(0, 50) + "...");
            } else {
                detail.setMessagePreview(content);
            }
        } catch (Exception e) {
            detail.setMessagePreview("[消息内容]");
        }

        // 获取会话成员
        List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(message.getConversationId());
        detail.setTotalCount(members.size());

        // 获取已读记录
        List<ImMessageRead> readRecords = messageReadMapper.selectImMessageReadList(
            new ImMessageRead() {{ setMessageId(messageId); }}
        );

        List<Long> readUserIds = readRecords.stream()
            .map(ImMessageRead::getUserId)
            .collect(Collectors.toList());

        detail.setReadCount(readUserIds.size());
        detail.setUnreadCount(members.size() - readUserIds.size());

        // 构建已读用户列表
        List<ImMessageReadDetailVO.ReadUserInfo> readUsers = new ArrayList<>();
        for (ImMessageRead read : readRecords) {
            ImUser user = userMapper.selectImUserById(read.getUserId());
            if (user != null) {
                ImMessageReadDetailVO.ReadUserInfo userInfo = new ImMessageReadDetailVO.ReadUserInfo();
                userInfo.setUserId(user.getId());
                userInfo.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                userInfo.setAvatar(user.getAvatar());
                userInfo.setReadTime(read.getReadTime());
                readUsers.add(userInfo);
            }
        }
        detail.setReadUsers(readUsers);

        // 构建未读用户列表
        List<ImMessageReadDetailVO.UnreadUserInfo> unreadUsers = new ArrayList<>();
        for (ImConversationMember member : members) {
            if (!readUserIds.contains(member.getUserId())) {
                ImUser user = userMapper.selectImUserById(member.getUserId());
                if (user != null) {
                    ImMessageReadDetailVO.UnreadUserInfo userInfo = new ImMessageReadDetailVO.UnreadUserInfo();
                    userInfo.setUserId(user.getId());
                    userInfo.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    userInfo.setAvatar(user.getAvatar());
                    unreadUsers.add(userInfo);
                }
            }
        }
        detail.setUnreadUsers(unreadUsers);

        return detail;
    }

    @Override
    public List<ImMessageReadStatusVO> getConversationMessageReadStatus(Long conversationId, Long userId) {
        // 验证用户是否是该会话的成员
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("用户不是会话成员");
        }

        // 获取会话中当前用户发送的消息
        List<ImMessage> messages = messageMapper.selectList(
            new LambdaQueryWrapper<ImMessage>()
                .eq(ImMessage::getConversationId, conversationId)
                .eq(ImMessage::getSenderId, userId)
                .orderByDesc(ImMessage::getCreateTime)
                .last("100") // 只返回最近100条
        );

        List<ImMessageReadStatusVO> statusList = new ArrayList<>();
        for (ImMessage message : messages) {
            try {
                ImMessageReadStatusVO status = getMessageReadStatus(message.getId(), userId);
                statusList.add(status);
            } catch (Exception e) {
                log.error("获取消息已读状态失败: messageId={}", message.getId(), e);
            }
        }

        return statusList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeReadReceipt(Long messageId, Long userId) {
        ImMessageRead existing = messageReadMapper.selectByMessageIdAndUserId(messageId, userId);
        if (existing != null) {
            messageReadMapper.deleteImMessageReadById(existing.getId());
            log.info("撤回已读回执: messageId={}, userId={}", messageId, userId);
        }
    }

    /**
     * 广播已读状态给发送者
     */
    private void broadcastReadStatus(Long messageId, Long readerUserId, Long senderId) {
        try {
            Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            javax.websocket.Session senderSession = onlineUsers.get(senderId);

            if (senderSession != null && senderSession.isOpen()) {
                // 获取读者信息
                ImUser reader = userMapper.selectImUserById(readerUserId);
                String readerName = reader != null ?
                    (reader.getNickname() != null ? reader.getNickname() : reader.getUsername()) : "未知用户";

                Map<String, Object> readNotification = new HashMap<>();
                readNotification.put("type", "read");
                readNotification.put("messageId", messageId);
                readNotification.put("userId", readerUserId);
                readNotification.put("userName", readerName);
                readNotification.put("timestamp", LocalDateTime.now().toString());

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String json = mapper.writeValueAsString(readNotification);
                senderSession.getBasicRemote().sendText(json);

                log.debug("已广播已读状态: messageId={}, readerId={}, senderId={}", messageId, readerUserId, senderId);
            }
        } catch (Exception e) {
            log.error("广播已读状态失败: messageId={}, readerId={}", messageId, readerUserId, e);
        }
    }

    /**
     * 获取需要标记为已读的消息列表
     */
    private List<ImMessage> getMessagesToMarkAsRead(Long conversationId, Long userId, Long upToMessageId) {
        // 获取用户最后已读消息ID
        ImConversationMember member = conversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        Long lastReadMessageId = member != null ? member.getLastReadMessageId() : null;

        // 查询需要标记的消息
        LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<ImMessage>()
            .eq(ImMessage::getConversationId, conversationId)
            .ne(ImMessage::getSenderId, userId); // 排除自己发送的消息

        if (lastReadMessageId != null) {
            queryWrapper.gt(ImMessage::getId, lastReadMessageId);
        }

        if (upToMessageId != null) {
            queryWrapper.le(ImMessage::getId, upToMessageId);
        }

        queryWrapper.orderByAsc(ImMessage::getId);

        return messageMapper.selectList(queryWrapper);
    }
}
