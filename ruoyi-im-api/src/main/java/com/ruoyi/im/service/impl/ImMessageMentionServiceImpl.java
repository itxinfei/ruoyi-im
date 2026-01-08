package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.domain.ImMessageMention;
import com.ruoyi.im.dto.mention.ImMentionInfo;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.mapper.ImMessageMentionMapper;
import com.ruoyi.im.service.ImMessageMentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息@提及服务实现
 *
 * @author ruoyi
 */
@Service
public class ImMessageMentionServiceImpl implements ImMessageMentionService {

    @Autowired
    private ImMessageMentionMapper mentionMapper;

    @Autowired
    private ImConversationMapper conversationMapper;

    @Autowired
    private ImGroupMemberMapper groupMemberMapper;

    @Override
    @Transactional
    public void createMentions(Long messageId, ImMentionInfo mentionInfo, Long senderId) {
        if (mentionInfo == null) {
            return;
        }

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
                }
            }
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
            if (!"OWNER".equals(role) && !"ADMIN".equals(role)) {
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
}
