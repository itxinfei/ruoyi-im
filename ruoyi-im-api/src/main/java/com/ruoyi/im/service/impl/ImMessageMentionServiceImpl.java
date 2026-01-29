package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.mention.ImMentionInfo;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImMessageMentionMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.constants.StatusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 消息@提及服务实现
 *
 * @author ruoyi
 */
@Service
public class ImMessageMentionServiceImpl implements ImMessageMentionService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageMentionServiceImpl.class);

    @Autowired
    private ImMessageMentionMapper mentionMapper;

    @Autowired
    private ImConversationMapper conversationMapper;

    @Autowired
    private ImGroupMemberMapper groupMemberMapper;

    @Autowired
    private ImConversationMemberMapper conversationMemberMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Autowired
    private ImMessageMapper messageMapper;

    @Override
    @Transactional
    public void createMentions(Long messageId, ImMentionInfo mentionInfo, Long senderId) {
        if (mentionInfo == null) {
            return;
        }

        List<Long> mentionedUserIds = new ArrayList<>();

        // 处理@所有人的情况
        if (Boolean.TRUE.equals(mentionInfo.getMentionAll())) {
            // 验证权限：只有群主和管理员可以使用@所有人
            if (mentionInfo.getConversationId() != null) {
                validateMentionAllPermission(mentionInfo.getConversationId(), senderId);
            }

            // 获取群组所有成员并创建提及记录
            List<Long> allMemberIds = getAllGroupMemberIds(mentionInfo.getConversationId());
            if (allMemberIds != null && !allMemberIds.isEmpty()) {
                for (Long memberId : allMemberIds) {
                    // 不@发送者自己
                    if (!memberId.equals(senderId)) {
                        ImMessageMention mention = new ImMessageMention();
                        mention.setMessageId(messageId);
                        mention.setMentionedUserId(memberId);
                        mention.setMentionedBy(senderId);
                        mention.setMentionType("ALL");
                        mention.setIsRead(0);
                        mention.setCreateTime(LocalDateTime.now());
                        mentionMapper.insert(mention);
                        mentionedUserIds.add(memberId);
                    }
                }
            } else {
                // 如果无法获取群成员列表，创建一条@所有人的记录作为标记
                ImMessageMention mention = new ImMessageMention();
                mention.setMessageId(messageId);
                mention.setMentionedUserId(-1L); // -1表示@所有人
                mention.setMentionedBy(senderId);
                mention.setMentionType("ALL");
                mention.setIsRead(0);
                mention.setCreateTime(LocalDateTime.now());
                mentionMapper.insert(mention);
            }
        }

        // 处理@特定用户的情况
        if (mentionInfo.getUserIds() != null && !mentionInfo.getUserIds().isEmpty()) {
            for (Long userId : mentionInfo.getUserIds()) {
                // 不@自己
                if (!userId.equals(senderId)) {
                    ImMessageMention mention = new ImMessageMention();
                    mention.setMessageId(messageId);
                    mention.setMentionedUserId(userId);
                    mention.setMentionedBy(senderId);
                    mention.setMentionType("USER");
                    mention.setIsRead(0);
                    mention.setCreateTime(LocalDateTime.now());

                    mentionMapper.insert(mention);
                    mentionedUserIds.add(userId);
                }
            }
        }

        // 通过WebSocket通知被@的用户
        if (!mentionedUserIds.isEmpty()) {
            broadcastMentionNotification(messageId, mentionedUserIds, senderId, mentionInfo.getConversationId());
        }
    }

    /**
     * 验证用户是否有权限使用@所有人
     * 只有群主和管理员可以使用@所有人
     *
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     */
    private void validateMentionAllPermission(Long conversationId, Long senderId) {
        ImConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return; // 会话不存在，不验证
        }

        // 只对群聊进行权限验证
        if ("GROUP".equals(conversation.getType())) {
            Long groupId = conversation.getTargetId();
            ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, senderId);

            if (member == null) {
                throw new BusinessException("您不是该群组成员");
            }

            String role = member.getRole();
            if (!StatusConstants.GroupMemberRole.OWNER.equals(role)
                    && !StatusConstants.GroupMemberRole.ADMIN.equals(role)) {
                throw new BusinessException("只有群主和管理员可以使用@所有人");
            }
        }
    }

    /**
     * 获取群组所有成员ID
     *
     * @param conversationId 会话ID
     * @return 成员ID列表
     */
    private List<Long> getAllGroupMemberIds(Long conversationId) {
        ImConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !"GROUP".equals(conversation.getType())) {
            return null;
        }

        Long groupId = conversation.getTargetId();
        List<ImGroupMember> members = groupMemberMapper.selectImGroupMemberListByGroupId(groupId);

        return members.stream()
                .map(ImGroupMember::getUserId)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<ImMessageMention> getUnreadMentions(Long userId) {
        return mentionMapper.selectUnreadMentions(userId);
    }

    @Override
    public int getUnreadMentionCount(Long userId) {
        return mentionMapper.countUnreadMentions(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        mentionMapper.markAsRead(messageId, userId);
    }

    @Override
    @Transactional
    public void batchMarkAsRead(List<Long> mentionIds) {
        if (mentionIds != null && !mentionIds.isEmpty()) {
            mentionMapper.batchMarkAsRead(mentionIds);
        }
    }

    @Override
    public ImMentionInfo parseMentions(String content) {
        ImMentionInfo mentionInfo = new ImMentionInfo();
        List<Long> userIds = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            return mentionInfo;
        }

        // 检测@所有人
        Pattern mentionAllPattern = Pattern.compile("@所有人|@all");
        Matcher mentionAllMatcher = mentionAllPattern.matcher(content);
        if (mentionAllMatcher.find()) {
            mentionInfo.setMentionAll(true);
            mentionInfo.setMentionAllType("ALL");
        }

        // 检测@用户（格式：@{userId:用户名} 或 @userId）
        Pattern mentionUserPattern = Pattern.compile("\\@\\{(\\d+):[^}]+\\}|@(\\d+)");
        Matcher mentionUserMatcher = mentionUserPattern.matcher(content);

        while (mentionUserMatcher.find()) {
            String userIdStr = mentionUserMatcher.group(1) != null ? mentionUserMatcher.group(1) : mentionUserMatcher.group(2);
            try {
                Long userId = Long.parseLong(userIdStr);
                if (!userIds.contains(userId)) {
                    userIds.add(userId);
                }
            } catch (NumberFormatException e) {
                // 忽略无效的用户ID
            }
        }

        mentionInfo.setUserIds(userIds);
        return mentionInfo;
    }

    /**
     * 获取会话中可以@的用户列表
     *
     * @param conversationId 会话ID
     * @param keyword         搜索关键词
     * @return 可@的用户列表
     */
    public List<Map<String, Object>> getMentionableUsers(Long conversationId, String keyword) {
        List<Map<String, Object>> users = new ArrayList<>();

        ImConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return users;
        }

        // 获取会话成员
        List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(conversationId);

        for (ImConversationMember member : members) {
            ImUser user = userMapper.selectImUserById(member.getUserId());
            if (user == null || user.getStatus() == null || user.getStatus() == 0) {
                continue;
            }

            // 过滤关键词
            if (keyword != null && !keyword.isEmpty()) {
                String name = user.getNickname() != null ? user.getNickname() : user.getUsername();
                if (!name.toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }
            }

            Map<String, Object> userInfo = new java.util.HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("userName", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("displayName", user.getNickname() != null ? user.getNickname() : user.getUsername());

            users.add(userInfo);
        }

        return users;
    }

    /**
     * 检查用户是否可以被@所有人
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 是否可以@所有人
     */
    public boolean canMentionAll(Long conversationId, Long userId) {
        try {
            validateMentionAllPermission(conversationId, userId);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }

    /**
     * 广播@提及通知给被@的用户
     *
     * @param messageId        消息ID
     * @param mentionedUserIds 被@的用户ID列表
     * @param senderId         发送者ID
     * @param conversationId    会话ID
     */
    private void broadcastMentionNotification(Long messageId, List<Long> mentionedUserIds,
                                               Long senderId, Long conversationId) {
        try {
            // 获取发送者信息
            ImUser sender = userMapper.selectImUserById(senderId);
            String senderName = sender != null ?
                (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : "未知用户";

            // 获取消息信息
            ImMessage message = messageMapper.selectImMessageById(messageId);

            // 构建通知数据
            Map<String, Object> notification = new java.util.HashMap<>();
            notification.put("type", "mention");
            notification.put("messageId", messageId);
            notification.put("conversationId", conversationId);
            notification.put("senderId", senderId);
            notification.put("senderName", senderName);
            notification.put("timestamp", LocalDateTime.now().toString());

            if (message != null) {
                notification.put("messageType", message.getMessageType());
                notification.put("messagePreview", getMessagePreview(message.getContent()));
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String messageJson = mapper.writeValueAsString(notification);

            // 获取在线用户
            Map<Long, javax.websocket.Session> onlineUsers =
                com.ruoyi.im.websocket.ImWebSocketEndpoint.getOnlineUsers();

            // 向被@的在线用户发送通知
            for (Long mentionedUserId : mentionedUserIds) {
                javax.websocket.Session targetSession = onlineUsers.get(mentionedUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    try {
                        targetSession.getBasicRemote().sendText(messageJson);
                        log.info("已发送@提及通知: messageId={}, mentionedUserId={}", messageId, mentionedUserId);
                    } catch (Exception e) {
                        log.error("发送@提及通知失败: userId={}", mentionedUserId, e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("广播@提及通知失败: messageId={}", messageId, e);
        }
    }

    /**
     * 获取消息预览
     *
     * @param content 消息内容
     * @return 预览文本
     */
    private String getMessagePreview(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // 如果是加密内容，尝试解密
        if (content.length() > 50) {
            return content.substring(0, 50) + "...";
        }
        return content;
    }
}
