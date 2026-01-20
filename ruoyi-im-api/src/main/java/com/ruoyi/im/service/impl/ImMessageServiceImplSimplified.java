package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.conversation.ImPrivateConversationCreateRequest;
import com.ruoyi.im.dto.mention.ImMentionInfo;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.utils.MessageEncryptionUtil;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImMessageServiceImpl implements ImMessageService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);

    private final ImMessageMapper messageMapper;
    private final ImUserMapper userMapper;
    private final ImConversationMapper conversationMapper;
    private final ImConversationMemberMapper memberMapper;
    private final ImConversationService conversationService;
    private final MessageEncryptionUtil encryptionUtil;
    private final ImMessageMentionService mentionService;

    public ImMessageServiceImpl(
            ImMessageMapper messageMapper,
            ImUserMapper userMapper,
            ImConversationMapper conversationMapper,
            ImConversationMemberMapper memberMapper,
            ImConversationService conversationService,
            MessageEncryptionUtil encryptionUtil,
            ImMessageMentionService mentionService) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
        this.conversationMapper = conversationMapper;
        this.memberMapper = memberMapper;
        this.conversationService = conversationService;
        this.encryptionUtil = encryptionUtil;
        this.mentionService = mentionService;
    }

    @Override
    @Transactional
    public Long sendMessage(ImMessageSendRequest request, Long userId) {
        ImUser sender = userMapper.selectImUserById(userId);
        if (sender == null) {
            throw new BusinessException("发送者不存在");
        }

        Long conversationId = getOrCreateConversation(request, userId);

        ImMessage message = createMessage(request, userId, conversationId);
        messageMapper.insertImMessage(message);

        updateConversationLastMessage(conversationId, message);
        updateUnreadCounts(conversationId, userId);
        processMentions(message, request);

        return message.getId();
    }

    @Override
    public List<ImMessageVO> getMessages(Long conversationId, Long userId, Long lastId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100;
        }

        return messageMapper.selectMessagesByConversation(conversationId, userId, lastId, limit);
    }

    @Override
    @Transactional
    public void recallMessage(Long messageId, Long userId) {
        ImMessage message = messageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("只能撤回自己的消息");
        }

        if (message.getCreateTime().plusMinutes(2).isBefore(LocalDateTime.now())) {
            throw new BusinessException("消息发送超过2分钟，无法撤回");
        }

        message.setIsRevoked(1);
        message.setUpdateTime(LocalDateTime.now());
        messageMapper.updateById(message);
    }

    @Override
    @Transactional
    public void editMessage(Long messageId, String newContent, Long userId) {
        ImMessage message = messageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("只能编辑自己的消息");
        }

        if (!"TEXT".equals(message.getMessageType())) {
            throw new BusinessException("只能编辑文本消息");
        }

        if (message.getCreateTime().plusMinutes(15).isBefore(LocalDateTime.now())) {
            throw new BusinessException("消息发送超过15分钟，无法编辑");
        }

        String encryptedContent = encryptionUtil.encryptMessage(newContent);
        message.setContent(encryptedContent);
        message.setIsEdited(1);
        message.setUpdateTime(LocalDateTime.now());
        messageMapper.updateById(message);
    }

    @Override
    public Long forwardMessage(Long messageId, Long toConversationId, Long toUserId, String content, Long userId) {
        ImMessage originalMessage = messageMapper.selectImMessageById(messageId);
        if (originalMessage == null) {
            throw new BusinessException("原消息不存在");
        }

        Long targetConversationId = toConversationId;
        if (targetConversationId == null && toUserId != null) {
            ImPrivateConversationCreateRequest request = new ImPrivateConversationCreateRequest();
            request.setPeerUserId(toUserId);
            targetConversationId = conversationService.createPrivateConversation(userId, request);
        }

        ImMessage forwardMessage = new ImMessage();
        forwardMessage.setConversationId(targetConversationId);
        forwardMessage.setSenderId(userId);
        forwardMessage.setMessageType(originalMessage.getMessageType());
        forwardMessage.setContent(encryptionUtil.encryptMessage(content != null ? content : originalMessage.getContent()));
        forwardMessage.setForwardFromMessageId(messageId);
        forwardMessage.setCreateTime(LocalDateTime.now());

        messageMapper.insertImMessage(forwardMessage);
        return forwardMessage.getId();
    }

    @Override
    public Long replyMessage(Long messageId, String content, Long userId) {
        ImMessage originalMessage = messageMapper.selectImMessageById(messageId);
        if (originalMessage == null) {
            throw new BusinessException("原消息不存在");
        }

        ImMessage replyMessage = new ImMessage();
        replyMessage.setConversationId(originalMessage.getConversationId());
        replyMessage.setSenderId(userId);
        replyMessage.setMessageType("TEXT");
        replyMessage.setContent(encryptionUtil.encryptMessage(content));
        replyMessage.setReplyToMessageId(messageId);
        replyMessage.setCreateTime(LocalDateTime.now());

        messageMapper.insertImMessage(replyMessage);
        return replyMessage.getId();
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long userId, List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        for (Long messageId : messageIds) {
            memberMapper.updateLastReadMessage(conversationId, userId, messageId, LocalDateTime.now());
        }
    }

    @Override
    public ImMessageSearchResultVO searchMessages(Long conversationId, String keyword, String messageType,
                                            Long senderId, LocalDateTime startTime,
                                            LocalDateTime endTime, Integer pageNum, Integer pageSize,
                                            Boolean includeRevoked, Boolean exactMatch, Long userId) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        List<ImMessageVO> messages = messageMapper.searchMessages(
                conversationId, keyword, messageType, senderId, startTime, endTime,
                (pageNum - 1) * pageSize, pageSize, includeRevoked, exactMatch, userId);

        Long totalCount = messageMapper.countSearchMessages(
                conversationId, keyword, messageType, senderId, startTime, endTime,
                includeRevoked, exactMatch, userId);

        ImMessageSearchResultVO result = new ImMessageSearchResultVO();
        result.setMessages(messages);
        result.setTotal(totalCount);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotalPages((int) Math.ceil((double) totalCount / pageSize));

        return result;
    }

    @Override
    public int getTodayMessageCount(Long userId) {
        return messageMapper.countTodayMessages(userId);
    }

    private Long getOrCreateConversation(ImMessageSendRequest request, Long userId) {
        Long conversationId = request.getConversationId();

        if (conversationId == null || conversationId == 0) {
            if (request.getReceiverId() == null || request.getReceiverId() == 0) {
                throw new BusinessException("会话ID和接收者ID不能同时为空");
            }

            ImPrivateConversationCreateRequest createRequest = new ImPrivateConversationCreateRequest();
            createRequest.setPeerUserId(request.getReceiverId());
            conversationId = conversationService.createPrivateConversation(userId, createRequest);
        } else {
            ImConversation conversation = conversationMapper.selectById(conversationId);
            if (conversation == null) {
                throw new BusinessException("会话不存在");
            }
        }

        return conversationId;
    }

    private ImMessage createMessage(ImMessageSendRequest request, Long userId, Long conversationId) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setReceiverId(request.getReceiverId());
        message.setMessageType(request.getType());
        message.setReplyToMessageId(request.getReplyToMessageId());
        message.setContent(encryptionUtil.encryptMessage(request.getContent()));
        message.setIsRevoked(0);
        message.setIsEdited(0);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }

    private void updateConversationLastMessage(Long conversationId, ImMessage message) {
        ImConversation conversationUpdate = new ImConversation();
        conversationUpdate.setId(conversationId);
        conversationUpdate.setLastMessageId(message.getId());
        conversationUpdate.setLastMessageTime(message.getCreateTime());
        conversationUpdate.setUpdateTime(LocalDateTime.now());
        conversationMapper.updateById(conversationUpdate);
    }

    private void updateUnreadCounts(Long conversationId, Long excludeUserId) {
        List<ImConversationMember> members = memberMapper.selectByConversationId(conversationId);
        for (ImConversationMember member : members) {
            if (!member.getUserId().equals(excludeUserId)) {
                memberMapper.incrementUnreadCount(conversationId, member.getUserId(), 1);
            }
        }
    }

    private void processMentions(ImMessage message, ImMessageSendRequest request) {
        ImMentionInfo mentionInfo = request.getMentionInfo();
        if (mentionInfo == null && request.getContent() != null) {
            mentionInfo = mentionService.parseMentions(request.getContent());
        }

        if (mentionInfo != null && (mentionInfo.getUserIds() != null || Boolean.TRUE.equals(mentionInfo.getMentionAll()))) {
            mentionInfo.setConversationId(message.getConversationId());
            mentionService.createMentions(message.getId(), mentionInfo, message.getSenderId());
        }
    }
}