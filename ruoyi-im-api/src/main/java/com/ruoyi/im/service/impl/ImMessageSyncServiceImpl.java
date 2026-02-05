package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUserSyncPoint;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserSyncPointMapper;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.service.ImMessageSyncService;
import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.vo.message.SyncMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息同步服务实现类
 *
 * 参考野火IM的同步机制实现
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class ImMessageSyncServiceImpl implements ImMessageSyncService {

    @Autowired
    private ImUserSyncPointMapper syncPointMapper;

    @Autowired
    private ImMessageMapper messageMapper;

    @Autowired
    private ImConversationMemberService conversationMemberService;

    @Override
    public ImUserSyncPoint getSyncPoint(Long userId, String deviceId) {
        return syncPointMapper.selectByUserAndDevice(userId, deviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImUserSyncPoint initSyncPoint(Long userId, String deviceId) {
        ImUserSyncPoint syncPoint = syncPointMapper.selectByUserAndDevice(userId, deviceId);
        
        if (syncPoint == null) {
            // 创建新的同步点
            syncPoint = new ImUserSyncPoint();
            syncPoint.setUserId(userId);
            syncPoint.setDeviceId(deviceId);
            syncPoint.setLastSyncTime(System.currentTimeMillis());
            syncPoint.setLastMessageId(0L);
            syncPoint.setCreateTime(LocalDateTime.now());
            syncPoint.setUpdateTime(LocalDateTime.now());
            
            syncPointMapper.insert(syncPoint);
            log.info("初始化用户同步点: userId={}, deviceId={}", userId, deviceId);
        }
        
        return syncPoint;
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SyncMessageResponse syncMessages(Long userId, String deviceId, Long lastSyncTime, Integer limit) {
        // 参数校验
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        if (limit > 500) {
            limit = 500; // 最大500条
        }
        
        SyncMessageResponse response = new SyncMessageResponse();
        
        // 如果没有提供lastSyncTime，尝试从数据库获取
        if (lastSyncTime == null || lastSyncTime <= 0) {
            ImUserSyncPoint syncPoint = syncPointMapper.selectByUserAndDevice(userId, deviceId);
            if (syncPoint != null) {
                lastSyncTime = syncPoint.getLastSyncTime();
            } else {
                lastSyncTime = 0L;
            }
        }
        
        // 将时间戳转换为LocalDateTime
        LocalDateTime syncTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastSyncTime),
            ZoneId.systemDefault()
        );
        
        // 查询用户参与的所有会话ID
        List<Long> conversationIds = conversationMemberService.getUserConversationIds(userId);
        
        if (conversationIds.isEmpty()) {
            response.setMessages(new ArrayList<>());
            response.setHasMore(false);
            response.setNewSyncTime(System.currentTimeMillis());
            response.setTotalCount(0);
            return response;
        }
        
        // 查询同步点之后的消息
        // 查询条件：
        // 1. 消息属于用户的会话
        // 2. 消息创建时间 > lastSyncTime
        // 3. 消息不是用户自己发送的（自己发送的消息已经在本地）
        // 4. 消息未被删除
        LambdaQueryWrapper<ImMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.in(ImMessage::getConversationId, conversationIds)
               .gt(ImMessage::getCreateTime, syncTime)
               .ne(ImMessage::getSenderId, userId) // 排除自己发送的消息
               .eq(ImMessage::getIsDeleted, 0)
               .orderByAsc(ImMessage::getCreateTime)
               .last("LIMIT " + (limit + 1)); // 多查一条用于判断是否还有更多
        
        List<ImMessage> messages = messageMapper.selectList(wrapper);
        
        // 判断是否还有更多
        boolean hasMore = messages.size() > limit;
        if (hasMore) {
            messages = messages.subList(0, limit);
        }
        
        // 转换为VO
        List<ImMessageVO> messageVOs = messages.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        // 计算新的同步点
        Long newSyncTime = lastSyncTime;
        Long lastMessageId = 0L;
        if (!messages.isEmpty()) {
            ImMessage lastMessage = messages.get(messages.size() - 1);
            newSyncTime = lastMessage.getCreateTime()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
            lastMessageId = lastMessage.getId();
        } else {
            newSyncTime = System.currentTimeMillis();
        }
        
        response.setMessages(messageVOs);
        response.setHasMore(hasMore);
        response.setNewSyncTime(newSyncTime);
        response.setLastMessageId(lastMessageId);
        response.setTotalCount(messageVOs.size());
        
        log.info("消息同步: userId={}, deviceId={}, syncTime={}, count={}, hasMore={}", 
                userId, deviceId, lastSyncTime, messageVOs.size(), hasMore);
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSyncPoint(Long userId, String deviceId, Long lastSyncTime, Long lastMessageId) {
        ImUserSyncPoint syncPoint = syncPointMapper.selectByUserAndDevice(userId, deviceId);
        
        if (syncPoint == null) {
            // 创建新的同步点
            syncPoint = new ImUserSyncPoint();
            syncPoint.setUserId(userId);
            syncPoint.setDeviceId(deviceId);
            syncPoint.setLastSyncTime(lastSyncTime);
            syncPoint.setLastMessageId(lastMessageId);
            syncPoint.setCreateTime(LocalDateTime.now());
            syncPoint.setUpdateTime(LocalDateTime.now());
            syncPointMapper.insert(syncPoint);
        } else {
            // 更新同步点
            syncPoint.setLastSyncTime(lastSyncTime);
            syncPoint.setLastMessageId(lastMessageId);
            syncPoint.setUpdateTime(LocalDateTime.now());
            syncPointMapper.updateById(syncPoint);
        }
        
        log.debug("更新同步点: userId={}, deviceId={}, lastSyncTime={}", 
                userId, deviceId, lastSyncTime);
    }

    @Override
    public List<ImUserSyncPoint> getUserSyncPoints(Long userId) {
        return syncPointMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSyncPoint(Long userId, String deviceId) {
        ImUserSyncPoint syncPoint = syncPointMapper.selectByUserAndDevice(userId, deviceId);
        if (syncPoint != null) {
            syncPointMapper.deleteById(syncPoint.getId());
            log.info("删除同步点: userId={}, deviceId={}", userId, deviceId);
        }
    }

    @Override
    public Long getMinSyncTime(Long userId) {
        List<ImUserSyncPoint> syncPoints = syncPointMapper.selectByUserId(userId);
        if (syncPoints.isEmpty()) {
            return 0L;
        }
        
        return syncPoints.stream()
            .mapToLong(ImUserSyncPoint::getLastSyncTime)
            .min()
            .orElse(0L);
    }

    /**
     * 转换为VO
     */
    private ImMessageVO convertToVO(ImMessage message) {
        ImMessageVO vo = new ImMessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }
}
