package com.ruoyi.im.service.impl;

import cn.hutool.http.HtmlUtil;
import com.ruoyi.im.constant.MessageStatusConstants;
import com.ruoyi.im.constants.StatusConstants;
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
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImSystemConfigService;
import com.ruoyi.im.util.AuditLogUtil;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.util.MessageEncryptionUtil;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ImMessageServiceImpl implements ImMessageService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);

    @Value("${im.search.fulltext:false}")
    private boolean useFullTextSearch;

    private final ImMessageMapper imMessageMapper;
    private final ImUserService imUserService;
    private final ImConversationMapper imConversationMapper;
    private final ImConversationMemberMapper imConversationMemberMapper;
    private final ImConversationService imConversationService;
    private final MessageEncryptionUtil encryptionUtil;
    private final ImMessageMentionService messageMentionService;
    private final ImMessageEditHistoryMapper editHistoryMapper;
    private final com.ruoyi.im.util.ImRedisUtil redisUtil;
    private final com.ruoyi.im.service.ImWebSocketBroadcastService broadcastService;
    private final com.ruoyi.im.service.ImSystemConfigService systemConfigService;

    /**
     * 构造器注入依赖 - 简化后
     */
    public ImMessageServiceImpl(ImMessageMapper imMessageMapper,
            ImUserService imUserService,
            ImConversationMapper imConversationMapper,
            ImConversationMemberMapper imConversationMemberMapper,
            ImConversationService imConversationService,
            MessageEncryptionUtil encryptionUtil,
            ImMessageMentionService messageMentionService,
            ImMessageEditHistoryMapper editHistoryMapper,
            com.ruoyi.im.util.ImRedisUtil redisUtil,
            com.ruoyi.im.service.ImWebSocketBroadcastService broadcastService,
            ImSystemConfigService systemConfigService) {
        this.imMessageMapper = imMessageMapper;
        this.imUserService = imUserService;
        this.imConversationMapper = imConversationMapper;
        this.imConversationMemberMapper = imConversationMemberMapper;
        this.imConversationService = imConversationService;
        this.encryptionUtil = encryptionUtil;
        this.messageMentionService = messageMentionService;
        this.editHistoryMapper = editHistoryMapper;
        this.redisUtil = redisUtil;
        this.broadcastService = broadcastService;
        this.systemConfigService = systemConfigService;
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
                    BeanConvertUtil.copyProperties(existingMessage, vo);
                    vo.setContent(encryptionUtil.decryptMessage(existingMessage.getContent())); // 解密
                    vo.setIsSelf(existingMessage.getSenderId().equals(userId));
                    ImUser sender = imUserService.getUserEntityById(existingMessage.getSenderId());
                    if (sender != null) {
                        vo.setSenderName(sender.getNickname());
                        vo.setSenderAvatar(sender.getAvatar());
                    }
                    return vo;
                }
            }
        }

        ImUser sender = imUserService.getUserEntityById(userId);
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
            ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                    userId);
            if (member == null || (member.getIsDeleted() != null && member.getIsDeleted() == 1)) {
                throw new BusinessException("您不是该会话成员，无法发送消息");
            }
        }

        // 私有化部署：单机环境无需分布式锁，直接发送
        return doSendMessage(request, userId, conversationId, sender, clientMsgId);
    }

    /**
     * 实际执行发送消息的逻辑
     * 支持消息发送状态追踪和失败重试
     */
    private ImMessageVO doSendMessage(ImMessageSendRequest request, Long userId, Long conversationId,
            ImUser sender, String clientMsgId) {
        ImMessage message = null;
        try {
            // 1. 创建并保存消息
            message = createAndSaveMessage(request, userId, conversationId, clientMsgId);

            // 2. 记录客户端消息ID（用于幂等性）
            if (clientMsgId != null && !clientMsgId.isEmpty()) {
                redisUtil.recordClientMsgId(clientMsgId, message.getId());
            }

            // 3. 更新会话最后消息信息
            updateConversationLastMessage(conversationId, message);

            // 4. 处理未读计数
            updateUnreadCount(conversationId, userId);

            // 5. 处理@提及功能
            processMentions(request, conversationId, message.getId(), userId);

            // 6. 广播消息到会话成员
            broadcastService.broadcastMessageToConversation(conversationId, message.getId(), sender);

            // 9. 更新消息状态为已送达
            updateMessageSendStatus(message.getId(), "DELIVERED", null);

            // 10. 构建返回VO
            return buildMessageVO(message, sender);

        } catch (Exception e) {
            // 记录发送失败
            log.error("消息发送失败: clientMsgId={}, error={}", clientMsgId, e.getMessage(), e);

            // 如果消息已创建，更新状态为失败
            if (message != null && message.getId() != null) {
                updateMessageSendStatus(message.getId(), "FAILED", e.getMessage());
            }

            // 重新抛出异常，让上层处理
            throw new BusinessException("消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 创建并保存消息实体
     */
    private ImMessage createAndSaveMessage(ImMessageSendRequest request, Long userId, Long conversationId,
            String clientMsgId) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setReceiverId(request.getReceiverId());
        message.setMessageType(request.getType());
        message.setReplyToMessageId(request.getReplyToMessageId());

        // XSS防护 + 加密
        String plainContent = request.getContent();
        if (MessageStatusConstants.MESSAGE_TYPE_TEXT.equalsIgnoreCase(request.getType())) {
            plainContent = HtmlUtil.escape(plainContent);
        }
        message.setContent(encryptionUtil.encryptMessage(plainContent));

        message.setIsRevoked(0);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        message.setClientMsgId(clientMsgId);
        message.setSendStatus("SENDING");
        message.setSendRetryCount(0);
        message.setDeliveredTime(LocalDateTime.now());

        imMessageMapper.insertImMessage(message);
        return message;
    }

    /**
     * 更新会话的最后消息信息
     */
    private void updateConversationLastMessage(Long conversationId, ImMessage message) {
        imConversationMapper.updateLastMessage(conversationId, message.getId(), message.getCreateTime());
    }

    /**
     * 更新会话成员的未读计数 - 优化版：使用 Redis 缓存 + 定时同步
     */
    private void updateUnreadCount(Long conversationId, Long senderId) {
        try {
            // 获取会话所有成员
            List<ImConversationMember> members = imConversationMemberMapper.selectByConversationId(conversationId);
            if (members == null || members.isEmpty()) {
                return;
            }

            // 1. 立即更新 Redis 缓存（快速响应）
            for (ImConversationMember member : members) {
                if (!member.getUserId().equals(senderId)) {
                    // 增加未读数到 Redis
                    redisUtil.incrementUnreadCount(member.getUserId(), conversationId);
                }
            }

            // 2. 异步更新数据库（避免阻塞消息发送）
            CompletableFuture.runAsync(() -> {
                try {
                    imConversationMemberMapper.incrementUnreadCountExcludeSender(conversationId, senderId);
                    // 清除会话列表缓存
                    redisUtil.delete("conversation:list:" + senderId);
                } catch (Exception e) {
                    log.error("异步更新未读数到数据库失败: conversationId={}", conversationId, e);
                }
            });

        } catch (Exception e) {
            log.error("更新未读数失败: conversationId={}", conversationId, e);
        }
    }

    /**
     * 处理消息@提及功能
     */
    private void processMentions(ImMessageSendRequest request, Long conversationId, Long messageId, Long senderId) {
        ImMentionInfo mentionInfo = request.getMentionInfo();
        if (mentionInfo == null && request.getContent() != null) {
            mentionInfo = messageMentionService.parseMentions(request.getContent());
        }

        if (mentionInfo != null
                && (mentionInfo.getUserIds() != null || Boolean.TRUE.equals(mentionInfo.getMentionAll()))) {
            mentionInfo.setConversationId(conversationId);
            messageMentionService.createMentions(messageId, mentionInfo, senderId);
        }
    }

    /**
     * 更新消息发送状态
     *
     * @param messageId  消息ID
     * @param sendStatus 发送状态（SENDING/DELIVERED/FAILED）
     * @param errorMsg   错误信息（失败时）
     */
    private void updateMessageSendStatus(Long messageId, String sendStatus, String errorMsg) {
        try {
            ImMessage updateMsg = new ImMessage();
            updateMsg.setId(messageId);
            updateMsg.setSendStatus(sendStatus);
            updateMsg.setUpdateTime(LocalDateTime.now());

            if ("DELIVERED".equals(sendStatus)) {
                updateMsg.setDeliveredTime(LocalDateTime.now());
            } else if ("FAILED".equals(sendStatus)) {
                updateMsg.setSendErrorMsg(errorMsg);
                // 增加重试计数
                ImMessage existingMsg = imMessageMapper.selectImMessageById(messageId);
                if (existingMsg != null) {
                    int currentRetryCount = existingMsg.getSendRetryCount() != null ? existingMsg.getSendRetryCount()
                            : 0;
                    updateMsg.setSendRetryCount(currentRetryCount + 1);
                }
            }

            imMessageMapper.updateImMessage(updateMsg);
        } catch (Exception e) {
            log.error("更新消息发送状态失败: messageId={}, status={}", messageId, sendStatus, e);
        }
    }

    /**
     * 构建消息返回VO
     */
    private ImMessageVO buildMessageVO(ImMessage message, ImUser sender) {
        String plainContent = encryptionUtil.decryptMessage(message.getContent());

        ImMessageVO vo = new ImMessageVO();
        BeanConvertUtil.copyProperties(message, vo);
        vo.setContent(plainContent);
        vo.setType(message.getMessageType() != null ? message.getMessageType().toUpperCase()
                : MessageStatusConstants.MESSAGE_TYPE_TEXT);
        vo.setIsSelf(true);
        vo.setSenderName(sender.getNickname());
        vo.setSenderAvatar(sender.getAvatar());
        vo.setSendTime(message.getCreateTime());
        // 使用 Entity 中的 sendStatus，不要硬编码
        if (vo.getSendStatus() == null) {
            vo.setSendStatus(message.getSendStatus() != null ? message.getSendStatus() : "SENDING");
        }
        return vo;
    }

    @Override
    public List<ImMessageVO> getMessages(Long conversationId, Long userId, Long lastId, Integer limit) {
        log.info("getMessages 被调用 - conversationId={}, userId={}, lastId={}, limit={}",
                conversationId, userId, lastId, limit);

        // 1. 权限检查
        validateConversationAccess(conversationId, userId);

        // 2. 参数校验和默认值
        limit = validateAndNormalizeLimit(limit);

        // 3. 查询消息列表
        List<ImMessage> messageList = queryMessages(conversationId, lastId, limit);
        if (messageList.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. 批量加载相关数据
        MessageBatchData batchData = loadMessageBatchData(messageList);

        // 5. 构建VO列表
        List<ImMessageVO> voList = buildMessageVOList(messageList, batchData, userId);

        // 6. 反转列表（SQL返回降序，前端需要时间顺序）
        java.util.Collections.reverse(voList);

        return voList;
    }

    /**
     * 验证用户是否有权访问会话消息
     */
    private void validateConversationAccess(Long conversationId, Long userId) {
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (member == null || (member.getIsDeleted() != null && member.getIsDeleted() == 1)) {
            throw new BusinessException("无权查看该会话消息");
        }
    }

    /**
     * 校验并规范化分页参数
     */
    private int validateAndNormalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 20;
        }
        return Math.min(limit, 100);
    }

    /**
     * 查询消息列表
     */
    private List<ImMessage> queryMessages(Long conversationId, Long lastId, int limit) {
        ImMessage query = new ImMessage();
        query.setConversationId(conversationId);
        if (lastId != null && lastId > 0) {
            query.setId(lastId);
        }
        query.getParams().put("limit", limit);
        return imMessageMapper.selectImMessageList(query);
    }

    /**
     * 批量加载消息相关数据（避免N+1查询）
     */
    private MessageBatchData loadMessageBatchData(List<ImMessage> messageList) {
        java.util.Set<Long> senderIds = new java.util.HashSet<>();
        java.util.Set<Long> replyToMessageIds = new java.util.HashSet<>();

        // 收集ID
        for (ImMessage message : messageList) {
            senderIds.add(message.getSenderId());
            if (message.getReplyToMessageId() != null && message.getReplyToMessageId() > 0) {
                replyToMessageIds.add(message.getReplyToMessageId());
            }
        }

        // 批量查询用户信息
        java.util.Map<Long, ImUser> userMap = new java.util.HashMap<>();
        if (!senderIds.isEmpty()) {
            List<ImUser> users = imUserService.getUserEntitiesByIds(new java.util.ArrayList<>(senderIds));
            for (ImUser user : users) {
                userMap.put(user.getId(), user);
            }
        }

        // 批量查询被回复的消息
        java.util.Map<Long, ImMessage> replyToMessageMap = new java.util.HashMap<>();
        for (Long replyToId : replyToMessageIds) {
            ImMessage replyToMsg = imMessageMapper.selectImMessageById(replyToId);
            if (replyToMsg != null) {
                replyToMessageMap.put(replyToId, replyToMsg);
                // 补充被回复消息的发送者信息
                if (replyToMsg.getSenderId() != null && !userMap.containsKey(replyToMsg.getSenderId())) {
                    ImUser replyUser = imUserService.getUserEntityById(replyToMsg.getSenderId());
                    if (replyUser != null) {
                        userMap.put(replyUser.getId(), replyUser);
                    }
                }
            }
        }

        return new MessageBatchData(userMap, replyToMessageMap);
    }

    /**
     * 构建消息VO列表
     */
    private List<ImMessageVO> buildMessageVOList(List<ImMessage> messageList,
            MessageBatchData batchData, Long userId) {
        List<ImMessageVO> voList = new ArrayList<>();

        for (ImMessage message : messageList) {
            ImMessageVO vo = new ImMessageVO();
            BeanConvertUtil.copyProperties(message, vo);

            // 解密内容
            String decryptedContent = encryptionUtil.decryptMessage(message.getContent());
            vo.setContent(decryptedContent);
            vo.setType(message.getMessageType() != null ? message.getMessageType().toUpperCase()
                    : MessageStatusConstants.MESSAGE_TYPE_TEXT);

            // 设置发送者信息
            ImUser sender = batchData.userMap.get(message.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatar());
            }

            // 设置是否为自己发送
            boolean isSelf = message.getSenderId().equals(userId);
            vo.setIsSelf(isSelf);
            log.debug("消息 isSelf 判断 - messageId={}, senderId={}, userId={}, isSelf={}, senderName={}",
                    message.getId(), message.getSenderId(), userId, isSelf, vo.getSenderName());

            // 处理引用消息（回复）
            if (message.getReplyToMessageId() != null && message.getReplyToMessageId() > 0) {
                vo.setReplyToMessageId(message.getReplyToMessageId());
                ImMessageVO.QuotedMessageVO quotedMessage = buildQuotedMessageFromMap(
                        message.getReplyToMessageId(), batchData.replyToMessageMap, batchData.userMap);
                vo.setQuotedMessage(quotedMessage);
                // 同时设置replyTo字段以兼容前端
                vo.setReplyTo(quotedMessage);
            }

            voList.add(vo);
        }

        return voList;
    }

    /**
     * 消息批量数据容器
     */
    private static class MessageBatchData {
        final java.util.Map<Long, ImUser> userMap;
        final java.util.Map<Long, ImMessage> replyToMessageMap;

        MessageBatchData(java.util.Map<Long, ImUser> userMap, java.util.Map<Long, ImMessage> replyToMessageMap) {
            this.userMap = userMap;
            this.replyToMessageMap = replyToMessageMap;
        }
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
            // 被引用的消息不存在（可能被删除），返回占位对象
            ImMessageVO.QuotedMessageVO placeholder = new ImMessageVO.QuotedMessageVO();
            placeholder.setId(messageId);
            placeholder.setSenderName("未知用户");
            placeholder.setContent("[消息已删除]");
            placeholder.setType(MessageStatusConstants.MESSAGE_TYPE_TEXT);
            placeholder.setIsFile(false);
            return placeholder;
        }

        ImMessageVO.QuotedMessageVO quotedMessage = new ImMessageVO.QuotedMessageVO();
        quotedMessage.setId(originalMessage.getId());
        quotedMessage.setType(originalMessage.getMessageType() != null ? originalMessage.getMessageType().toUpperCase()
                : MessageStatusConstants.MESSAGE_TYPE_TEXT);
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
        if (MessageStatusConstants.MESSAGE_TYPE_IMAGE.equalsIgnoreCase(messageType)
                || MessageStatusConstants.MESSAGE_TYPE_FILE.equalsIgnoreCase(messageType)
                || MessageStatusConstants.MESSAGE_TYPE_VIDEO.equalsIgnoreCase(messageType)
                || MessageStatusConstants.MESSAGE_TYPE_VOICE.equalsIgnoreCase(messageType)) {
            quotedMessage.setIsFile(true);
            // 文件类型消息显示文件名或类型标识
            if (MessageStatusConstants.MESSAGE_TYPE_IMAGE.equalsIgnoreCase(messageType)) {
                quotedMessage.setContent("[图片]");
            } else if (MessageStatusConstants.MESSAGE_TYPE_VIDEO.equalsIgnoreCase(messageType)) {
                quotedMessage.setContent("[视频]");
            } else if (MessageStatusConstants.MESSAGE_TYPE_VOICE.equalsIgnoreCase(messageType)) {
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

        if (!MessageStatusConstants.MESSAGE_TYPE_TEXT.equals(message.getMessageType())) {
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
            final int finalReadCount = readCount;
            final Long finalMaxMessageId = maxMessageId;

            // 1. 立即更新 Redis 缓存
            redisUtil.decrementUnreadCount(userId, conversationId, finalReadCount);

            // 2. 异步更新数据库
            CompletableFuture.runAsync(() -> {
                try {
                    imConversationMemberMapper.decrementUnreadCount(conversationId, userId, finalReadCount);
                    // 更新最后已读消息ID
                    if (finalMaxMessageId != null) {
                        imConversationMemberMapper.updateLastReadMessageId(conversationId, userId, finalMaxMessageId);
                    }
                } catch (Exception e) {
                    log.error("异步更新已读状态到数据库失败: conversationId={}, userId={}", conversationId, userId, e);
                }
            });
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
        com.ruoyi.im.domain.ImUser user = imUserService.getUserEntityById(senderId);
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
        replyMessage.setMessageType(MessageStatusConstants.MESSAGE_TYPE_TEXT); // 回复消息类型为TEXT
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
            List<ImUser> users = imUserService.getUserEntitiesByIds(new java.util.ArrayList<>(senderIds));
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
        BeanConvertUtil.copyProperties(message, vo);

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
        int totalMessages = imMessageMapper.countSearchResults(null, null, null, null, startTime, endTime, false, false,
                false);
        stats.put("totalMessages", totalMessages);

        int textMessages = imMessageMapper.countSearchResults(null, null, MessageStatusConstants.MESSAGE_TYPE_TEXT,
                null, startTime, endTime, false, false, false);
        stats.put("textMessages", textMessages);

        int imageMessages = imMessageMapper.countSearchResults(null, null, MessageStatusConstants.MESSAGE_TYPE_IMAGE,
                null, startTime, endTime, false, false, false);
        stats.put("imageMessages", imageMessages);

        int fileMessages = imMessageMapper.countSearchResults(null, null, MessageStatusConstants.MESSAGE_TYPE_FILE,
                null, startTime, endTime, false, false, false);
        stats.put("fileMessages", fileMessages);

        int revokedMessages = imMessageMapper.countSearchResults(null, null, null, null, startTime, endTime, true,
                false, false);
        stats.put("revokedMessages", revokedMessages);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearConversationMessages(Long conversationId, Long userId) {
        // 验证用户是否有权限访问该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (member == null) {
            throw new BusinessException("NO_PERMISSION", "无权限访问该会话");
        }

        // 删除该会话的所有消息
        imMessageMapper.deleteByConversationId(conversationId);

        log.info("用户清空会话聊天记录: conversationId={}, userId={}", conversationId, userId);
    }

    @Override
    public List<ImMessageVO> getMessagesByCategory(Long conversationId, String category, Long userId, Long lastId,
            Integer limit) {
        // 验证用户是否有权限访问该会话
        ImConversationMember member = imConversationMemberMapper.selectByConversationIdAndUserId(conversationId,
                userId);
        if (member == null) {
            throw new BusinessException("NO_PERMISSION", "无权限访问该会话");
        }

        // 根据category确定消息类型筛选
        String messageTypeFilter = null;
        if ("image".equals(category)) {
            messageTypeFilter = MessageStatusConstants.MESSAGE_TYPE_IMAGE;
        } else if ("file".equals(category)) {
            messageTypeFilter = MessageStatusConstants.MESSAGE_TYPE_FILE;
        } else if ("voice".equals(category)) {
            messageTypeFilter = MessageStatusConstants.MESSAGE_TYPE_VOICE;
        } else if ("video".equals(category)) {
            messageTypeFilter = MessageStatusConstants.MESSAGE_TYPE_VIDEO;
        }
        // "all" 和 "link" 不筛选类型，link 需要特殊处理内容

        // 查询消息列表
        List<ImMessage> messageList = imMessageMapper.selectMessagesByCategory(
                conversationId, messageTypeFilter, lastId, limit);

        // 转换为VO
        List<ImMessageVO> resultList = new ArrayList<>();
        for (ImMessage message : messageList) {
            // 如果是 link 类型，需要检查消息内容是否包含链接
            if ("link".equals(category)) {
                String content = encryptionUtil.decryptMessage(message.getContent());
                if (!containsLink(content)) {
                    continue;
                }
            }
            resultList.add(convertToVO(message));
        }

        return resultList;
    }

    /**
     * 检查消息内容是否包含链接
     *
     * @param content 消息内容
     * @return 是否包含链接
     */
    private boolean containsLink(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        // 简单的链接检测正则表达式
        String urlPattern = "https?://[\\w\\-._~:/?#[\\]@!$&'()*+,;=]+";
        return content.matches(".*" + urlPattern + ".*");
    }
}
