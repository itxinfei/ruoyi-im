package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constants.StatusConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.domain.ImVideoMeeting;
import com.ruoyi.im.domain.ImVideoMeetingParticipant;
import com.ruoyi.im.dto.meeting.ImVideoMeetingCreateRequest;
import com.ruoyi.im.dto.meeting.ImVideoMeetingUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.mapper.ImVideoMeetingMapper;
import com.ruoyi.im.mapper.ImVideoMeetingParticipantMapper;
import com.ruoyi.im.service.ImVideoMeetingService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.vo.meeting.ImVideoMeetingDetailVO;
import com.ruoyi.im.vo.meeting.ImVideoMeetingVO;
import com.ruoyi.im.websocket.ImWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.im.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 视频会议服务实现
 *
 * @author ruoyi
 */
@Service
public class ImVideoMeetingServiceImpl implements ImVideoMeetingService {

    private static final Logger log = LoggerFactory.getLogger(ImVideoMeetingServiceImpl.class);

    private static final String MEETING_CACHE_KEY = "im:meeting:";
    private static final String MEETING_USER_PREFIX = "im:meeting_user:";

    private static final String DEFAULT_MEETING_SERVER_URL = "https://meeting.example.com";

    private final ImVideoMeetingMapper meetingMapper;
    private final ImVideoMeetingParticipantMapper participantMapper;
    private final ImUserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ImRedisUtil redisUtil;
    private final ImWebSocketBroadcastService webSocketBroadcastService;
    private final String meetingServerUrl;

    public ImVideoMeetingServiceImpl(ImVideoMeetingMapper meetingMapper,
                                     ImVideoMeetingParticipantMapper participantMapper,
                                     ImUserMapper userMapper,
                                     RedisTemplate<String, Object> redisTemplate,
                                     ImRedisUtil redisUtil,
                                     ImWebSocketBroadcastService webSocketBroadcastService,
                                     @Value("${meeting.server.url:}") String meetingServerUrl) {
        this.meetingMapper = meetingMapper;
        this.participantMapper = participantMapper;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.redisUtil = redisUtil;
        this.webSocketBroadcastService = webSocketBroadcastService;
        this.meetingServerUrl = meetingServerUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMeeting(ImVideoMeetingCreateRequest request, Long userId) {
        // 获取用户信息
        ImUser host = userMapper.selectImUserById(userId);
        if (host == null) {
            throw new BusinessException("用户不存在");
        }

        // 创建会议
        ImVideoMeeting meeting = new ImVideoMeeting();
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setHostId(userId);
        meeting.setHostName(host.getNickname() != null ? host.getNickname() : host.getUsername());
        meeting.setMeetingType(request.getMeetingType() != null ? request.getMeetingType() : StatusConstants.VideoMeetingType.MEETING);
        meeting.setStatus(StatusConstants.Meeting.SCHEDULED);
        meeting.setScheduledStartTime(request.getScheduledStartTime());
        meeting.setScheduledEndTime(request.getScheduledEndTime());
        meeting.setDuration(request.getDuration());
        meeting.setMaxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 9);
        meeting.setCurrentParticipants(1); // 发起者自动加入
        meeting.setRequirePassword(request.getRequirePassword() != null ? request.getRequirePassword() : false);
        meeting.setMeetingPassword(request.getMeetingPassword());
        meeting.setMuteOnJoin(request.getMuteOnJoin() != null ? request.getMuteOnJoin() : false);
        meeting.setAllowScreenShare(request.getAllowScreenShare() != null ? request.getAllowScreenShare() : true);
        meeting.setAllowRecord(request.getAllowRecord() != null ? request.getAllowRecord() : false);
        meeting.setDelFlag(0);
        meeting.setCreateTime(LocalDateTime.now());

        // 生成房间ID和会议链接
        String roomId = "meeting_" + System.currentTimeMillis();
        meeting.setRoomId(roomId);
        meeting.setMeetingLink(generateMeetingLink(roomId));

        meetingMapper.insert(meeting);

        Long meetingId = meeting.getId();

        // 添加发起者作为参与者
        ImVideoMeetingParticipant hostParticipant = new ImVideoMeetingParticipant();
        hostParticipant.setMeetingId(meetingId);
        hostParticipant.setUserId(userId);
        hostParticipant.setUserName(host.getNickname() != null ? host.getNickname() : host.getUsername());
        hostParticipant.setUserAvatar(host.getAvatar());
        hostParticipant.setRole(StatusConstants.ParticipantRole.HOST);
        hostParticipant.setStatus(StatusConstants.ParticipantStatus.JOINED);
        hostParticipant.setJoinTime(LocalDateTime.now());
        hostParticipant.setIsMuted(false);
        hostParticipant.setIsVideoOff(false);
        hostParticipant.setIsSharing(false);
        hostParticipant.setCreateTime(LocalDateTime.now());

        participantMapper.insert(hostParticipant);

        // 邀请其他用户
        if (request.getInvitedUserIds() != null && !request.getInvitedUserIds().isEmpty()) {
            inviteUsers(meetingId, request.getInvitedUserIds(), userId);
        }

        // 缓存会议信息
        cacheMeetingInfo(meeting);

        log.info("创建视频会议: meetingId={}, title={}, host={}", meetingId, meeting.getTitle(), userId);

        return meetingId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMeeting(Long meetingId, ImVideoMeetingUpdateRequest request, Long userId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 只有主持人可以修改会议
        if (!meeting.getHostId().equals(userId)) {
            throw new BusinessException("只有主持人可以修改会议");
        }

        // 已开始或已结束的会议不能修改
        if (StatusConstants.Meeting.IN_PROGRESS.equals(meeting.getStatus())
                || StatusConstants.Meeting.ENDED.equals(meeting.getStatus())) {
            throw new BusinessException("会议已开始或已结束，无法修改");
        }

        // 更新字段
        if (request.getTitle() != null) {
            meeting.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            meeting.setDescription(request.getDescription());
        }
        if (request.getScheduledStartTime() != null) {
            meeting.setScheduledStartTime(LocalDateTime.parse(request.getScheduledStartTime()));
        }
        if (request.getScheduledEndTime() != null) {
            meeting.setScheduledEndTime(LocalDateTime.parse(request.getScheduledEndTime()));
        }
        if (request.getDuration() != null) {
            meeting.setDuration(request.getDuration());
        }
        if (request.getMaxParticipants() != null) {
            meeting.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getRequirePassword() != null) {
            meeting.setRequirePassword(request.getRequirePassword());
        }
        if (request.getMeetingPassword() != null) {
            meeting.setMeetingPassword(request.getMeetingPassword());
        }
        if (request.getMuteOnJoin() != null) {
            meeting.setMuteOnJoin(request.getMuteOnJoin());
        }
        if (request.getAllowScreenShare() != null) {
            meeting.setAllowScreenShare(request.getAllowScreenShare());
        }
        if (request.getAllowRecord() != null) {
            meeting.setAllowRecord(request.getAllowRecord());
        }

        meeting.setUpdateTime(LocalDateTime.now());
        meetingMapper.updateById(meeting);

        cacheMeetingInfo(meeting);

        log.info("更新视频会议: meetingId={}, title={}", meetingId, meeting.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMeeting(Long meetingId, Long userId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 只有主持人可以取消会议
        if (!meeting.getHostId().equals(userId)) {
            throw new BusinessException("只有主持人可以取消会议");
        }

        // 只有预定状态的会议可以取消
        if (!StatusConstants.Meeting.SCHEDULED.equals(meeting.getStatus())) {
            throw new BusinessException("只有预定状态的会议可以取消");
        }

        meeting.setStatus(StatusConstants.Meeting.CANCELLED);
        meeting.setUpdateTime(LocalDateTime.now());
        meetingMapper.updateById(meeting);

        // 清除缓存
        clearMeetingCache(meetingId);

        // 广播会议取消通知给所有参与者
        broadcastMeetingNotification(meetingId, StatusConstants.MeetingEvent.CANCELLED, "会议已被主持人取消", userId);

        log.info("取消视频会议: meetingId={}, title={}", meetingId, meeting.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMeeting(Long meetingId, Long userId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 只有主持人可以删除会议
        if (!meeting.getHostId().equals(userId)) {
            throw new BusinessException("只有主持人可以删除会议");
        }

        // 只有已结束或已取消的会议可以删除
        if (!StatusConstants.Meeting.ENDED.equals(meeting.getStatus())
                && !StatusConstants.Meeting.CANCELLED.equals(meeting.getStatus())) {
            throw new BusinessException("只有已结束或已取消的会议可以删除");
        }

        meeting.setDelFlag(1);
        meetingMapper.updateById(meeting);

        log.info("删除视频会议: meetingId={}, title={}", meetingId, meeting.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startMeeting(Long meetingId, Long userId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        if (!meeting.getHostId().equals(userId)) {
            throw new BusinessException("只有主持人可以开始会议");
        }

        if (!StatusConstants.Meeting.SCHEDULED.equals(meeting.getStatus())) {
            throw new BusinessException("会议状态不正确");
        }

        meeting.setStatus(StatusConstants.Meeting.IN_PROGRESS);
        meeting.setActualStartTime(LocalDateTime.now());
        meeting.setUpdateTime(LocalDateTime.now());
        meetingMapper.updateById(meeting);

        cacheMeetingInfo(meeting);

        // 广播会议开始通知
        broadcastMeetingNotification(meetingId, StatusConstants.MeetingEvent.STARTED, "会议已开始", userId);

        log.info("开始视频会议: meetingId={}, title={}", meetingId, meeting.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endMeeting(Long meetingId, Long userId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        if (!StatusConstants.Meeting.IN_PROGRESS.equals(meeting.getStatus())) {
            return;
        }

        // 计算实际时长
        LocalDateTime endTime = LocalDateTime.now();
        Integer duration = null;
        if (meeting.getActualStartTime() != null) {
            duration = (int) java.time.Duration.between(meeting.getActualStartTime(), endTime).toMinutes();
        }

        meeting.setStatus(StatusConstants.Meeting.ENDED);
        meeting.setActualEndTime(endTime);
        meeting.setDuration(duration);
        meeting.setUpdateTime(LocalDateTime.now());
        meetingMapper.updateById(meeting);

        clearMeetingCache(meetingId);

        // 广播会议结束通知
        broadcastMeetingNotification(meetingId, StatusConstants.MeetingEvent.ENDED, "会议已结束", userId);

        log.info("结束视频会议: meetingId={}, title={}, duration={}分钟", meetingId, meeting.getTitle(), duration);
    }

    @Override
    public ImVideoMeetingDetailVO joinMeeting(Long meetingId, String password, Long userId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 检查会议状态
        if (StatusConstants.Meeting.CANCELLED.equals(meeting.getStatus())) {
            throw new BusinessException("会议已被取消");
        }

        // 检查密码
        if (Boolean.TRUE.equals(meeting.getRequirePassword())) {
            if (password == null || !password.equals(meeting.getMeetingPassword())) {
                throw new BusinessException("会议密码错误");
            }
        }

        // 获取用户信息
        ImUser user = userMapper.selectImUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查或创建参与者记录
        ImVideoMeetingParticipant participant = participantMapper.selectByMeetingAndUser(meetingId, userId);
        if (participant == null) {
            // 检查人数限制
            Integer currentCount = participantMapper.countJoinedByMeetingId(meetingId);
            if (currentCount >= meeting.getMaxParticipants()) {
                throw new BusinessException("会议人数已满");
            }

            // 创建参与者记录
            participant = new ImVideoMeetingParticipant();
            participant.setMeetingId(meetingId);
            participant.setUserId(userId);
            participant.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            participant.setUserAvatar(user.getAvatar());
            participant.setRole(StatusConstants.ParticipantRole.ATTENDEE);
            participant.setStatus(StatusConstants.ParticipantStatus.JOINED);
            participant.setJoinTime(LocalDateTime.now());
            participant.setIsMuted(meeting.getMuteOnJoin());
            participant.setIsVideoOff(false);
            participant.setIsSharing(false);
            participant.setCreateTime(LocalDateTime.now());

            participantMapper.insert(participant);

            // 更新当前参与人数
            meeting.setCurrentParticipants(currentCount + 1);
            meetingMapper.updateById(meeting);
        } else if (StatusConstants.ParticipantStatus.LEFT.equals(participant.getStatus())) {
            // 重新加入
            participant.setStatus(StatusConstants.ParticipantStatus.JOINED);
            participant.setLeaveTime(null);
            participantMapper.updateById(participant);
        }

        cacheMeetingInfo(meeting);

        return formatMeetingDetail(meeting, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveMeeting(Long meetingId, Long userId) {
        ImVideoMeetingParticipant participant = participantMapper.selectByMeetingAndUser(meetingId, userId);
        if (participant == null) {
            return; // 用户不在会议中
        }

        participant.setStatus(StatusConstants.ParticipantStatus.LEFT);
        participant.setLeaveTime(LocalDateTime.now());
        participantMapper.updateById(participant);

        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting != null && StatusConstants.Meeting.IN_PROGRESS.equals(meeting.getStatus())) {
            // 更新参与人数
            Integer currentCount = meeting.getCurrentParticipants();
            if (currentCount != null && currentCount > 0) {
                meeting.setCurrentParticipants(currentCount - 1);
                meetingMapper.updateById(meeting);
            }
        }

        // 如果正在共享屏幕，停止共享
        if (Boolean.TRUE.equals(participant.getIsSharing())) {
            stopScreenShare(meetingId, userId);
        }

        log.info("离开视频会议: meetingId={}, userId={}", meetingId, userId);
    }

    @Override
    public ImVideoMeetingDetailVO getMeetingDetail(Long meetingId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null || meeting.getDelFlag() == 1) {
            return null;
        }

        // 获取参与者列表
        List<ImVideoMeetingParticipant> participants = participantMapper.selectByMeetingId(meetingId);

        return formatMeetingDetail(meeting, null);
    }

    @Override
    public List<ImVideoMeetingVO> getUserMeetings(Long userId, String status) {
        LambdaQueryWrapper<ImVideoMeeting> query = new LambdaQueryWrapper<>();
        query.eq(ImVideoMeeting::getDelFlag, 0);

        // 查询用户参与的会议
        // 这里需要通过参与者表来查询，简化处理直接查询发起的会议
        query.and(wrapper -> wrapper
            .eq(ImVideoMeeting::getHostId, userId)
            .or().exists(
                "SELECT 1 FROM im_video_meeting_participant WHERE meeting_id = im_video_meeting.id AND user_id = {0}".replace("{0}", String.valueOf(userId))
            )
        );

        if (status != null && !status.isEmpty()) {
            query.eq(ImVideoMeeting::getStatus, status);
        }

        query.orderByDesc(ImVideoMeeting::getCreateTime);

        List<ImVideoMeeting> meetings = meetingMapper.selectList(query);

        return meetings.stream()
            .map(this::formatMeetingVO)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<?> getMeetingParticipants(Long meetingId) {
        List<ImVideoMeetingParticipant> participants = participantMapper.selectByMeetingId(meetingId);

        return participants.stream()
            .map(p -> {
                Map<String, Object> info = new HashMap<>();
                info.put("userId", p.getUserId());
                info.put("userName", p.getUserName());
                info.put("userAvatar", p.getUserAvatar());
                info.put("role", p.getRole());
                info.put("status", p.getStatus());
                info.put("isMuted", p.getIsMuted());
                info.put("isVideoOff", p.getIsVideoOff());
                info.put("isSharing", p.getIsSharing());
                return info;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @SuppressWarnings("all")
    @Transactional(rollbackFor = Exception.class)
    public void inviteUsers(Long meetingId, List<Long> userIds, Long inviterId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 获取邀请者信息
        ImUser inviter = userMapper.selectImUserById(inviterId);
        String inviterName = inviter != null ?
            (inviter.getNickname() != null ? inviter.getNickname() : inviter.getUsername()) : "系统";

        // 收集成功邀请的用户ID
        Set<Long> invitedUserIds = new HashSet<>();

        for (Long userId : userIds) {
            // 检查是否已是参与者
            ImVideoMeetingParticipant existing = participantMapper.selectByMeetingAndUser(meetingId, userId);
            if (existing == null) {
                ImUser user = userMapper.selectImUserById(userId);
                if (user == null) {
                    continue;
                }

                ImVideoMeetingParticipant participant = new ImVideoMeetingParticipant();
                participant.setMeetingId(meetingId);
                participant.setUserId(userId);
                participant.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                participant.setUserAvatar(user.getAvatar());
                participant.setRole(StatusConstants.ParticipantRole.ATTENDEE);
                participant.setStatus(StatusConstants.ParticipantStatus.INVITED);
                participant.setCreateTime(LocalDateTime.now());

                participantMapper.insert(participant);
                invitedUserIds.add(userId);

                log.info("会议邀请: meetingId={}, invited={}, inviter={}", meetingId, userId, inviterName);
            }
        }

        // 通过WebSocket发送会议邀请通知
        if (!invitedUserIds.isEmpty()) {
            webSocketBroadcastService.broadcastMeetingInvitation(meetingId, invitedUserIds, inviterId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeParticipant(Long meetingId, Long userId, Long operatorId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 只有主持人可以移除参与者
        if (!meeting.getHostId().equals(operatorId)) {
            throw new BusinessException("只有主持人可以移除参与者");
        }

        ImVideoMeetingParticipant participant = participantMapper.selectByMeetingAndUser(meetingId, userId);
        if (participant == null) {
            throw new BusinessException("用户不在会议中");
        }

        // 不能移除主持人
        if (StatusConstants.ParticipantRole.HOST.equals(participant.getRole())) {
            throw new BusinessException("不能移除主持人");
        }

        participant.setStatus(StatusConstants.ParticipantStatus.LEFT);
        participant.setLeaveTime(LocalDateTime.now());
        participantMapper.updateById(participant);

        // 更新参与人数
        Integer currentCount = meeting.getCurrentParticipants();
        if (currentCount != null && currentCount > 0) {
            meeting.setCurrentParticipants(currentCount - 1);
            meetingMapper.updateById(meeting);
        }

        cacheMeetingInfo(meeting);

        log.info("移除会议参与者: meetingId={}, userId={}", meetingId, userId);
    }

    @Override
    public void muteParticipant(Long meetingId, Long userId, Boolean muted) {
        ImVideoMeetingParticipant participant = participantMapper.selectByMeetingAndUser(meetingId, userId);
        if (participant == null) {
            throw new BusinessException("用户不在会议中");
        }

        participant.setIsMuted(muted);
        participantMapper.updateById(participant);

        log.info("设置静音状态: meetingId={}, userId={}, muted={}", meetingId, userId, muted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startScreenShare(Long meetingId, Long userId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        if (!Boolean.TRUE.equals(meeting.getAllowScreenShare())) {
            throw new BusinessException("该会议不允许屏幕共享");
        }

        ImVideoMeetingParticipant participant = participantMapper.selectByMeetingAndUser(meetingId, userId);
        if (participant == null) {
            throw new BusinessException("用户不在会议中");
        }

        // 先停止其他人的屏幕共享
        participantMapper.updateScreenShareStatus(null, false);

        participant.setIsSharing(true);
        participantMapper.updateById(participant);

        // 广播屏幕共享事件
        broadcastScreenShareEvent(meetingId, userId, true);

        log.info("开始屏幕共享: meetingId={}, userId={}", meetingId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopScreenShare(Long meetingId, Long userId) {
        ImVideoMeetingParticipant participant = participantMapper.selectByMeetingAndUser(meetingId, userId);
        if (participant == null) {
            return; // 用户不在会议中
        }

        if (Boolean.TRUE.equals(participant.getIsSharing())) {
            participant.setIsSharing(false);
            participantMapper.updateById(participant);

            // 广播屏幕共享事件
            broadcastScreenShareEvent(meetingId, userId, false);

            log.info("停止屏幕共享: meetingId={}, userId={}", meetingId, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeHost(Long meetingId, Long userId, Long operatorId) {
        ImVideoMeeting meeting = getMeetingEntity(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 只有当前主持人可以转移
        if (!meeting.getHostId().equals(operatorId)) {
            throw new BusinessException("只有主持人可以转移权限");
        }

        ImVideoMeetingParticipant participant = participantMapper.selectByMeetingAndUser(meetingId, userId);
        if (participant == null) {
            throw new BusinessException("用户不在会议中");
        }

        // 更新旧主持人角色
        ImVideoMeetingParticipant oldHost = participantMapper.selectByMeetingAndUser(meetingId, operatorId);
        if (oldHost != null) {
            oldHost.setRole(StatusConstants.ParticipantRole.ATTENDEE);
            participantMapper.updateById(oldHost);
        }

        // 更新新主持人
        participant.setRole(StatusConstants.ParticipantRole.HOST);
        participantMapper.updateById(participant);

        // 更新会议表
        meeting.setHostId(userId);
        ImUser newUser = userMapper.selectImUserById(userId);
        if (newUser != null) {
            meeting.setHostName(newUser.getNickname() != null ? newUser.getNickname() : newUser.getUsername());
        }
        meetingMapper.updateById(meeting);

        cacheMeetingInfo(meeting);

        log.info("转移主持人: meetingId={}, oldHost={}, newHost={}", meetingId, operatorId, userId);
    }

    // ==================== 私有方法 ====================

    /**
     * 获取会议实体
     */
    private ImVideoMeeting getMeetingEntity(Long meetingId) {
        String cacheKey = MEETING_CACHE_KEY + meetingId;
        ImVideoMeeting meeting = (ImVideoMeeting) redisTemplate.opsForValue().get(cacheKey);
        if (meeting == null) {
            meeting = meetingMapper.selectById(meetingId);
        }
        return meeting;
    }

    /**
     * 缓存会议信息
     */
    private void cacheMeetingInfo(ImVideoMeeting meeting) {
        if (meeting == null) {
            return;
        }
        String cacheKey = MEETING_CACHE_KEY + meeting.getId();
        // 缓存1小时
        redisTemplate.opsForValue().set(cacheKey, meeting, 3600, TimeUnit.SECONDS);
    }

    /**
     * 清除会议缓存
     */
    private void clearMeetingCache(Long meetingId) {
        String cacheKey = MEETING_CACHE_KEY + meetingId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 广播会议通知
     */
    private void broadcastMeetingNotification(Long meetingId, String eventType, String message, Long operatorId) {
        try {
            // 获取会议参与者列表
            List<ImVideoMeetingParticipant> participants = participantMapper.selectList(
                    new LambdaQueryWrapper<ImVideoMeetingParticipant>()
                            .eq(ImVideoMeetingParticipant::getMeetingId, meetingId)
            );

            if (participants == null || participants.isEmpty()) {
                return;
            }

            // 构建会议通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "meeting_notification");
            notification.put("meetingId", meetingId);
            notification.put("eventType", eventType);
            notification.put("message", message);
            notification.put("operatorId", operatorId);
            notification.put("timestamp", System.currentTimeMillis());

            String messageJson = new ObjectMapper().writeValueAsString(notification);

            // 发送给所有参与者
            for (ImVideoMeetingParticipant participant : participants) {
                try {
                    webSocketBroadcastService.broadcastOnlineStatus(participant.getUserId(), true);
                } catch (Exception e) {
                    log.error("发送会议通知给用户失败: userId={}", participant.getUserId(), e);
                }
            }

            log.info("会议通知已推送: meetingId={}, type={}, participants={}", meetingId, eventType, participants.size());
        } catch (Exception e) {
            log.error("广播会议通知失败: meetingId={}", meetingId, e);
        }
    }

    /**
     * 广播屏幕共享事件
     */
    private void broadcastScreenShareEvent(Long meetingId, Long userId, Boolean isSharing) {
        try {
            // 获取会议参与者列表
            List<ImVideoMeetingParticipant> participants = participantMapper.selectList(
                    new LambdaQueryWrapper<ImVideoMeetingParticipant>()
                            .eq(ImVideoMeetingParticipant::getMeetingId, meetingId)
            );

            if (participants == null || participants.isEmpty()) {
                return;
            }

            // 构建屏幕共享通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "screen_share");
            notification.put("meetingId", meetingId);
            notification.put("userId", userId);
            notification.put("isSharing", isSharing);
            notification.put("timestamp", System.currentTimeMillis());

            String messageJson = new ObjectMapper().writeValueAsString(notification);

            // 广播给所有在线的会议参与者
            Map<Long, javax.websocket.Session> onlineUsers = ImWebSocketEndpoint.getOnlineUsers();
            for (ImVideoMeetingParticipant participant : participants) {
                javax.websocket.Session session = onlineUsers.get(participant.getUserId());
                if (session != null && session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(messageJson);
                    } catch (Exception e) {
                        log.error("发送屏幕共享通知失败: userId={}", participant.getUserId(), e);
                    }
                }
            }

            log.info("屏幕共享事件已推送: meetingId={}, userId={}, sharing={}", meetingId, userId, isSharing);
        } catch (Exception e) {
            log.error("广播屏幕共享事件失败: meetingId={}", meetingId, e);
        }
    }

    /**
     * 生成会议链接
     */
    private String generateMeetingLink(String roomId) {
        String serverUrl = (meetingServerUrl != null && !meetingServerUrl.isEmpty())
                ? meetingServerUrl
                : DEFAULT_MEETING_SERVER_URL;
        return serverUrl + "/join?room=" + roomId;
    }

    /**
     * 格式化会议VO
     */
    private ImVideoMeetingVO formatMeetingVO(ImVideoMeeting meeting) {
        if (meeting == null) {
            return null;
        }

        ImVideoMeetingVO vo = new ImVideoMeetingVO();
        BeanConvertUtil.copyProperties(meeting, vo);

        // 设置发起人头像
        ImUser host = userMapper.selectImUserById(meeting.getHostId());
        if (host != null) {
            vo.setHostAvatar(host.getAvatar());
        }

        return vo;
    }

    /**
     * 格式化会议详情
     */
    private ImVideoMeetingDetailVO formatMeetingDetail(ImVideoMeeting meeting, Long currentUserId) {
        if (meeting == null) {
            return null;
        }

        ImVideoMeetingDetailVO vo = new ImVideoMeetingDetailVO();
        BeanConvertUtil.copyProperties(meeting, vo);

        // 设置发起人头像
        ImUser host = userMapper.selectImUserById(meeting.getHostId());
        if (host != null) {
            vo.setHostAvatar(host.getAvatar());
        }

        // 判断是否为主持人
        if (currentUserId != null) {
            vo.setIsHost(meeting.getHostId().equals(currentUserId));
        }

        // 获取参与者列表
        List<ImVideoMeetingParticipant> participants = participantMapper.selectByMeetingId(meeting.getId());
        if (participants != null) {
            List<ImVideoMeetingDetailVO.ParticipantInfo> participantInfos = participants.stream()
                .map(p -> {
                    ImVideoMeetingDetailVO.ParticipantInfo info = new ImVideoMeetingDetailVO.ParticipantInfo();
                    info.setUserId(p.getUserId());
                    info.setUserName(p.getUserName());
                    info.setUserAvatar(p.getUserAvatar());
                    info.setRole(p.getRole());
                    info.setStatus(p.getStatus());
                    info.setIsMuted(p.getIsMuted());
                    info.setIsVideoOff(p.getIsVideoOff());
                    info.setIsSharing(p.getIsSharing());
                    return info;
                })
                .collect(Collectors.toList());
            vo.setParticipants(participantInfos);

            // 设置当前屏幕共享者
            participants.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsSharing()))
                .findFirst()
                .ifPresent(p -> vo.setScreenSharerId(p.getUserId()));
        }

        return vo;
    }
}
