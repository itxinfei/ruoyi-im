package com.ruoyi.im.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.domain.ImDingMessage;
import com.ruoyi.im.domain.ImDingRead;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.ding.DingQueryRequest;
import com.ruoyi.im.dto.ding.DingSendRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.mapper.ImDingMapper;
import com.ruoyi.im.mapper.ImDingReadMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImDingService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.vo.ding.DingMessageVO;
import com.ruoyi.im.vo.ding.DingReadUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DING消息服务实现
 *
 * @author ruoyi
 */
@Service
public class ImDingServiceImpl implements ImDingService {

    private static final Logger log = LoggerFactory.getLogger(ImDingServiceImpl.class);

    // DING类型常量
    private static final String DING_TYPE_APP = "APP";
    private static final String DING_TYPE_SMS = "SMS";
    private static final String DING_TYPE_CALL = "CALL";

    // 优先级常量
    private static final String PRIORITY_URGENT = "URGENT";
    private static final String PRIORITY_NORMAL = "NORMAL";

    // 状态常量
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final ImDingMapper dingMapper;
    private final ImDingReadMapper dingReadMapper;
    private final ImConversationMapper conversationMapper;
    private final ImConversationMemberMapper conversationMemberMapper;
    private final ImUserMapper userMapper;
    private final ImWebSocketBroadcastService broadcastService;

    /**
     * 构造器注入依赖
     */
    public ImDingServiceImpl(ImDingMapper dingMapper,
                            ImDingReadMapper dingReadMapper,
                            ImConversationMapper conversationMapper,
                            ImConversationMemberMapper conversationMemberMapper,
                            ImUserMapper userMapper,
                            ImWebSocketBroadcastService broadcastService) {
        this.dingMapper = dingMapper;
        this.dingReadMapper = dingReadMapper;
        this.conversationMapper = conversationMapper;
        this.conversationMemberMapper = conversationMemberMapper;
        this.userMapper = userMapper;
        this.broadcastService = broadcastService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DingMessageVO sendDing(DingSendRequest request, Long userId) {
        log.info("发送DING消息: userId={}, conversationId={}, content={}",
                userId, request.getConversationId(), request.getContent());

        // 1. 验证会话是否存在
        ImConversation conversation = conversationMapper.selectById(request.getConversationId());
        if (conversation == null) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NOT_FOUND);
        }

        // 2. 获取会话成员列表
        List<ImConversationMember> members = conversationMemberMapper.selectByConversationId(
                request.getConversationId());
        if (members == null || members.isEmpty()) {
            throw new BusinessException(ImErrorCode.CONVERSATION_NO_MEMBER);
        }

        // 3. 确定目标用户列表
        Set<Long> targetUserIds = new HashSet<>();
        if (request.getTargetUsers() != null && !request.getTargetUsers().isEmpty()) {
            // 使用指定的目标用户
            targetUserIds.addAll(request.getTargetUsers());
        } else if (request.getReceiverIds() != null && request.getReceiverIds().length > 0) {
            // 兼容旧版：使用receiverIds
            for (Long id : request.getReceiverIds()) {
                targetUserIds.add(id);
            }
        } else {
            // 发送给会话中所有成员（除了发送者）
            for (ImConversationMember member : members) {
                if (!member.getUserId().equals(userId)) {
                    targetUserIds.add(member.getUserId());
                }
            }
        }

        if (targetUserIds.isEmpty()) {
            throw new BusinessException(ImErrorCode.DING_NO_TARGET_USER);
        }

        // 4. 创建DING消息
        ImDingMessage ding = new ImDingMessage();
        ding.setConversationId(request.getConversationId());
        ding.setSenderId(userId);
        ding.setDingType(request.getDingType() != null ? request.getDingType() : DING_TYPE_APP);
        ding.setPriority(request.getPriority() != null ? request.getPriority() : PRIORITY_NORMAL);
        ding.setContent(request.getContent());
        ding.setTargetUsers(JSONUtil.toJsonStr(targetUserIds));
        ding.setSendCount(targetUserIds.size());
        ding.setReadCount(0);

        // 设置过期时间
        if (request.getExpireHours() != null && request.getExpireHours() > 0) {
            ding.setExpireTime(LocalDateTime.now().plusHours(request.getExpireHours()));
        } else {
            ding.setExpireTime(LocalDateTime.now().plusHours(24));
        }

        ding.setStatus(STATUS_ACTIVE);

        // 保存到数据库
        dingMapper.insert(ding);
        log.info("DING消息已保存: dingId={}", ding.getId());

        // 5. 通过WebSocket推送DING消息给目标用户
        broadcastDingMessage(ding, targetUserIds, userId);

        // 6. 转换为VO返回
        return convertToVO(ding, userId);
    }

    @Override
    public List<DingMessageVO> queryDings(DingQueryRequest request, Long userId) {
        log.info("查询DING消息: userId={}, request={}", userId, request);

        // 构建查询条件
        LambdaQueryWrapper<ImDingMessage> wrapper = new LambdaQueryWrapper<>();

        if (request.getConversationId() != null) {
            wrapper.eq(ImDingMessage::getConversationId, request.getConversationId());
        }
        if (request.getSenderId() != null) {
            wrapper.eq(ImDingMessage::getSenderId, request.getSenderId());
        }
        if (request.getDingType() != null) {
            wrapper.eq(ImDingMessage::getDingType, request.getDingType());
        }
        if (request.getPriority() != null) {
            wrapper.eq(ImDingMessage::getPriority, request.getPriority());
        }
        if (request.getStatus() != null) {
            wrapper.eq(ImDingMessage::getStatus, request.getStatus());
        }

        // 只查询激活或已取消的DING（过期的默认不显示）
        wrapper.in(ImDingMessage::getStatus, STATUS_ACTIVE, STATUS_CANCELLED);

        // 按创建时间倒序
        wrapper.orderByDesc(ImDingMessage::getCreateTime);

        // 分页
        Integer pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
        Integer pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        wrapper.last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);

        List<ImDingMessage> dings = dingMapper.selectList(wrapper);

        // 如果只查询未读，过滤已读的
        if (request.getUnreadOnly() != null && request.getUnreadOnly()) {
            List<Long> readDingIds = dingReadMapper.selectReadDingIdsByUserId(
                    dings.stream().map(ImDingMessage::getId).collect(Collectors.toList()), userId);
            dings = dings.stream()
                    .filter(ding -> !readDingIds.contains(ding.getId()))
                    .collect(Collectors.toList());
        }

        // 转换为VO
        return dings.stream()
                .map(ding -> convertToVO(ding, userId))
                .collect(Collectors.toList());
    }

    @Override
    public DingMessageVO getDingDetail(Long dingId, Long userId) {
        ImDingMessage ding = dingMapper.selectById(dingId);
        if (ding == null) {
            throw new BusinessException(ImErrorCode.DING_NOT_FOUND);
        }
        return convertToVO(ding, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long dingId, Long userId) {
        log.info("标记DING已读: dingId={}, userId={}", dingId, userId);

        // 检查DING是否存在
        ImDingMessage ding = dingMapper.selectById(dingId);
        if (ding == null) {
            throw new BusinessException(ImErrorCode.DING_NOT_FOUND);
        }

        // 检查是否已读
        ImDingRead existing = dingReadMapper.selectByDingIdAndUserId(dingId, userId);
        if (existing != null) {
            log.info("DING已读记录已存在，跳过: dingId={}, userId={}", dingId, userId);
            return;
        }

        // 创建已读记录
        ImDingRead readRecord = new ImDingRead();
        readRecord.setDingId(dingId);
        readRecord.setUserId(userId);
        readRecord.setReadTime(LocalDateTime.now());
        readRecord.setDeviceType("WEB");
        dingReadMapper.insert(readRecord);

        // 增加已读人数
        dingMapper.incrementReadCount(dingId);

        log.info("DING已读记录已创建: dingId={}, userId={}", dingId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelDing(Long dingId, Long userId) {
        log.info("取消DING消息: dingId={}, userId={}", dingId, userId);

        ImDingMessage ding = dingMapper.selectById(dingId);
        if (ding == null) {
            throw new BusinessException(ImErrorCode.DING_NOT_FOUND);
        }

        // 只有发送者可以取消
        if (!ding.getSenderId().equals(userId)) {
            throw new BusinessException(ImErrorCode.NO_PERMISSION);
        }

        // 更新状态为已取消
        dingMapper.updateStatus(dingId, STATUS_CANCELLED);

        log.info("DING消息已取消: dingId={}", dingId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return dingMapper.countUnreadByUserId(userId);
    }

    @Override
    public List<Long> getReadUserIds(Long dingId, Long userId) {
        // 获取DING消息验证权限
        ImDingMessage ding = dingMapper.selectById(dingId);
        if (ding == null) {
            throw new BusinessException(ImErrorCode.DING_NOT_FOUND);
        }

        // 获取已读用户列表
        List<ImDingRead> readRecords = dingReadMapper.selectByDingId(dingId);
        return readRecords.stream()
                .map(ImDingRead::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processExpiredDings() {
        log.info("处理过期的DING消息");

        List<ImDingMessage> expiredDings = dingMapper.selectExpiredDings();
        for (ImDingMessage ding : expiredDings) {
            dingMapper.updateStatus(ding.getId(), STATUS_EXPIRED);
            log.info("DING消息已过期: dingId={}", ding.getId());
        }

        log.info("处理过期DING消息完成，共{}条", expiredDings.size());
    }

    /**
     * 广播DING消息给目标用户
     *
     * @param ding         DING消息
     * @param targetUserIds 目标用户ID列表
     * @param senderId     发送者ID
     */
    private void broadcastDingMessage(ImDingMessage ding, Set<Long> targetUserIds, Long senderId) {
        try {
            // 获取发送者信息
            ImUser sender = userMapper.selectImUserById(senderId);

            // 构建WebSocket消息
            DingMessageVO vo = convertToVO(ding, senderId);
            if (sender != null) {
                vo.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
                vo.setSenderAvatar(sender.getAvatar());
            }

            // 通过广播服务发送
            if (broadcastService != null) {
                broadcastService.broadcastDingMessage(vo, targetUserIds);
            }

            log.info("DING消息已推送给{}个用户", targetUserIds.size());
        } catch (Exception e) {
            log.error("广播DING消息失败: dingId={}", ding.getId(), e);
        }
    }

    /**
     * 转换为VO
     *
     * @param ding   DING消息实体
     * @param userId 当前用户ID
     * @return DING消息VO
     */
    private DingMessageVO convertToVO(ImDingMessage ding, Long userId) {
        DingMessageVO vo = new DingMessageVO();
        vo.setId(ding.getId());
        vo.setConversationId(ding.getConversationId());
        vo.setSenderId(ding.getSenderId());
        vo.setDingType(ding.getDingType());
        vo.setPriority(ding.getPriority());
        vo.setContent(ding.getContent());
        vo.setReadCount(ding.getReadCount());
        vo.setSendCount(ding.getSendCount());
        vo.setExpireTime(ding.getExpireTime());
        vo.setStatus(ding.getStatus());
        vo.setCreateTime(ding.getCreateTime());

        // 检查当前用户是否已读
        if (userId != null) {
            ImDingRead readRecord = dingReadMapper.selectByDingIdAndUserId(ding.getId(), userId);
            vo.setIsRead(readRecord != null);
        }

        // 获取发送者信息
        if (ding.getSender() != null) {
            vo.setSenderName(ding.getSender().getNickname());
            vo.setSenderAvatar(ding.getSender().getAvatar());
        } else if (ding.getSenderId() != null) {
            ImUser sender = userMapper.selectImUserById(ding.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
                vo.setSenderAvatar(sender.getAvatar());
            }
        }

        return vo;
    }
}
