package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImVideoCall;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImVideoCallMapper;
import com.ruoyi.im.service.ImVideoCallService;
import com.ruoyi.im.utils.ImRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 视频通话服务实现
 *
 * @author ruoyi
 */
@Service
public class ImVideoCallServiceImpl implements ImVideoCallService {

    private static final Logger log = LoggerFactory.getLogger(ImVideoCallServiceImpl.class);

    private static final String CALL_CACHE_KEY = "im:video_call:";
    private static final String USER_CALL_KEY = "im:user_call:";
    private static final int CALL_TIMEOUT_SECONDS = 60; // 60秒超时

    @Autowired
    private ImVideoCallMapper videoCallMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Long initiateCall(Long callerId, Long calleeId, Long conversationId, String callType) {
        // 检查接收者是否在线
        String onlineKey = "im:online:" + calleeId;
        Boolean isOnline = redisTemplate.hasKey(onlineKey);

        if (Boolean.FALSE.equals(isOnline)) {
            throw new BusinessException("对方不在线");
        }

        // 检查接收者是否正在通话中
        String userCallKey = USER_CALL_KEY + calleeId;
        Boolean isInCall = redisTemplate.hasKey(userCallKey);
        if (Boolean.TRUE.equals(isInCall)) {
            throw new BusinessException("对方正在通话中");
        }

        // 创建通话记录
        ImVideoCall call = new ImVideoCall();
        call.setCallerId(callerId);
        call.setCalleeId(calleeId);
        call.setConversationId(conversationId);
        call.setCallType(callType);
        call.setStatus("CALLING");
        call.setStartTime(LocalDateTime.now());

        videoCallMapper.insertImVideoCall(call);

        // 缓存通话信息
        String callKey = CALL_CACHE_KEY + call.getId();
        redisTemplate.opsForValue().set(callKey, call, CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 设置用户通话状态
        redisTemplate.opsForValue().set(USER_CALL_KEY + callerId, call.getId(), CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        log.info("发起通话: callId={}, caller={}, callee={}, type={}", call.getId(), callerId, calleeId, callType);

        return call.getId();
    }

    @Override
    public void acceptCall(Long callId, Long userId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            throw new BusinessException("通话不存在或已过期");
        }

        // 只有接收者可以接听
        if (!call.getCalleeId().equals(userId)) {
            throw new BusinessException("无权限接听此通话");
        }

        if (!"CALLING".equals(call.getStatus())) {
            throw new BusinessException("通话状态不正确");
        }

        // 更新状态
        call.setStatus("CONNECTED");
        call.setStartTime(LocalDateTime.now());
        videoCallMapper.updateImVideoCall(call);

        // 缓存通话信息（延长缓存时间到1小时）
        String callKey = CALL_CACHE_KEY + callId;
        redisTemplate.opsForValue().set(callKey, call, 3600, TimeUnit.SECONDS);

        // 设置双方用户通话状态
        redisTemplate.opsForValue().set(USER_CALL_KEY + call.getCallerId(), callId, 3600, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(USER_CALL_KEY + call.getCalleeId(), callId, 3600, TimeUnit.SECONDS);

        log.info("接听通话: callId={}, user={}", callId, userId);
    }

    @Override
    public void rejectCall(Long callId, Long userId, String reason) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            throw new BusinessException("通话不存在或已过期");
        }

        // 只有接收者可以拒绝
        if (!call.getCalleeId().equals(userId)) {
            throw new BusinessException("无权限拒绝此通话");
        }

        if (!"CALLING".equals(call.getStatus())) {
            throw new BusinessException("通话状态不正确");
        }

        // 更新状态
        call.setStatus("REJECTED");
        call.setRejectReason(reason);
        call.setEndTime(LocalDateTime.now());
        videoCallMapper.updateImVideoCall(call);

        // 清除通话状态
        clearCallState(callId);

        log.info("拒绝通话: callId={}, user={}, reason={}", callId, userId, reason);
    }

    @Override
    public void endCall(Long callId, Long userId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            throw new BusinessException("通话不存在或已过期");
        }

        // 只有通话参与者可以结束
        if (!call.getCallerId().equals(userId) && !call.getCalleeId().equals(userId)) {
            throw new BusinessException("无权限结束此通话");
        }

        if ("ENDED".equals(call.getStatus()) || "REJECTED".equals(call.getStatus()) || "TIMEOUT".equals(call.getStatus())) {
            return; // 已经结束
        }

        // 计算通话时长
        LocalDateTime endTime = LocalDateTime.now();
        Integer duration = null;
        if (call.getStartTime() != null) {
            duration = (int) Duration.between(call.getStartTime(), endTime).getSeconds();
        }

        // 更新状态
        call.setStatus("ENDED");
        call.setEndTime(endTime);
        call.setDuration(duration);
        videoCallMapper.updateImVideoCall(call);

        // 清除通话状态
        clearCallState(callId);

        log.info("结束通话: callId={}, user={}, duration={}s", callId, userId, duration);
    }

    @Override
    public Object getCallInfo(Long callId) {
        String callKey = CALL_CACHE_KEY + callId;
        ImVideoCall call = (ImVideoCall) redisTemplate.opsForValue().get(callKey);
        if (call == null) {
            call = videoCallMapper.selectImVideoCallById(callId);
        }
        return formatCallInfo(call);
    }

    @Override
    public Object getUserActiveCall(Long userId) {
        String userCallKey = USER_CALL_KEY + userId;
        Object callIdObj = redisTemplate.opsForValue().get(userCallKey);
        if (callIdObj == null) {
            return null;
        }

        Long callId = Long.valueOf(callIdObj.toString());
        return getCallInfo(callId);
    }

    @Override
    public void timeoutCall(Long callId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            return;
        }

        if (!"CALLING".equals(call.getStatus())) {
            return;
        }

        // 更新状态
        call.setStatus("TIMEOUT");
        call.setEndTime(LocalDateTime.now());
        videoCallMapper.updateImVideoCall(call);

        // 清除通话状态
        clearCallState(callId);

        log.info("通话超时: callId={}", callId);
    }

    /**
     * 获取通话实体
     */
    private ImVideoCall getCallEntity(Long callId) {
        String callKey = CALL_CACHE_KEY + callId;
        ImVideoCall call = (ImVideoCall) redisTemplate.opsForValue().get(callKey);
        if (call == null) {
            call = videoCallMapper.selectImVideoCallById(callId);
            if (call != null && "CALLING".equals(call.getStatus())) {
                // 如果数据库中还在呼叫中，说明可能缓存丢失了，恢复缓存
                redisTemplate.opsForValue().set(callKey, call, CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        }
        return call;
    }

    /**
     * 清除通话状态
     */
    private void clearCallState(Long callId) {
        ImVideoCall call = getCallEntity(callId);
        if (call == null) {
            return;
        }

        String callKey = CALL_CACHE_KEY + callId;
        redisTemplate.delete(callKey);

        if (call.getCallerId() != null) {
            redisTemplate.delete(USER_CALL_KEY + call.getCallerId());
        }
        if (call.getCalleeId() != null) {
            redisTemplate.delete(USER_CALL_KEY + call.getCalleeId());
        }
    }

    /**
     * 格式化通话信息
     */
    private Map<String, Object> formatCallInfo(ImVideoCall call) {
        if (call == null) {
            return null;
        }

        Map<String, Object> info = new HashMap<>();
        info.put("callId", call.getId());
        info.put("callerId", call.getCallerId());
        info.put("calleeId", call.getCalleeId());
        info.put("conversationId", call.getConversationId());
        info.put("callType", call.getCallType());
        info.put("status", call.getStatus());
        info.put("startTime", call.getStartTime());
        info.put("endTime", call.getEndTime());
        info.put("duration", call.getDuration());
        info.put("rejectReason", call.getRejectReason());

        return info;
    }

    @Override
    public List<?> getCallHistory(Long userId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100; // 最多返回100条
        }

        List<ImVideoCall> calls = videoCallMapper.selectCallsByUserId(userId, limit);
        return calls.stream()
                .map(this::formatCallInfo)
                .collect(Collectors.toList());
    }
}
