package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImConversation;
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
import com.ruoyi.im.util.AuditLogUtil;
import com.ruoyi.im.util.MessageEncryptionUtil;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImMessageServiceImpl implements ImMessageService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);

    @Autowired
    private ImMessageMapper imMessageMapper;

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private ImConversationMapper imConversationMapper;

    @Autowired
    private ImConversationMemberMapper imConversationMemberMapper;

    @Autowired
    private ImConversationService imConversationService;

    @Autowired
    private MessageEncryptionUtil encryptionUtil;

    @Autowired
    private ImMessageMentionService messageMentionService;

    @Autowired
    private ImMessageEditHistoryMapper editHistoryMapper;

    @Autowired
    private com.ruoyi.im.util.ImDistributedLock distributedLock;

    @Autowired
    private com.ruoyi.im.util.ImRedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(ImMessageSendRequest request, Long userId) {
        // 幂等性检查：使用clientMsgId防止重复发送
        String clientMsgId = request.getClientMsgId();
        if (clientMsgId != null && !clientMsgId.isEmpty()) {
            Long existingMessageId = redisUtil.checkAndRecordClientMsgId(clientMsgId);
            if (existingMessageId != null) {
                log.info("消息已存在，跳过重复发送: clientMsgId={}, messageId={}", clientMsgId, existingMessageId);
                return existingMessageId;
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
        }

        // 使用分布式锁防止并发发送消息导致的数据不一致
        final Long finalConversationId = conversationId;
        final String finalClientMsgId = clientMsgId;
        Long messageId = distributedLock.executeWithLock(
                com.ruoyi.im.util.ImDistributedLock.LockKeys.sendMessageKey(finalConversationId),
                10, // 10秒过期时间足够
                () -> doSendMessage(request, userId, finalConversationId, sender, finalClientMsgId));

        return messageId;
    }

    /**
     * 实际执行发送消息的逻辑
     * 支持消息发送状态追踪
     */
    private Long doSendMessage(ImMessageSendRequest request, Long userId, Long conversationId, ImUser sender,
            String clientMsgId) {
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setReceiverId(request.getReceiverId());
        message.setMessageType(request.getType());
        message.setReplyToMessageId(request.getReplyToMessageId());

        String contentToSave = encryptionUtil.encryptMessage(request.getContent());
        message.setContent(contentToSave);
        message.setIsRevoked(0);
        message.setCreateTime(LocalDateTime.now());

        // 设置发送状态相关字段
        message.setClientMsgId(clientMsgId);
        message.setSendStatus("SENDING"); // 初始状态为发送中
        message.setSendRetryCount(0);
        message.setDeliveredTime(LocalDateTime.now()); // 暂时设置为当前时间，表示已处理

        imMessageMapper.insertImMessage(message);

        // 记录clientMsgId与消息ID的映射（用于幂等性）
        if (clientMsgId != null && !clientMsgId.isEmpty()) {
            redisUtil.recordClientMsgId(clientMsgId, message.getId());
        }

        // 更新会话的最后消息ID和时间（确保会话列表正确显示最新消息）
        ImConversation conversationUpdate = new ImConversation();
        conversationUpdate.setId(conversationId);
        conversationUpdate.setLastMessageId(message.getId());
        conversationUpdate.setLastMessageTime(message.getCreateTime());
        conversationUpdate.setUpdateTime(LocalDateTime.now());
        imConversationMapper.updateById(conversationUpdate);

        // 增加会话中其他成员的未读消息数
        List<com.ruoyi.im.domain.ImConversationMember> members = imConversationMemberMapper
                .selectByConversationId(conversationId);

        // 性能优化：只有在成员数量较少时才清除接收者的会话列表缓存
        // 大群（>20人）依赖WebSocket更新或TTL过期，避免循环操作Redis导致发送延迟
        boolean shouldEvictCache = members.size() <= 20;

        for (com.ruoyi.im.domain.ImConversationMember member : members) {
            if (!member.getUserId().equals(userId)) {
                imConversationMemberMapper.incrementUnreadCount(conversationId, member.getUserId(), 1);

                if (shouldEvictCache) {
                    redisUtil.delete("conversation:list:" + member.getUserId());
                }
            }
        }

        // 始终清除发送者的会话列表缓存，确保发送者看到最新状态
        redisUtil.delete("conversation:list:" + userId);

        // 处理@提及
        ImMentionInfo mentionInfo = request.getMentionInfo();
        if (mentionInfo == null && request.getContent() != null) {
            // 自动解析消息内容中的@提及
            mentionInfo = messageMentionService.parseMentions(request.getContent());
        }
        if (mentionInfo != null
                && (mentionInfo.getUserIds() != null || Boolean.TRUE.equals(mentionInfo.getMentionAll()))) {
            // 设置会话ID用于权限验证
            mentionInfo.setConversationId(conversationId);
            messageMentionService.createMentions(message.getId(), mentionInfo, userId);
        }

        // 记录消息发送审计日志
        AuditLogUtil.logSendMessage(userId, message.getId(), conversationId, true);

        return message.getId();
    }

    @Override
    public List<ImMessageVO> getMessages(Long conversationId, Long userId, Long lastId, Integer limit) {
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

            // 从Map中获取发送者信息（避免重复查询）
            ImUser sender = userMap.get(message.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatar());
            }

            vo.setIsSelf(message.getSenderId().equals(userId));

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
        quotedMessage.setType(originalMessage.getMessageType());
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

        LocalDateTime now = LocalDateTime.now();
        if (message.getCreateTime().plusMinutes(2).isBefore(now)) {
            throw new BusinessException("消息发送超过2分钟，无法撤回");
        }

        message.setIsRevoked(1);
        message.setRevokedTime(now);
        imMessageMapper.updateImMessage(message);

        // 广播撤回通知给会话中的其他用户
        broadcastRecallNotification(message.getConversationId(), messageId, userId);
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
                senderId, startTime, endTime, includeRevoked, exactMatch);

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / pageSize);

        // 查询结果
        List<ImMessage> messageList = imMessageMapper.searchMessages(conversationId, keyword, messageType,
                senderId, startTime, endTime, includeRevoked, exactMatch, offset, pageSize);

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
            item.setType(message.getMessageType());
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

    /**
     * 广播消息撤回通知
     *
     * @param conversationId 会话ID
     * @param messageId      消息ID
     * @param userId         用户ID
     */
    private void broadcastRecallNotification(Long conversationId, Long messageId, Long userId) {
        try {
            // com.ruoyi.im.websocket.ImWebSocketEndpoint wsEndpoint = new
            // com.ruoyi.im.websocket.ImWebSocketEndpoint();

            java.util.Map<String, Object> recallNotification = new java.util.HashMap<>();
            recallNotification.put("type", "recall");
            recallNotification.put("conversationId", conversationId);
            recallNotification.put("messageId", messageId);
            recallNotification.put("userId", userId);
            recallNotification.put("timestamp", java.time.LocalDateTime.now().toString());

            // 获取用户信息
            if (imUserMapper != null) {
                com.ruoyi.im.domain.ImUser user = imUserMapper.selectImUserById(userId);
                if (user != null) {
                    recallNotification.put("userName",
                            user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            }

            // 获取会话成员
            List<com.ruoyi.im.domain.ImConversationMember> members = imConversationMemberMapper
                    .selectByConversationId(conversationId);

            if (members != null) {
                java.util.Map<Long, javax.websocket.Session> onlineUsers = com.ruoyi.im.websocket.ImWebSocketEndpoint
                        .getOnlineUsers();

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String messageJson = mapper.writeValueAsString(recallNotification);

                // 向会话中的每个在线成员发送撤回通知
                for (com.ruoyi.im.domain.ImConversationMember member : members) {
                    // 不发送给撤回者自己
                    if (member.getUserId().equals(userId)) {
                        continue;
                    }

                    javax.websocket.Session targetSession = onlineUsers.get(member.getUserId());
                    if (targetSession != null && targetSession.isOpen()) {
                        try {
                            targetSession.getBasicRemote().sendText(messageJson);
                            log.info("已发送撤回通知: messageId={}, targetUserId={}", messageId, member.getUserId());
                        } catch (Exception e) {
                            log.error("发送撤回通知失败: userId={}", member.getUserId(), e);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("广播撤回通知失败: messageId={}", messageId, e);
        }
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
}
