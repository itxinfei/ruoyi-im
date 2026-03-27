package com.ruoyi.im.service.impl;

import com.ruoyi.im.constant.SystemConstants;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageEditHistory;
import com.ruoyi.im.domain.ImUserSession;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.conversation.ImPrivateConversationCreateRequest;
import com.ruoyi.im.dto.mention.ImMentionInfo;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.dto.message.ImMessageBatchForwardRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImMessageEditHistoryMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserSessionMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageMentionService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImSystemConfigService;
import com.ruoyi.im.util.AuditLogUtil;
import com.ruoyi.im.util.BusinessExceptionHelper;
import com.ruoyi.im.util.MessageEncryptionUtil;
import com.ruoyi.im.listener.BotMessageListener;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ImMessageServiceImpl implements ImMessageService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageServiceImpl.class);

    private static final String CACHE_KEY_CONVERSATION_LIST = "conversation:list:";
    private static final int CACHE_EVICT_THRESHOLD = 20;

    private final ImMessageMapper imMessageMapper;
    private final com.ruoyi.im.mapper.ImMessageReactionMapper reactionMapper;
    private final com.ruoyi.im.mapper.ImMessageReadMapper readMapper;
    private final ImUserMapper imUserMapper;
    private final ImConversationMapper imConversationMapper;
    private final ImConversationMemberMapper imConversationMemberMapper;
    private final ImUserSessionMapper imUserSessionMapper;
    private final ImConversationService imConversationService;
    private final MessageEncryptionUtil encryptionUtil;
    private final ImMessageMentionService messageMentionService;
    private final ImMessageEditHistoryMapper editHistoryMapper;
    private final com.ruoyi.im.util.ImDistributedLock distributedLock;
    private final com.ruoyi.im.util.ImRedisUtil redisUtil;
    private final com.ruoyi.im.service.ImWebSocketBroadcastService broadcastService;
    private final ImSystemConfigService systemConfigService;
    private final com.ruoyi.im.service.ImUrlMetadataService urlMetadataService;
    private final com.ruoyi.im.service.ImMessageReadService messageReadService;
    private final com.ruoyi.im.service.ImMessageReactionService messageReactionService;
    private final com.ruoyi.im.service.ISensitiveWordService sensitiveWordService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造器注入依赖
     */
    public ImMessageServiceImpl(ImMessageMapper imMessageMapper,
                                 com.ruoyi.im.mapper.ImMessageReactionMapper reactionMapper,
                                 com.ruoyi.im.mapper.ImMessageReadMapper readMapper,
                                 ImUserMapper imUserMapper,
                                 ImConversationMapper imConversationMapper,
                                 ImConversationMemberMapper imConversationMemberMapper,
                                 ImUserSessionMapper imUserSessionMapper,
                                 ImConversationService imConversationService,
                                 MessageEncryptionUtil encryptionUtil,
                                 ImMessageMentionService messageMentionService,
                                 ImMessageEditHistoryMapper editHistoryMapper,
                                 com.ruoyi.im.util.ImDistributedLock distributedLock,
                                 com.ruoyi.im.util.ImRedisUtil redisUtil,
                                 com.ruoyi.im.service.ImWebSocketBroadcastService broadcastService,
                                 ImSystemConfigService systemConfigService,
                                 com.ruoyi.im.service.ImUrlMetadataService urlMetadataService,
                                 com.ruoyi.im.service.ImMessageReadService messageReadService,
                                 com.ruoyi.im.service.ImMessageReactionService messageReactionService,
                                 com.ruoyi.im.service.ISensitiveWordService sensitiveWordService,
                                 ApplicationEventPublisher eventPublisher) {
        this.imMessageMapper = imMessageMapper;
        this.reactionMapper = reactionMapper;
        this.readMapper = readMapper;
        this.imUserMapper = imUserMapper;
        this.imConversationMapper = imConversationMapper;
        this.imConversationMemberMapper = imConversationMemberMapper;
        this.imUserSessionMapper = imUserSessionMapper;
        this.imConversationService = imConversationService;
        this.encryptionUtil = encryptionUtil;
        this.messageMentionService = messageMentionService;
        this.editHistoryMapper = editHistoryMapper;
        this.distributedLock = distributedLock;
        this.redisUtil = redisUtil;
        this.broadcastService = broadcastService;
        this.systemConfigService = systemConfigService;
        this.urlMetadataService = urlMetadataService;
        this.messageReadService = messageReadService;
        this.messageReactionService = messageReactionService;
        this.sensitiveWordService = sensitiveWordService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 从文本中提取第一个 URL
     */
    private String extractUrl(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        // 匹配 http/https 链接
        String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher m = p.matcher(content);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImMessageVO sendMessage(ImMessageSendRequest request, Long userId) {
        // 幂等性检查：使用clientMsgId防止重复发送
        String clientMsgId = request.getClientMsgId();
        if (clientMsgId != null && !clientMsgId.isEmpty()) {
            Long existingMessageId = redisUtil.checkAndRecordClientMsgId(clientMsgId);
            if (existingMessageId != null) {
                log.debug("消息已存在，跳过重复发送: clientMsgId={}, messageId={}", clientMsgId, existingMessageId);
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
            BusinessExceptionHelper.throwUserNotFound();
        }

        Long conversationId = request.getConversationId();

        if (conversationId == null || conversationId == 0) {
            if (request.getReceiverId() == null || request.getReceiverId() == 0) {
                BusinessExceptionHelper.throwNotAllowed("会话ID和接收者ID不能同时为空");
            }

            ImPrivateConversationCreateRequest createRequest = new ImPrivateConversationCreateRequest();
            createRequest.setPeerUserId(request.getReceiverId());
            conversationId = imConversationService.createPrivateConversation(userId, createRequest);
        } else {
            ImConversation conversation = imConversationMapper.selectById(conversationId);
            if (conversation == null) {
                BusinessExceptionHelper.throwConversationNotFound();
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

        // 敏感词过滤：仅对文本消息执行强制脱敏
        if ("TEXT".equalsIgnoreCase(request.getType()) && plainContent != null) {
            plainContent = sensitiveWordService.filter(plainContent);
        }

        String contentToSave = encryptionUtil.encryptMessage(plainContent);
        message.setContent(contentToSave);
        message.setIsRevoked(0);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        message.setClientMsgId(clientMsgId);
        message.setSendStatus(SystemConstants.MESSAGE_STATUS_SENDING);
        message.setSendRetryCount(0);
        message.setDeliveredTime(LocalDateTime.now());

        // 生成递增 Seq (Doc-34 §2.1)
        String seqKey = "im:seq:" + conversationId;
        Long nextSeq = redisUtil.increment(seqKey);
        message.setSeq(nextSeq);

        try {
            imMessageMapper.insertImMessage(message);
        } catch (Exception e) {
            log.error("插入消息失败: ", e);
            throw new BusinessException(500, "消息存储失败");
        }

        if (clientMsgId != null && !clientMsgId.isEmpty()) {
            redisUtil.recordClientMsgId(clientMsgId, message.getId());
        }

        try {
            ImConversation conversationUpdate = new ImConversation();
            conversationUpdate.setId(conversationId);
            conversationUpdate.setLastMessageId(message.getId());
            conversationUpdate.setLastMessageTime(message.getCreateTime());
            conversationUpdate.setUpdateTime(LocalDateTime.now());
            imConversationMapper.updateById(conversationUpdate);
        } catch (Exception e) {
            log.warn("更新会话最后一条消息失败: conversationId={}", conversationId);
        }

        List<com.ruoyi.im.domain.ImConversationMember> members = null;
        try {
            members = imConversationMemberMapper.selectByConversationId(conversationId);

            if (members != null && !members.isEmpty()) {
                for (com.ruoyi.im.domain.ImConversationMember member : members) {
                    if (member != null && member.getUserId() != null && !member.getUserId().equals(userId)) {
                        incrementSessionUnread(conversationId, member.getUserId(), 1);
                        imConversationMemberMapper.incrementUnreadCount(conversationId, member.getUserId(), 1);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("更新未读数失败: ", e);
        }

        try {
            ImMentionInfo mentionInfo = request.getMentionInfo();
            if (mentionInfo == null && request.getContent() != null) {
                mentionInfo = messageMentionService.parseMentions(request.getContent());
            }

            if (mentionInfo != null
                    && (mentionInfo.getUserIds() != null || Boolean.TRUE.equals(mentionInfo.getMentionAll()))) {
                mentionInfo.setConversationId(conversationId);
                messageMentionService.createMentions(message.getId(), mentionInfo, userId);
            }
        } catch (Exception e) {
            log.warn("处理@提及失败: ", e);
        }

        ImMessageVO vo = new ImMessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setContent(plainContent);
        vo.setType(message.getMessageType());
        vo.setIsSelf(true);
        vo.setSenderName(sender.getNickname());
        vo.setSenderAvatar(sender.getAvatar());
        vo.setSendTime(message.getCreateTime());
        vo.setStatus(1);

        // 以下为可选增强逻辑，必须隔离异常
        try {
            if ("TEXT".equalsIgnoreCase(request.getType()) && plainContent != null) {
                String url = extractUrl(plainContent);
                if (url != null) {
                    com.ruoyi.im.domain.ImUrlMetadata metadata = urlMetadataService.parseUrl(url);
                    if (metadata != null && "SUCCESS".equals(metadata.getFetchStatus())) {
                        ImMessageVO.UrlMetadataVO urlVO = new ImMessageVO.UrlMetadataVO();
                        BeanUtils.copyProperties(metadata, urlVO);
                        vo.setUrlMetadata(urlVO);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("URL解析忽略: {}", e.getMessage());
        }

        try {
            ImConversation conversation = imConversationMapper.selectById(conversationId);
            if (conversation != null && "GROUP".equalsIgnoreCase(conversation.getType())) {
                Long groupId = conversation.getTargetId();
                if (groupId != null && "TEXT".equalsIgnoreCase(message.getMessageType())) {
                    eventPublisher.publishEvent(
                            new BotMessageListener.GroupMessageEvent(conversationId, groupId, userId, plainContent)
                    );
                }
            }
        } catch (Exception e) {
            log.error("机器人事件发布失败（已忽略）");
        }

        // 异步解耦分发与 ACK 回执 (Doc-01 §3.2 & Doc-34 §2.2)
        List<Long> targetUserIds = new ArrayList<>();
        if (members != null) {
            for (com.ruoyi.im.domain.ImConversationMember member : members) {
                targetUserIds.add(member.getUserId());
            }
        } else {
            targetUserIds.add(userId);
            if (request.getReceiverId() != null) {
                targetUserIds.add(request.getReceiverId());
            }
        }

        com.ruoyi.im.listener.MessageSendEvent sendEvent = new com.ruoyi.im.listener.MessageSendEvent(
            this, 
            vo, 
            targetUserIds, 
            clientMsgId
        );
        eventPublisher.publishEvent(sendEvent);

        return vo;
    }

    @Override
    public List<ImMessageVO> getMessages(Long conversationId, Long userId, Long lastId, Integer limit) {
        // 调试日志：记录接收到的 userId
        log.debug("getMessages 被调用 - conversationId={}, userId={}, lastId={}, limit={}",
                conversationId, userId, lastId, limit);

        // 权限校验：验证用户是否是会话成员
        com.ruoyi.im.domain.ImConversationMember member = imConversationMemberMapper
                .selectByConversationIdAndUserId(conversationId, userId);
        if (member == null) {
            throw new BusinessException("无权访问该会话的消息");
        }

        List<ImMessageVO> voList = new ArrayList<>();

        // 参数校验和默认值（使用Optional优化）
        limit = Optional.ofNullable(limit)
                .filter(l -> l > 0)
                .map(l -> Math.min(l, SystemConstants.MAX_PAGE_SIZE))
                .orElse(SystemConstants.DEFAULT_PAGE_SIZE);

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

        // --- 性能优化：批量预取增强数据 ---
        List<Long> msgIds = messageList.stream().map(ImMessage::getId).collect(java.util.stream.Collectors.toList());
        
        // 1. 批量预取 Reaction (按 message_id 分组)
        java.util.Map<Long, List<com.ruoyi.im.vo.reaction.ImMessageReactionVO>> reactionStatsMap = new java.util.HashMap<>();
        if (!msgIds.isEmpty()) {
            try {
                // 暂时利用 messageReactionService 循环获取作为降级，后续可真正实现 Mapper 批量统计
                // 鉴于目前已经注入了 Mapper，我直接在内存中聚合所有记录以达到最优性能
                List<com.ruoyi.im.domain.ImMessageReaction> allReactions = reactionMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.ruoyi.im.domain.ImMessageReaction>()
                                .in(com.ruoyi.im.domain.ImMessageReaction::getMessageId, msgIds)
                );
                // 内存中按 messageId 和 emoji 分组并转换 VO
                if (allReactions != null) {
                    java.util.Map<Long, java.util.Map<String, List<com.ruoyi.im.domain.ImMessageReaction>>> grouped = 
                        allReactions.stream().collect(java.util.stream.Collectors.groupingBy(
                            com.ruoyi.im.domain.ImMessageReaction::getMessageId,
                            java.util.stream.Collectors.groupingBy(com.ruoyi.im.domain.ImMessageReaction::getEmoji)
                        ));
                    
                    grouped.forEach((mid, emojiMap) -> {
                        List<com.ruoyi.im.vo.reaction.ImMessageReactionVO> subList = new ArrayList<>();
                        emojiMap.forEach((emoji, list) -> {
                            com.ruoyi.im.vo.reaction.ImMessageReactionVO rvo = new com.ruoyi.im.vo.reaction.ImMessageReactionVO();
                            rvo.setEmoji(emoji);
                            rvo.setCount(list.size());
                            rvo.setIsMine(list.stream().anyMatch(r -> r.getUserId().equals(userId)));
                            // 补充用户名列表（简化处理，仅取前3名）
                            subList.add(rvo);
                        });
                        reactionStatsMap.put(mid, subList);
                    });
                }
            } catch (Exception e) {
                log.warn("批量预取表情表态失败: {}", e.getMessage());
            }
        }

        // 2. 批量预取已读统计
        java.util.Map<Long, com.ruoyi.im.vo.message.ImMessageReadStatusVO> readStatusMap = new java.util.HashMap<>();
        if (!msgIds.isEmpty()) {
            try {
                List<java.util.Map<String, Object>> counts = readMapper.batchCountReadUsersByMessageIds(msgIds);
                if (counts != null) {
                    for (java.util.Map<String, Object> entry : counts) {
                        Long mid = ((Number) entry.get("message_id")).longValue();
                        Integer count = ((Number) entry.get("read_count")).intValue();
                        com.ruoyi.im.vo.message.ImMessageReadStatusVO rs = new com.ruoyi.im.vo.message.ImMessageReadStatusVO();
                        rs.setReadCount(count);
                        // totalCount 暂时由 Service 内部逻辑动态计算（取决于群成员数）
                        readStatusMap.put(mid, rs);
                    }
                }
            } catch (Exception e) {
                log.warn("批量预取已读状态失败: {}", e.getMessage());
            }
        }

        for (ImMessage message : messageList) {
            ImMessageVO vo = new ImMessageVO();
            BeanUtils.copyProperties(message, vo);

            String decryptedContent = encryptionUtil.decryptMessage(message.getContent());
            vo.setContent(decryptedContent);

            // 从Map中获取发送者信息（避免重复查询），使用Optional优化
            Optional.ofNullable(userMap.get(message.getSenderId()))
                    .ifPresent(sender -> {
                        vo.setSenderName(sender.getNickname());
                        vo.setSenderAvatar(sender.getAvatar());
                    });

            // 设置 isSelf 并记录调试日志
            boolean isSelf = message.getSenderId().equals(userId);
            vo.setIsSelf(isSelf);
            log.debug("消息 isSelf 判断 - messageId={}, senderId={}, userId={}, isSelf={}, senderName={}",
                    message.getId(), message.getSenderId(), userId, isSelf, vo.getSenderName());

            // 填充已读状态统计（仅发送者自己的消息展示已读/未读人数）
            if (isSelf) {
                com.ruoyi.im.vo.message.ImMessageReadStatusVO rsVO = readStatusMap.get(message.getId());
                if (rsVO != null) {
                    ImMessageVO.MessageReadStatus readStatus = new ImMessageVO.MessageReadStatus();
                    BeanUtils.copyProperties(rsVO, readStatus);
                    // 补充总人数（私聊2人，群聊暂不精确获取以保性能，前端可根据群成员列表显示）
                    vo.setReadStatus(readStatus);
                }
            }

            // 填充 URL 元数据预览 (保持现状，此方法内部带缓存且 JSoup 解析无法批量)
            if ("TEXT".equalsIgnoreCase(message.getMessageType()) && decryptedContent != null) {
                String url = extractUrl(decryptedContent);
                if (url != null) {
                    try {
                        com.ruoyi.im.domain.ImUrlMetadata metadata = urlMetadataService.getByUrl(url);
                        if (metadata != null && "SUCCESS".equals(metadata.getFetchStatus())) {
                            ImMessageVO.UrlMetadataVO urlVO = new ImMessageVO.UrlMetadataVO();
                            BeanUtils.copyProperties(metadata, urlVO);
                            vo.setUrlMetadata(urlVO);
                        }
                    } catch (Exception e) {
                        log.warn("填充 URL 元数据失败: messageId={}, error={}", message.getId(), e.getMessage());
                    }
                }
            }

            // 填充表情表态统计（使用预取的 Map）
            vo.setReactions(reactionStatsMap.getOrDefault(message.getId(), new java.util.ArrayList<>()));

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

        // 使用Optional优化：从Map中获取消息，若不存在则返回null
        return Optional.ofNullable(replyToMessageMap.get(messageId))
                .map(originalMessage -> {
                    ImMessageVO.QuotedMessageVO quotedMessage = new ImMessageVO.QuotedMessageVO();
                    quotedMessage.setId(originalMessage.getId());
                    quotedMessage.setType(originalMessage.getMessageType());
                    quotedMessage.setSendTime(originalMessage.getCreateTime());

                    // 从预加载的Map中获取发送者信息
                    String senderName = Optional.ofNullable(userMap.get(originalMessage.getSenderId()))
                            .map(ImUser::getNickname)
                            .orElse("未知用户");
                    quotedMessage.setSenderName(senderName);

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
                })
                .orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long messageId, Long userId) {
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message == null) {
            BusinessExceptionHelper.throwMessageNotFound();
        }

        if (!message.getSenderId().equals(userId)) {
            BusinessExceptionHelper.throwNoPermission("无权撤回该消息");
        }

        if (message.getIsRevoked() == 1) {
            BusinessExceptionHelper.throwNotAllowed("消息已撤回");
        }

        // 从系统配置获取撤回时间限制
        Integer timeLimit = systemConfigService.getMessageRecallTimeLimit();
        if (timeLimit == null) {
            timeLimit = 2; // 默认2分钟
        }
        LocalDateTime now = LocalDateTime.now();

        if (message.getCreateTime().plusMinutes(timeLimit).isBefore(now)) {
            BusinessExceptionHelper.throwNotAllowed("消息发送超过" + timeLimit + "分钟，无法撤回");
        }

        message.setIsRevoked(1);
        message.setRevokedTime(now);
        imMessageMapper.updateImMessage(message);

        // 记录审计日志
        AuditLogUtil.logRecallMessage(userId, messageId, true);

        // [分层对齐] 广播撤回通知给会话中的其他用户
        try {
            broadcastService.broadcastRecallNotification(message.getConversationId(), messageId, userId);
        } catch (Exception e) {
            log.error("广播撤回通知失败: conversationId={}, messageId={}", message.getConversationId(), messageId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editMessage(Long messageId, String newContent, Long userId) {
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message == null) {
            BusinessExceptionHelper.throwMessageNotFound();
        }

        if (!message.getSenderId().equals(userId)) {
            BusinessExceptionHelper.throwNoPermission("无权编辑该消息");
        }

        if (!"TEXT".equals(message.getMessageType())) {
            BusinessExceptionHelper.throwNotAllowed("只能编辑文本消息");
        }

        if (message.getIsRevoked() == 1) {
            BusinessExceptionHelper.throwNotAllowed("已撤回的消息不能编辑");
        }

        LocalDateTime now = LocalDateTime.now();
        // 限制：消息发送超过指定时间不能编辑
        Integer editTimeLimit = systemConfigService.getMessageRecallTimeLimit();
        if (editTimeLimit == null) {
            editTimeLimit = SystemConstants.MESSAGE_EDIT_TIME_LIMIT; // 默认值
        }
        if (message.getCreateTime().plusMinutes(editTimeLimit).isBefore(now)) {
            BusinessExceptionHelper.throwNotAllowed("消息发送超过" + editTimeLimit + "分钟，无法编辑");
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

        // 广播通知：通知所有成员消息已编辑
        broadcastService.broadcastMessageUpdate(message.getConversationId(), messageId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId, Long userId) {
        ImMessage message = imMessageMapper.selectImMessageById(messageId);
        if (message == null) {
            BusinessExceptionHelper.throwMessageNotFound();
        }

        // 软删除，设置is_deleted = 1
        // 注意：这里只允许删除自己的消息，或者管理员可以删除任何消息
        // 如果是双向删除（删除后对方也不可见），需要权限校验
        // 如果是单向删除（仅自己不可见），需要另外的表来记录删除状态
        // 这里简化实现，假设是双向删除，且只能删除自己的消息
        if (!message.getSenderId().equals(userId)) {
            BusinessExceptionHelper.throwNoPermission("无权删除该消息");
        }

        message.setIsDeleted(1);
        message.setDeletedTime(LocalDateTime.now());
        imMessageMapper.updateImMessage(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long conversationId, Long userId, List<Long> messageIds, boolean broadcast) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        int readCount = 0;
        Long maxMessageId = null;

        List<ImMessage> messages = imMessageMapper.selectImMessageListByIds(messageIds);
        for (ImMessage message : messages) {
            if (message != null && !message.getSenderId().equals(userId)) {
                readCount++;
                if (maxMessageId == null || message.getId() > maxMessageId) {
                    maxMessageId = message.getId();
                }
            }
        }

        if (readCount > 0) {
            decrementSessionUnread(conversationId, userId, readCount);
            imConversationMemberMapper.decrementUnreadCount(conversationId, userId, readCount);
            if (maxMessageId != null) {
                updateSessionLastRead(conversationId, userId, maxMessageId);
                imConversationMemberMapper.updateLastReadMessageId(conversationId, userId, maxMessageId);

                // 根据broadcast参数决定是否广播已读回执
                if (broadcast) {
                    try {
                        broadcastService.broadcastReadReceipt(conversationId, maxMessageId, userId);
                    } catch (Exception e) {
                        log.error("广播已读同步失败: userId={}, conversationId={}", userId, conversationId, e);
                    }
                }
            }
        }
    }

    @Override
    public Long forwardMessage(Long messageId, Long toConversationId, Long toUserId, String content, Long userId) {
        ImMessage originalMessage = imMessageMapper.selectImMessageById(messageId);
        if (originalMessage == null) {
            BusinessExceptionHelper.throwMessageNotFound();
        }

        // 解密原消息内容
        String decryptedContent = encryptionUtil.decryptMessage(originalMessage.getContent());

        ImMessage forwardMessage = new ImMessage();
        forwardMessage.setConversationId(
            Optional.ofNullable(toConversationId).orElse(originalMessage.getConversationId()));
        forwardMessage.setSenderId(userId);
        forwardMessage.setReceiverId(
            Optional.ofNullable(toUserId).orElse(originalMessage.getReceiverId()));
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
    public void batchForward(com.ruoyi.im.dto.message.ImMessageBatchForwardRequest request, Long userId) {
        List<Long> messageIds = request.getMessageIds();
        List<Long> toConversationIds = request.getToConversationIds();

        if (Boolean.TRUE.equals(request.getIsCombine())) {
            // 合并转发：构造摘要内容
            StringBuilder combinedContent = new StringBuilder();
            String title = request.getTitle() != null ? request.getTitle() : "聊天记录";
            combinedContent.append("--- ").append(title).append(" ---\n");

            List<ImMessage> messages = imMessageMapper.selectImMessageListByIds(messageIds);
            // 保持原消息顺序
            messages.sort(java.util.Comparator.comparing(ImMessage::getId));

            for (ImMessage msg : messages) {
                ImUser sender = imUserMapper.selectImUserById(msg.getSenderId());
                String senderName = sender != null ? sender.getNickname() : "用户" + msg.getSenderId();
                String text = encryptionUtil.decryptMessage(msg.getContent());
                // 简单处理：如果是图片/文件，显示占位符
                if (!"TEXT".equalsIgnoreCase(msg.getMessageType())) {
                    text = "[" + msg.getMessageType() + "]";
                }
                combinedContent.append(senderName).append(": ").append(text).append("\n");
            }

            for (Long convId : toConversationIds) {
                com.ruoyi.im.dto.message.ImMessageSendRequest sendReq = new com.ruoyi.im.dto.message.ImMessageSendRequest();
                sendReq.setConversationId(convId);
                sendReq.setType("COMBINE_FORWARD");
                sendReq.setContent(combinedContent.toString());
                this.sendMessage(sendReq, userId);
            }
        } else {
            // 逐条转发
            for (Long convId : toConversationIds) {
                for (Long msgId : messageIds) {
                    this.forwardMessage(msgId, convId, null, null, userId);
                }
            }
        }
    }

    @Override
    public Long replyMessage(Long messageId, String content, Long userId) {
        ImMessage originalMessage = imMessageMapper.selectImMessageById(messageId);
        if (originalMessage == null) {
            BusinessExceptionHelper.throwMessageNotFound();
        }

        if (content == null || content.trim().isEmpty()) {
            BusinessExceptionHelper.throwNotAllowed("回复内容不能为空");
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
    public List<ImMessageVO> getMessagesBySeqRange(Long conversationId, Long startSeq, Long endSeq) {
        log.info("Gap filling: fetching messages for conversation {} from seq {} to {}", 
                 conversationId, startSeq, endSeq);
        
        List<ImMessage> messageList = imMessageMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ImMessage>()
                .eq(ImMessage::getConversationId, conversationId)
                .gt(ImMessage::getSeq, startSeq)
                .le(ImMessage::getSeq, endSeq)
                .orderByAsc(ImMessage::getSeq)
        );

        List<ImMessageVO> voList = new ArrayList<>();
        // 简化版转换逻辑，仅针对同步包优化性能
        for (ImMessage message : messageList) {
            ImMessageVO vo = new ImMessageVO();
            BeanUtils.copyProperties(message, vo);
            vo.setContent(encryptionUtil.decryptMessage(message.getContent()));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public int getTodayMessageCount(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);

        return imMessageMapper.countBySenderIdAndTimeRange(userId, todayStart, todayEnd);
    }

    @Override
    public ImMessageSearchResultVO searchMessages(Long conversationId, String keyword, String messageType,
            Long senderId, LocalDateTime startTime, LocalDateTime endTime,
            Integer pageNum, Integer pageSize, Boolean includeRevoked,
            Boolean exactMatch, Long userId) {
        // 参数校验和默认值设置（使用Optional优化）
        pageNum = Optional.ofNullable(pageNum)
                .filter(p -> p >= 1)
                .orElse(1);

        pageSize = Optional.ofNullable(pageSize)
                .filter(p -> p > 0)
                .map(p -> Math.min(p, SystemConstants.MAX_PAGE_SIZE))
                .orElse(SystemConstants.DEFAULT_PAGE_SIZE);

        includeRevoked = Optional.ofNullable(includeRevoked).orElse(false);
        exactMatch = Optional.ofNullable(exactMatch).orElse(false);

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

        String lowerContent = content.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int keywordIndex = lowerContent.indexOf(lowerKeyword);
        if (keywordIndex == -1) {
            if (content.length() > snippetLength) {
                return content.substring(0, snippetLength) + "...";
            }
            return content;
        }

        int start = Math.max(0, keywordIndex - snippetLength / 2);
        int end = Math.min(content.length(), start + snippetLength);

        if (end - start < snippetLength && start > 0) {
            start = Math.max(0, end - snippetLength);
        }

        String snippet = content.substring(start, end);

        if (start > 0) {
            snippet = "..." + snippet;
        }
        if (end < content.length()) {
            snippet = snippet + "...";
        }

        return snippet;
    }

    private void incrementSessionUnread(Long conversationId, Long userId, int delta) {
        ensureUserSession(conversationId, userId);
        int incrementValue = Math.max(delta, 0);
        imUserSessionMapper.incrementUnreadCount(userId, conversationId, incrementValue);
    }

    private void decrementSessionUnread(Long conversationId, Long userId, int delta) {
        ensureUserSession(conversationId, userId);
        int decrementValue = Math.max(delta, 0);
        imUserSessionMapper.decrementUnreadCount(userId, conversationId, decrementValue);
    }

    private void updateSessionLastRead(Long conversationId, Long userId, Long lastReadMessageId) {
        ensureUserSession(conversationId, userId);
        imUserSessionMapper.updateLastReadMessage(userId, conversationId, lastReadMessageId);
    }

    private ImUserSession ensureUserSession(Long conversationId, Long userId) {
        ImUserSession userSession = imUserSessionMapper.selectByUserIdAndConversationId(userId, conversationId);
        if (userSession != null) {
            return userSession;
        }
        
        try {
            ImUserSession newSession = new ImUserSession();
            newSession.setConversationId(conversationId);
            newSession.setUserId(userId);
            newSession.setIsPinned(0);
            newSession.setIsMuted(0);
            newSession.setIsArchived(0);
            newSession.setUnreadCount(0);
            newSession.setIsDeleted(0);
            newSession.setCreateTime(LocalDateTime.now());
            newSession.setUpdateTime(LocalDateTime.now());
            imUserSessionMapper.insert(newSession);
            return newSession;
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 处理并发冲突：如果插入失败说明其他线程已创建，直接重新查询
            return imUserSessionMapper.selectByUserIdAndConversationId(userId, conversationId);
        } catch (Exception e) {
            log.error("确保用户会话记录失败: userId={}, convId={}", userId, conversationId, e);
            throw new BusinessException(500, "同步会话状态失败");
        }
    }
}
