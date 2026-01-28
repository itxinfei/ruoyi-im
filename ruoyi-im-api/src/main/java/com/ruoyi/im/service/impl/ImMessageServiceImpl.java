package com.ruoyi.im.service.impl;

import cn.hutool.http.HtmlUtil;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageEditHistory;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.conversation.ImPrivateConversationCreateRequest;
import com.ruoyi.im.dto.mention.ImMentionInfo;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageEditHistoryMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImSystemConfigService;
import com.ruoyi.im.util.AuditLogUtil;
import com.ruoyi.im.util.MessageEncryptionUtil;
import com.ruoyi.im.listener.BotMessageListener;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.im.util.SecurityUtils;

@Service
public class ImMessageServiceImpl implements ImMessageService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);

    @Value("${im.search.fulltext:false}")
    private boolean useFullTextSearch;

    private final ImMessageMapper imMessageMapper;
    private final ImUserMapper imUserMapper;
    private final ImConversationMapper imConversationMapper;
    private final ImConversationMemberMapper imConversationMemberMapper;
    private final ImConversationService imConversationService;
    private final MessageEncryptionUtil encryptionUtil;
    private final ImMessageMentionService messageMentionService;
    private final ImMessageEditHistoryMapper editHistoryMapper;
    private final com.ruoyi.im.util.ImDistributedLock distributedLock;
    private final com.ruoyi.im.util.ImRedisUtil redisUtil;
    private final com.ruoyi.im.service.ImWebSocketBroadcastService broadcastService;
    private final ImSystemConfigService systemConfigService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造器注入依赖
     */
    public ImMessageServiceImpl(ImMessageMapper imMessageMapper,
                                 ImUserMapper imUserMapper,
                                 ImConversationMapper imConversationMapper,
                                 ImConversationMemberMapper imConversationMemberMapper,
                                 ImConversationService imConversationService,
                                 MessageEncryptionUtil encryptionUtil,
                                 ImMessageMentionService messageMentionService,
                                 ImMessageEditHistoryMapper editHistoryMapper,
                                 com.ruoyi.im.util.ImDistributedLock distributedLock,
                                 com.ruoyi.im.util.ImRedisUtil redisUtil,
                                 com.ruoyi.im.service.ImWebSocketBroadcastService broadcastService,
                                 ImSystemConfigService systemConfigService,
                                 ApplicationEventPublisher eventPublisher) {
        this.imMessageMapper = imMessageMapper;
        this.imUserMapper = imUserMapper;
        this.imConversationMapper = imConversationMapper;
        this.imConversationMemberMapper = imConversationMemberMapper;
        this.imConversationService = imConversationService;
        this.encryptionUtil = encryptionUtil;
        this.messageMentionService = messageMentionService;
        this.editHistoryMapper = editHistoryMapper;
        this.distributedLock = distributedLock;
        this.redisUtil = redisUtil;
        this.broadcastService = broadcastService;
        this.systemConfigService = systemConfigService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImMessageVO sendMessage(ImMessageSendRequest request, Long userId) {
        // 幂等性检查：使用clientMsgId防止重复发送
        String clientMsgId = request.getClientMsgId();
        if (clientMsgId != null && !clientMsgId.isEmpty()) {
            Long existingMessageId = redisUtil.checkAndRecordClientMsgId(clientMsgId);
            if (existingMessageId != null) {
                log.info("消息已存在，跳过重复发送: clientMsgId={}, messageId={}", clientMsgId, existingMessageId);
                ImMessage existingMessage = imMessageMapper.selectImMessageById(existingMessageId);
                if (existingMessage != null) {
                    ImMessageVO vo = new ImMessageVO();
                    BeanUtils.copyProperties(existingMessage, vo);
                    vo.setContent(encryptionUtil.decryptMessage(existingMessage.getContent())); // 解密
                    vo.setIsSelf(existingMessage.getSenderId().equals(userId));
                    ImUser sender = imUserMapper.selectImUserById(existingMessage.getSenderId());
                    if (sender != null) {
                        vo.setSenderName(sender.getNickname());
                        vo.setSenderAvatar(sender.getAvatar());
                    }
                    return vo;
                }
            }
        }

        ImUser sender = imUserMapper.selectImUserById(userId);
        if (sender == null) {
            throw new BusinessException("发送者不存在");
        }

        Long conversationId = request.getConversationId();

        if (conversationId == null || conversationId == 0) {
            if (request.getReceiverId() == null || request.getReceiverId() == 0) {
                throw new BusinessException("会话ID和接收者ID不能同时为空");
            }

            ImPrivateConversationCreateRequest createRequest = new ImPrivateConversationCreateRequest();
            createRequest.setPeerUserId(request.getReceiverId());
            conversationId = imConversationService.createPrivateConversation(userId, createRequest);
        } else {
            ImConversation conversation = imConversationMapper.selectById(conversationId);
            if (conversation == null) {
                throw new BusinessException("会话不存在");
            }
            
            // 权限检查：验证用户是否是会话成员
            ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
            if (member == null || (member.getIsDeleted() != null && member.getIsDeleted() == 1)) {
                throw new BusinessException("您不是该会话成员，无法发送消息");
            }
        }

        // 使用分布式锁防止并发发送消息导致的数据不一致
        final Long finalConversationId = conversationId;
        final String finalClientMsgId = clientMsgId;
        ImMessageVO messageVO = distributedLock.executeWithLock(
                com.ruoyi.im.util.ImDistributedLock.LockKeys.sendMessageKey(finalConversationId),
                10, // 10秒过期时间足够
                () -> doSendMessage(request, userId, finalConversationId, sender, finalClientMsgId));

        return messageVO;
    }

    /**
     * 实际执行发送消息的逻辑
     * 支持消息发送状态追踪
     */
    private ImMessageVO doSendMessage(ImMessageSendRequest request, Long userId, Long conversationId, ImUser sender,
            String clientMsgId) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setReceiverId(request.getReceiverId());
        message.setMessageType(request.getType());
        message.setReplyToMessageId(request.getReplyToMessageId());

        String plainContent = request.getContent();
        // XSS防护：如果是文本消息，进行HTML转义
        if ("TEXT".equalsIgnoreCase(request.getType())) {
            plainContent = HtmlUtil.escape(plainContent);
        }

        String contentToSave = encryptionUtil.encryptMessage(plainContent);
        message.setContent(contentToSave);
        message.setIsRevoked(0);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        message.setClientMsgId(clientMsgId);
        message.setSendStatus("SENDING");
        message.setSendRetryCount(0);
        message.setDeliveredTime(LocalDateTime.now());

        imMessageMapper.insertImMessage(message);

        if (clientMsgId != null && !clientMsgId.isEmpty()) {
            redisUtil.recordClientMsgId(clientMsgId, message.getId());
        }

        ImConversation conversationUpdate = new ImConversation();
        conversationUpdate.setId(conversationId);
        conversationUpdate.setLastMessageId(message.getId());
        conversationUpdate.setLastMessageTime(message.getCreateTime());
        conversationUpdate.setUpdateTime(LocalDateTime.now());
        imConversationMapper.updateById(conversationUpdate);

        List<com.ruoyi.im.domain.ImConversationMember> members = imConversationMemberMapper
                .selectByConversationId(conversationId);

        boolean shouldEvictCache = members.size() <= 20;

        for (com.ruoyi.im.domain.ImConversationMember member : members) {
            if (!member.getUserId().equals(userId)) {
                imConversationMemberMapper.incrementUnreadCount(conversationId, member.getUserId(), 1);

                if (shouldEvictCache) {
                    redisUtil.delete("conversation:list:" + member.getUserId());
                }
            }
        }

        redisUtil.delete("conversation:list:" + userId);

        ImMentionInfo mentionInfo = request.getMentionInfo();
        if (mentionInfo == null && request.getContent() != null) {
            mentionInfo = messageMentionService.parseMentions(request.getContent());
        }

        if (mentionInfo != null
                && (mentionInfo.getUserIds() != null || Boolean.TRUE.equals(mentionInfo.getMentionAll()))) {
            mentionInfo.setConversationId(conversationId);
            messageMentionService.createMentions(message.getId(), mentionInfo, userId);
        }

        AuditLogUtil.logSendMessage(userId, message.getId(), conversationId, true);

        ImMessageVO vo = new ImMessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setContent(plainContent);
        vo.setType(message.getMessageType() != null ? message.getMessageType().toUpperCase() : "TEXT");
        vo.setIsSelf(true);
        vo.setSenderName(sender.getNickname());
        vo.setSenderAvatar(sender.getAvatar());
        vo.setSendTime(message.getCreateTime());
        vo.setStatus(1);

        // 如果是群组消息，发布群组消息事件触发机器人自动回复
        ImConversation conversation = imConversationMapper.selectById(conversationId);
        if (conversation != null && "GROUP".equalsIgnoreCase(conversation.getType())) {
            Long groupId = conversation.getTargetId();
            if (groupId != null && "TEXT".equalsIgnoreCase(message.getMessageType())) {
                try {
                    eventPublisher.publishEvent(
                            new BotMessageListener.GroupMessageEvent(conversationId, groupId, userId, plainContent)
                    );
                } catch (Exception e) {
                    log.error("发布群组消息事件失败: groupId={}", groupId, e);
                }
            }
        }

        // 异步广播消息
        broadcastService.broadcastMessageToConversation(conversationId, message.getId(), sender);

        return vo;
    }

    @Override
    public List<ImMessageVO> getMessages(Long conversationId, Long userId, Long lastId, Integer limit) {
        // 调试日志：记录接收到的 userId
        log.info("getMessages 被调用 - conversationId={}, userId={}, lastId={}, limit={}",
                conversationId, userId, lastId, limit);

        // 权限检查：验证用户是否是会话成员
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null || (member.getIsDeleted() != null && member.getIsDeleted() == 1)) {
            throw new BusinessException("无权查看该会话消息");
        }

        List<ImMessageVO> voList = new ArrayList<>();

        // 参数校验和默认值
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100; // 限制最大返回数量
        }

        // 构建查询条件
        ImMessage query = new ImMessage();
        query.setConversationId(conversationId);

        // 如果指定了lastId，只查询ID小于lastId的消息（用于向上翻页）
        if (lastId != null && lastId > 0) {
            query.setId(lastId);
            // 设置查询条件，需要在Mapper XML中实现 lt 条件
        }

        // 限制查询数量
        query.getParams().put("limit", limit);

        List<ImMessage> messageList = imMessageMapper.selectImMessageList(query);

        // 优化：批量查询发送者信息，避免N+1查询问题
        java.util.Set<Long> senderIds = new java.util.HashSet<>();
        java.util.Set<Long> replyToMessageIds = new java.util.HashSet<>();
        for (ImMessage message : messageList) {
            senderIds.add(message.getSenderId());
            if (message.getReplyToMessageId() != null && message.getReplyToMessageId() > 0) {
                replyToMessageIds.add(message.getReplyToMessageId());
            }
        }

        // 批量查询用户信息
        java.util.Map<Long, ImUser> userMap = new java.util.HashMap<>();
        if (!senderIds.isEmpty()) {
            List<ImUser> users = imUserMapper.selectImUserListByIds(new java.util.ArrayList<>(senderIds));
            for (ImUser user : users) {
                userMap.put(user.getId(), user);
            }
        }

        // 批量查询被回复的消息（用于获取其发送者信息）
        java.util.Set<Long> replyToSenderIds = new java.util.HashSet<>();
        java.util.Map<Long, ImMessage> replyToMessageMap = new java.util.HashMap<>();
        for (Long replyToId : replyToMessageIds) {
            ImMessage replyToMsg = imMessageMapper.selectImMessageById(replyToId);
            if (replyToMsg != null) {
                replyToMessageMap.put(replyToId, replyToMsg);
                if (replyToMsg.getSenderId() != null) {
                    replyToSenderIds.add(replyToMsg.getSenderId());
                }
            }
        }

        // 批量查询被回复消息的发送者信息
        if (!replyToSenderIds.isEmpty()) {
            List<ImUser> replyUsers = imUserMapper.selectImUserListByIds(new java.util.ArrayList<>(replyToSenderIds));
            for (ImUser user : replyUsers) {
                userMap.put(user.getId(), user);
            }
        }

        for (ImMessage message : messageList) {
            ImMessageVO vo = new ImMessageVO();
            BeanUtils.copyProperties(message, vo);

            String decryptedContent = encryptionUtil.decryptMessage(message.getContent());
            vo.setContent(decryptedContent);
            vo.setType(message.getMessageType() != null ? message.getMessageType().toUpperCase() : "TEXT");

            // 从Map中获取发送者信息（避免重复查询）
            ImUser sender = userMap.get(message.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatar());
            }

            // 设置 isSelf 并记录调试日志
            boolean isSelf = message.getSenderId().equals(userId);
            vo.setIsSelf(isSelf);
            log.debug("消息 isSelf 判断 - messageId={}, senderId={}, userId={}, isSelf={}, senderName={}",
                    message.getId(), message.getSenderId(), userId, isSelf, vo.getSenderName());

            // 处理引用消息（回复）
            if (message.getReplyToMessageId() != null && message.getReplyToMessageId() > 0) {
                vo.setReplyToMessageId(message.getReplyToMessageId());
                ImMessageVO.QuotedMessageVO quotedMessage = buildQuotedMessageFromMap(
                        message.getReplyToMessageId(), replyToMessageMap, userMap);
                vo.setQuotedMessage(quotedMessage);
            }

            voList.add(vo);
        }

        // 重要：SQL返回的是按ID降序（最新的在前），需要反转以便前端按时间顺序显示
        java.util.Collections.reverse(voList);

        return voList;
    }

    /**
     * 构建引用消息VO（从预加载的Map中获取数据）
     * 优化版本，避免N+1查询问题
     *
     * @param messageId         被回复的消息ID
     * @param replyToMessageMap 预加载的消息Map
     * @param userMap           预加载的用户Map
     * @return 引用消息VO
     */
    private ImMessageVO.QuotedMessageVO buildQuotedMessageFromMap(
            Long messageId,
            java.util.Map<Long, ImMessage> replyToMessageMap,
            java.util.Map<Long, ImUser> userMap) {

        ImMessage originalMessage = replyToMessageMap.get(messageId);
        if (originalMessage == null) {
            return null;
        }

        ImMessageVO.QuotedMessageVO quotedMessage = new ImMessageVO.QuotedMessageVO();
        quotedMessage.setId(originalMessage.getId());
        quotedMessage.setType(originalMessage.getMessageType() != null ? originalMessage.getMessageType().toUpperCase() : "TEXT");
        quotedMessage.setSendTime(originalMessage.getCreateTime());

        // 从预加载的Map中获取发送者信息
        ImUser sender = userMap.get(originalMessage.getSenderId());
        if (sender != null) {
            quotedMessage.setSenderName(sender.getNickname());
        } else {
            quotedMessage.setSenderName("未知用户");
        }

        // 解密并截取消息内容
        String content = encryptionUtil.decryptMessage(originalMessage.getContent());

        // 根据消息类型处理内容
        String messageType = originalMessage.getMessageType();
        if ("IMAGE".equalsIgnoreCase(messageType) || "FILE".equalsIgnoreCase(messageType)
                || "VIDEO".equalsIgnoreCase(messageType) || "VOICE".equalsIgnoreCase(messageType)) {
            quotedMessage.setIsFile(true);
            // 文件类型消息显示文件名或类型标识
            if ("IMAGE".equalsIgnoreCase(messageType)) {
                quotedMessage.setContent("[图片]");
            } else if ("VIDEO".equalsIgnoreCase(messageType)) {
                quotedMessage.setContent("[视频]");
            } else if ("VOICE".equalsIgnoreCase(messageType)) {
                quotedMessage.setContent("[语音]");
            } else {
                quotedMessage.setContent("[文件]");
            }
        } else {
            // 文本消息截取前50个字符
            quotedMessage.setIsFile(false);
            if (content != null && content.length() > 50) {
                quotedMessage.setContent(content.substring(0, 50) + "...");
            } else {
                quotedMessage.setContent(content);
            }
        }

        return quotedMessage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long messageId, Long userId) {
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("无权撤回该消息");
        }

        if (message.getIsRevoked() == 1) {
            throw new BusinessException("消息已撤回");
        }

        // 从系统配置获取撤回时间限制
        Integer timeLimit = systemConfigService.getMessageRecallTimeLimit();
        LocalDateTime now = LocalDateTime.now();

        if (timeLimit > 0 && message.getCreateTime().plusMinutes(timeLimit).isBefore(now)) {
            throw new BusinessException("消息发送超过" + timeLimit + "分钟，无法撤回");
        }

        message.setIsRevoked(1);
        message.setRevokedTime(now);
        imMessageMapper.updateImMessage(message);

        // 记录审计日志
        AuditLogUtil.logRecallMessage(userId, messageId, true);

        // 广播撤回通知给会话中的其他用户
        broadcastService.broadcastRecallNotification(message.getConversationId(), messageId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editMessage(Long messageId, String newContent, Long userId) {
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("无权编辑该消息");
        }

        if (!"TEXT".equals(message.getMessageType())) {
            throw new BusinessException("只能编辑文本消息");
        }

        if (message.getIsRevoked() == 1) {
            throw new BusinessException("已撤回的消息不能编辑");
        }

        LocalDateTime now = LocalDateTime.now();
        // 限制：消息发送超过15分钟不能编辑
        if (message.getCreateTime().plusMinutes(15).isBefore(now)) {
            throw new BusinessException("消息发送超过15分钟，无法编辑");
        }

        // 保存编辑历史
        String oldContent = message.getIsEdited() == 1 ? message.getEditedContent() : message.getContent();
        ImMessageEditHistory history = new ImMessageEditHistory();
        history.setMessageId(messageId);
        history.setOldContent(oldContent);
        history.setNewContent(newContent);
        history.setEditorId(userId);
        history.setEditTime(now);
        history.setCreateTime(now);
        editHistoryMapper.insert(history);

        // 更新消息
        message.setEditedContent(newContent);
        message.setIsEdited(1);
        message.setEditCount(message.getEditCount() == null ? 1 : message.getEditCount() + 1);
        message.setEditTime(now);
        imMessageMapper.updateImMessage(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId, Long userId) {
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 软删除，设置is_deleted = 1
        // 注意：这里只允许删除自己的消息，或者管理员可以删除任何消息
        // 如果是双向删除（删除后对方也不可见），需要权限校验
        // 如果是单向删除（仅自己不可见），需要另外的表来记录删除状态
        // 这里简化实现，假设是双向删除，且只能删除自己的消息
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("无权删除该消息");
        }

        message.setIsDeleted(1);
        message.setDeletedTime(LocalDateTime.now());
        imMessageMapper.updateImMessage(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long conversationId, Long userId, List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        int readCount = 0;
        Long maxMessageId = null;

        for (Long messageId : messageIds) {
            ImMessage message = imMessageMapper.selectImMessageById(messageId);
            if (message != null && !message.getSenderId().equals(userId)) {
                // 不再更新消息状态（status是非数据库字段）
                // 已读状态通过im_message_read表记录
                readCount++;
                if (maxMessageId == null || messageId > maxMessageId) {
                    maxMessageId = messageId;
                }
            }
        }

        // 减少未读消息数
        if (readCount > 0) {
            imConversationMemberMapper.decrementUnreadCount(conversationId, userId, readCount);
            // 更新最后已读消息ID
            if (maxMessageId != null) {
                imConversationMemberMapper.updateLastReadMessageId(conversationId, userId, maxMessageId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long forwardMessage(Long messageId, Long toConversationId, Long toUserId, String content, Long userId) {
        ImMessage originalMessage = imMessageMapper.selectImMessageById(messageId);
        if (originalMessage == null) {
            throw new BusinessException("消息不存在");
        }

        // 解密原消息内容
        String decryptedContent = encryptionUtil.decryptMessage(originalMessage.getContent());

        ImMessage forwardMessage = new ImMessage();
        forwardMessage
                .setConversationId(toConversationId != null ? toConversationId : originalMessage.getConversationId());
        forwardMessage.setSenderId(userId);
        forwardMessage.setReceiverId(toUserId != null ? toUserId : originalMessage.getReceiverId());
        forwardMessage.setMessageType(originalMessage.getMessageType()); // 转发时保留原消息类型

        // 构建转发消息内容，包含原消息信息
        String forwardContent;
        if (content != null && !content.isEmpty()) {
            forwardContent = content + "\n\n[转发消息]" + decryptedContent;
        } else {
            forwardContent = "[转发消息]\n" + decryptedContent;
        }
        forwardMessage.setContent(encryptionUtil.encryptMessage(forwardContent));

        forwardMessage.setForwardFromMessageId(messageId); // 设置转发来源消息ID
        forwardMessage.setIsRevoked(0);
        forwardMessage.setCreateTime(LocalDateTime.now());

        imMessageMapper.insertImMessage(forwardMessage);
        return forwardMessage.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.List<Long> batchForwardMessages(java.util.List<Long> messageIds, Long toConversationId,
                                                        String forwardType, String content, Long userId) {
        if ("combine".equals(forwardType)) {
            // 合并转发：创建一条聊天记录类型的消息
            return combineForwardMessages(messageIds, toConversationId, content, userId);
        } else {
            // 逐条转发（默认）
            return batchForwardMessagesIndividually(messageIds, toConversationId, content, userId);
        }
    }

    /**
     * 逐条转发消息
     */
    private java.util.List<Long> batchForwardMessagesIndividually(java.util.List<Long> messageIds,
                                                                  Long toConversationId,
                                                                  String content, Long userId) {
        java.util.List<Long> newMessageIds = new java.util.ArrayList<>();
        for (Long messageId : messageIds) {
            Long newId = forwardMessage(messageId, toConversationId, null, content, userId);
            newMessageIds.add(newId);
        }
        return newMessageIds;
    }

    /**
     * 合并转发消息
     * 将多条消息合并为一条聊天记录类型的消息
     */
    private java.util.List<Long> combineForwardMessages(java.util.List<Long> messageIds,
                                                         Long toConversationId,
                                                         String content, Long userId) {
        // 获取所有原始消息
        java.util.List<ImMessage> originalMessages = imMessageMapper.selectBatchIds(messageIds);
        if (originalMessages == null || originalMessages.isEmpty()) {
            throw new BusinessException("消息不存在");
        }

        // 构建合并转发内容
        StringBuilder combineContent = new StringBuilder();
        if (content != null && !content.isEmpty()) {
            combineContent.append(content).append("\n\n");
        }
        combineContent.append("------ 聊天记录 ------\n");

        // 按时间排序构建聊天记录
        originalMessages.stream()
                .sorted(java.util.Comparator.comparing(ImMessage::getCreateTime))
                .forEach(msg -> {
                    String decryptedContent = encryptionUtil.decryptMessage(msg.getContent());
                    String senderName = getSenderName(msg.getSenderId());
                    String timeStr = msg.getCreateTime().toLocalTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

                    combineContent.append(String.format("[%s %s] %s\n",
                            senderName, timeStr, decryptedContent));
                });

        // 创建合并转发消息
        ImMessage combineMessage = new ImMessage();
        combineMessage.setConversationId(toConversationId);
        combineMessage.setSenderId(userId);
        combineMessage.setMessageType("COMBINE"); // 新增消息类型用于合并转发
        combineMessage.setContent(encryptionUtil.encryptMessage(combineContent.toString()));
        combineMessage.setIsRevoked(0);
        combineMessage.setCreateTime(java.time.LocalDateTime.now());

        imMessageMapper.insertImMessage(combineMessage);
        return java.util.Collections.singletonList(combineMessage.getId());
    }

    /**
     * 获取发送者名称（简单实现，可从缓存或数据库获取）
     */
    private String getSenderName(Long senderId) {
        // 简单实现，实际应从用户服务获取
        com.ruoyi.im.domain.ImUser user = imUserMapper.selectImUserById(senderId);
        return user != null ? user.getNickname() : "用户" + senderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long replyMessage(Long messageId, String content, Long userId) {
        ImMessage originalMessage = imMessageMapper.selectImMessageById(messageId);
        if (originalMessage == null) {
            throw new BusinessException("消息不存在");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("回复内容不能为空");
        }

        ImMessage replyMessage = new ImMessage();
        replyMessage.setConversationId(originalMessage.getConversationId());
        replyMessage.setReceiverId(originalMessage.getSenderId());
        replyMessage.setSenderId(userId);
        replyMessage.setReplyToMessageId(messageId); // 设置回复消息ID
        replyMessage.setMessageType("TEXT"); // 回复消息类型为TEXT
        replyMessage.setContent(encryptionUtil.encryptMessage(content));
        replyMessage.setIsRevoked(0);
        replyMessage.setCreateTime(LocalDateTime.now());

        imMessageMapper.insertImMessage(replyMessage);
        return replyMessage.getId();
    }

    @Override
    public ImMessageSearchResultVO searchMessages(Long conversationId, String keyword, String messageType,
            Long senderId, LocalDateTime startTime, LocalDateTime endTime,
            Integer pageNum, Integer pageSize, Boolean includeRevoked,
            Boolean exactMatch, Long userId) {
        // 参数校验和默认值设置
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        if (includeRevoked == null) {
            includeRevoked = false;
        }
        if (exactMatch == null) {
            exactMatch = false;
        }

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 统计总数
        int total = imMessageMapper.countSearchResults(conversationId, keyword, messageType,
                senderId, startTime, endTime, includeRevoked, exactMatch, useFullTextSearch);

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / pageSize);

        // 查询结果
        List<ImMessage> messageList = imMessageMapper.searchMessages(conversationId, keyword, messageType,
                senderId, startTime, endTime, includeRevoked, exactMatch, useFullTextSearch, offset, pageSize);

        // 优化：批量查询发送者信息，避免N+1查询问题
        java.util.Map<Long, ImUser> userMap = new java.util.HashMap<>();
        if (!messageList.isEmpty()) {
            java.util.Set<Long> senderIds = new java.util.HashSet<>();
            for (ImMessage message : messageList) {
                senderIds.add(message.getSenderId());
            }
            List<ImUser> users = imUserMapper.selectImUserListByIds(new java.util.ArrayList<>(senderIds));
            for (ImUser user : users) {
                userMap.put(user.getId(), user);
            }
        }

        // 构建结果VO
        ImMessageSearchResultVO resultVO = new ImMessageSearchResultVO();
        resultVO.setTotal(total);
        resultVO.setPageNum(pageNum);
        resultVO.setPageSize(pageSize);
        resultVO.setTotalPages(totalPages);

        List<ImMessageSearchResultVO.SearchResultItem> items = new ArrayList<>();

        for (ImMessage message : messageList) {
            ImMessageSearchResultVO.SearchResultItem item = new ImMessageSearchResultVO.SearchResultItem();
            item.setId(message.getId());
            item.setConversationId(message.getConversationId());
            item.setSenderId(message.getSenderId());
            item.setType(message.getMessageType() != null ? message.getMessageType().toUpperCase() : "TEXT");
            item.setSendTime(message.getCreateTime());

            // 解密消息内容
            String decryptedContent = encryptionUtil.decryptMessage(message.getContent());
            item.setContent(decryptedContent);

            // 生成高亮片段
            if (keyword != null && !keyword.isEmpty()) {
                item.setHighlightSnippet(generateHighlightSnippet(decryptedContent, keyword, 100));
            }

            // 从预加载的Map中获取发送者信息（避免重复查询）
            ImUser sender = userMap.get(message.getSenderId());
            if (sender != null) {
                item.setSenderName(sender.getNickname());
                item.setSenderAvatar(sender.getAvatar());
            } else {
                item.setSenderName("未知用户");
            }

            item.setIsSelf(message.getSenderId().equals(userId));
            items.add(item);
        }

        resultVO.setItems(items);
        return resultVO;
    }

    /**
     * 生成高亮片段
     * 截取包含关键词的上下文
     *
     * @param content       原始内容
     * @param keyword       关键词
     * @param snippetLength 片段长度
     * @return 高亮片段
     */
    private String generateHighlightSnippet(String content, String keyword, int snippetLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // 查找关键词位置
        int keywordIndex = content.toLowerCase().indexOf(keyword.toLowerCase());
        if (keywordIndex == -1) {
            // 未找到关键词，返回前N个字符
            if (content.length() > snippetLength) {
                return content.substring(0, snippetLength) + "...";
            }
            return content;
        }

        // 计算片段起始位置
        int start = Math.max(0, keywordIndex - snippetLength / 2);
        int end = Math.min(content.length(), start + snippetLength);

        // 调整起始位置以确保片段包含完整关键词
        if (end - start < snippetLength && start > 0) {
            start = Math.max(0, end - snippetLength);
        }

        String snippet = content.substring(start, end);

        // 添加省略号
        if (start > 0) {
            snippet = "..." + snippet;
        }
        if (end < content.length()) {
            snippet = snippet + "...";
        }

        // 高亮关键词（使用简单的标记，前端可以进一步处理）
        // 注意：这里只是简单的标记，实际高亮由前端CSS实现
        return snippet;
    }

    @Override
    public int getTodayMessageCount(Long userId) {
        // 获取今日开始时间
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        // 获取今日结束时间
        LocalDateTime todayEnd = todayStart.plusDays(1);

        // 统计用户今日发送的消息数量
        return imMessageMapper.countBySenderIdAndTimeRange(userId, todayStart, todayEnd);
    }

    @Override
    public ImMessageVO getMessageById(Long messageId) {
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message == null) {
            return null;
        }
        return convertToVO(message);
    }

    /**
     * 将消息实体转换为VO
     *
     * @param message 消息实体
     * @return 消息VO
     */
    private ImMessageVO convertToVO(ImMessage message) {
        ImMessageVO vo = new ImMessageVO();
        BeanUtils.copyProperties(message, vo);
        
        // 解密消息内容
        if (message.getContent() != null) {
            try {
                vo.setContent(encryptionUtil.decryptMessage(message.getContent()));
            } catch (Exception e) {
                log.warn("消息解密失败: messageId={}, error={}", message.getId(), e.getMessage());
                vo.setContent("[加密消息]");
            }
        }
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteMessage(Long messageId) {
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("MESSAGE_NOT_EXIST", "消息不存在");
        }

        imMessageMapper.deleteImMessageById(messageId);

        // 记录删除日志
        AuditLogUtil.logSuccess(SecurityUtils.getLoginUserId(), "DELETE_MESSAGE", "MESSAGE", messageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Integer> adminBatchDeleteMessages(List<Long> messageIds) {
        int successCount = 0;
        int failCount = 0;

        for (Long messageId : messageIds) {
            try {
                ImMessage message = imMessageMapper.selectImMessageById(messageId);
                if (message != null) {
                    imMessageMapper.deleteImMessageById(messageId);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("删除消息失败，messageId={}", messageId, e);
                failCount++;
            }
        }

        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        return result;
    }

    @Override
    public java.util.Map<String, Object> getMessageStats(LocalDateTime startTime, LocalDateTime endTime) {
        // 默认统计最近7天
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (startTime == null) {
            startTime = endTime.minusDays(7);
        }

        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // 统计各类型消息数量
        int totalMessages = imMessageMapper.countSearchResults(null, null, null, null, startTime, endTime, false, false, false);
        stats.put("totalMessages", totalMessages);

        int textMessages = imMessageMapper.countSearchResults(null, null, "TEXT", null, startTime, endTime, false, false, false);
        stats.put("textMessages", textMessages);

        int imageMessages = imMessageMapper.countSearchResults(null, null, "IMAGE", null, startTime, endTime, false, false, false);
        stats.put("imageMessages", imageMessages);

        int fileMessages = imMessageMapper.countSearchResults(null, null, "FILE", null, startTime, endTime, false, false, false);
        stats.put("fileMessages", fileMessages);

        int revokedMessages = imMessageMapper.countSearchResults(null, null, null, null, startTime, endTime, true, false, false);
        stats.put("revokedMessages", revokedMessages);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearConversationMessages(Long conversationId, Long userId) {
        // 验证用户是否有权限访问该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("NO_PERMISSION", "无权限访问该会话");
        }

        // 删除该会话的所有消息
        imMessageMapper.deleteByConversationId(conversationId);

        log.info("用户清空会话聊天记录: conversationId={}, userId={}", conversationId, userId);
    }
}
