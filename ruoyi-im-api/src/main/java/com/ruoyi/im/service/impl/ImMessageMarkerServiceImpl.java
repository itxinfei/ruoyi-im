package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageMarker;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImMessageMarkerMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImMessageMarkerService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.vo.marker.ImMessageMarkerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息标记服务实现
 * 提供消息标记、待办提醒、标记查询等功能
 *
 * @author ruoyi
 */
@Service
public class ImMessageMarkerServiceImpl implements ImMessageMarkerService {

    private static final Logger logger = LoggerFactory.getLogger(ImMessageMarkerServiceImpl.class);

    private final ImMessageMarkerMapper messageMarkerMapper;
    private final ImMessageMapper messageMapper;
    private final ImConversationMapper conversationMapper;
    private final ImUserMapper userMapper;
    private final ImWebSocketBroadcastService webSocketBroadcastService;

    /**
     * 构造器注入依赖
     *
     * @param messageMarkerMapper    消息标记Mapper
     * @param messageMapper          消息Mapper
     * @param conversationMapper     会话Mapper
     * @param userMapper             用户Mapper
     * @param webSocketBroadcastService WebSocket广播服务
     */
    public ImMessageMarkerServiceImpl(
            ImMessageMarkerMapper messageMarkerMapper,
            ImMessageMapper messageMapper,
            ImConversationMapper conversationMapper,
            ImUserMapper userMapper,
            ImWebSocketBroadcastService webSocketBroadcastService) {
        this.messageMarkerMapper = messageMarkerMapper;
        this.messageMapper = messageMapper;
        this.conversationMapper = conversationMapper;
        this.userMapper = userMapper;
        this.webSocketBroadcastService = webSocketBroadcastService;
    }

    /**
     * 标记消息
     * 对消息添加标记（FLAG/IMPORTANT）或更新已有标记
     *
     * @param messageId   消息ID
     * @param markerType  标记类型
     * @param color       标记颜色（可选）
     * @param userId      用户ID
     * @return 标记ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long markMessage(Long messageId, String markerType, String color, Long userId) {
        // 检查消息是否存在
        ImMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 检查是否已标记，存在则更新
        ImMessageMarker existMarker = messageMarkerMapper.selectByMessageAndUser(messageId, userId);
        if (existMarker != null) {
            existMarker.setMarkerType(markerType);
            existMarker.setColor(color);
            existMarker.setUpdateTime(LocalDateTime.now());
            messageMarkerMapper.updateById(existMarker);
            return existMarker.getId();
        }

        // 创建新标记
        ImMessageMarker marker = new ImMessageMarker();
        marker.setMessageId(messageId);
        marker.setConversationId(message.getConversationId());
        marker.setUserId(userId);
        marker.setMarkerType(markerType);
        marker.setColor(color);
        marker.setTodoStatus("PENDING");
        marker.setCreateTime(LocalDateTime.now());
        marker.setUpdateTime(LocalDateTime.now());

        messageMarkerMapper.insert(marker);

        logger.info("标记消息成功: messageId={}, markerType={}, userId={}", messageId, markerType, userId);
        return marker.getId();
    }

    /**
     * 取消标记
     * 删除消息的标记，可指定类型或删除全部
     *
     * @param messageId   消息ID
     * @param markerType  标记类型（null则删除所有类型）
     * @param userId      用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unmarkMessage(Long messageId, String markerType, Long userId) {
        messageMarkerMapper.delete(
                new LambdaQueryWrapper<ImMessageMarker>()
                        .eq(ImMessageMarker::getMessageId, messageId)
                        .eq(ImMessageMarker::getUserId, userId)
                        .eq(markerType != null, ImMessageMarker::getMarkerType, markerType)
        );

        logger.info("取消标记成功: messageId={}, markerType={}, userId={}", messageId, markerType, userId);
    }

    /**
     * 设置待办提醒
     * 将消息标记为待办并设置提醒时间
     *
     * @param messageId   消息ID
     * @param remindTime  提醒时间
     * @param remark      备注（可选）
     * @param userId      用户ID
     * @return 待办标记ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long setTodoReminder(Long messageId, LocalDateTime remindTime, String remark, Long userId) {
        // 检查消息是否存在
        ImMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 检查是否已有待办标记，存在则更新
        ImMessageMarker existMarker = messageMarkerMapper.selectByMessageAndUser(messageId, userId);
        if (existMarker != null && "TODO".equals(existMarker.getMarkerType())) {
            existMarker.setRemindTime(remindTime);
            existMarker.setRemark(remark);
            existMarker.setUpdateTime(LocalDateTime.now());
            messageMarkerMapper.updateById(existMarker);
            return existMarker.getId();
        }

        // 创建新待办标记
        ImMessageMarker marker = new ImMessageMarker();
        marker.setMessageId(messageId);
        marker.setConversationId(message.getConversationId());
        marker.setUserId(userId);
        marker.setMarkerType("TODO");
        marker.setRemindTime(remindTime);
        marker.setRemark(remark);
        marker.setTodoStatus("PENDING");
        marker.setCreateTime(LocalDateTime.now());
        marker.setUpdateTime(LocalDateTime.now());

        messageMarkerMapper.insert(marker);

        logger.info("设置待办提醒成功: messageId={}, remindTime={}, userId={}", messageId, remindTime, userId);
        return marker.getId();
    }

    /**
     * 完成待办
     * 将待办标记为已完成
     *
     * @param markerId  标记ID
     * @param userId    用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTodo(Long markerId, Long userId) {
        ImMessageMarker marker = messageMarkerMapper.selectById(markerId);
        if (marker == null) {
            throw new BusinessException("标记不存在");
        }

        if (!marker.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此待办");
        }

        marker.setTodoStatus("DONE");
        marker.setDoneTime(LocalDateTime.now());
        marker.setUpdateTime(LocalDateTime.now());
        messageMarkerMapper.updateById(marker);

        logger.info("完成待办成功: markerId={}", markerId);
    }

    /**
     * 重启待办
     * 将已完成的待办重新设为待办状态
     *
     * @param markerId  标记ID
     * @param userId    用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reopenTodo(Long markerId, Long userId) {
        ImMessageMarker marker = messageMarkerMapper.selectById(markerId);
        if (marker == null) {
            throw new BusinessException("标记不存在");
        }

        if (!marker.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此待办");
        }

        marker.setTodoStatus("PENDING");
        marker.setDoneTime(null);
        marker.setUpdateTime(LocalDateTime.now());
        messageMarkerMapper.updateById(marker);

        logger.info("重启待办成功: markerId={}", markerId);
    }

    /**
     * 获取用户的标记列表
     * 查询指定用户的所有标记或按类型筛选
     *
     * @param userId     用户ID
     * @param markerType 标记类型筛选（可选）
     * @return 标记VO列表，包含消息预览和会话信息
     */
    @Override
    public List<ImMessageMarkerVO> getUserMarkers(Long userId, String markerType) {
        List<ImMessageMarker> markers = messageMarkerMapper.selectUserMarkers(userId, markerType);

        return markers.stream().map(marker -> {
            ImMessageMarkerVO vo = new ImMessageMarkerVO();
            BeanUtils.copyProperties(marker, vo);

            // 获取消息信息
            ImMessage message = messageMapper.selectById(marker.getMessageId());
            if (message != null) {
                String content = message.getContent();
                if (content != null && content.length() > 50) {
                    content = content.substring(0, 50) + "...";
                }
                vo.setMessagePreview(content);
                vo.setSenderId(message.getSenderId());

                ImUser sender = userMapper.selectImUserById(message.getSenderId());
                if (sender != null) {
                    vo.setSenderName(sender.getNickname());
                }
            }

            // 获取会话信息
            ImConversation conversation = conversationMapper.selectById(marker.getConversationId());
            if (conversation != null) {
                vo.setConversationName(conversation.getName());
            }

            // 检查是否过期
            if (marker.getRemindTime() != null) {
                vo.setIsExpired(marker.getRemindTime().isBefore(LocalDateTime.now()));
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取消息的标记列表
     * 查询指定消息的所有用户标记
     *
     * @param messageId 消息ID
     * @return 标记VO列表
     */
    @Override
    public List<ImMessageMarkerVO> getMessageMarkers(Long messageId) {
        List<ImMessageMarker> markers = messageMarkerMapper.selectByMessageId(messageId);

        return markers.stream().map(marker -> {
            ImMessageMarkerVO vo = new ImMessageMarkerVO();
            BeanUtils.copyProperties(marker, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户待办数量
     * 统计指定用户的未完成待办数量
     *
     * @param userId 用户ID
     * @return 待办数量
     */
    @Override
    public Integer getUserTodoCount(Long userId) {
        Integer count = messageMarkerMapper.countUserTodos(userId);
        return count != null ? count : 0;
    }

    /**
     * 处理待办提醒
     * 定时任务调用，处理到达提醒时间的待办
     *
     * @return 处理的待办数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processPendingReminders() {
        List<ImMessageMarker> markers = messageMarkerMapper.selectPendingReminders(LocalDateTime.now());

        int processed = 0;
        for (ImMessageMarker marker : markers) {
            try {
                // TODO: 发送提醒通知 - 需要实现WebSocket推送
                // webSocketBroadcastService.broadcastMessageToConversation(
                //         marker.getConversationId(), marker.getMessageId(), marker.getUserId());
                processed++;
            } catch (Exception e) {
                logger.error("发送待办提醒失败: markerId={}", marker.getId(), e);
            }
        }

        if (processed > 0) {
            logger.info("处理待办提醒完成: count={}", processed);
        }

        return processed;
    }
}
