package com.ruoyi.im.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.constants.StatusConstants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImScheduleEvent;
import com.ruoyi.im.domain.ImScheduleParticipant;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.schedule.ScheduleEventCreateRequest;
import com.ruoyi.im.dto.schedule.ScheduleEventQueryRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImScheduleEventMapper;
import com.ruoyi.im.mapper.ImScheduleParticipantMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImScheduleEventService;
import com.ruoyi.im.vo.schedule.ScheduleEventDetailVO;
import com.ruoyi.im.vo.schedule.ScheduleParticipantVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日程事件服务实现
 */
@Service
public class ImScheduleEventServiceImpl implements ImScheduleEventService {

    @Autowired
    private ImScheduleEventMapper eventMapper;

    @Autowired
    private ImScheduleParticipantMapper participantMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEvent(ScheduleEventCreateRequest request, Long userId) {
        ImScheduleEvent event = new ImScheduleEvent();
        BeanUtils.copyProperties(request, event);
        event.setUserId(userId);
        event.setIsAllDay(Boolean.TRUE.equals(request.getIsAllDay()) ? 1 : 0);
        event.setStatus("SCHEDULED");
        eventMapper.insert(event);

        // 添加参与人
        if (CollUtil.isNotEmpty(request.getParticipantIds())) {
            for (Long participantId : request.getParticipantIds()) {
                ImUser user = userMapper.selectImUserById(participantId);
                if (user != null && !participantId.equals(userId)) {
                    ImScheduleParticipant participant = new ImScheduleParticipant();
                    participant.setEventId(event.getId());
                    participant.setUserId(participantId);
                    participant.setStatus(StatusConstants.Approval.PENDING);
                    participant.setCreateTime(LocalDateTime.now());
                    participantMapper.insert(participant);
                }
            }
        }

        return event.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEvent(Long eventId, ScheduleEventCreateRequest request, Long userId) {
        ImScheduleEvent event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new BusinessException("日程不存在");
        }
        if (!event.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        BeanUtils.copyProperties(request, event);
        event.setIsAllDay(Boolean.TRUE.equals(request.getIsAllDay()) ? 1 : 0);
        eventMapper.updateById(event);

        // 更新参与人
        List<ImScheduleParticipant> existingParticipants = participantMapper.selectByEventId(eventId);
        for (ImScheduleParticipant p : existingParticipants) {
            participantMapper.deleteById(p.getId());
        }

        if (CollUtil.isNotEmpty(request.getParticipantIds())) {
            for (Long participantId : request.getParticipantIds()) {
                ImUser user = userMapper.selectImUserById(participantId);
                if (user != null && !participantId.equals(userId)) {
                    ImScheduleParticipant participant = new ImScheduleParticipant();
                    participant.setEventId(eventId);
                    participant.setUserId(participantId);
                    participant.setStatus(StatusConstants.Approval.PENDING);
                    participant.setCreateTime(LocalDateTime.now());
                    participantMapper.insert(participant);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEvent(Long eventId, Long userId) {
        ImScheduleEvent event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new BusinessException("日程不存在");
        }
        if (!event.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        eventMapper.deleteById(eventId);
    }

    @Override
    public ScheduleEventDetailVO getEventDetail(Long eventId, Long userId) {
        ImScheduleEvent event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new BusinessException("日程不存在");
        }

        ScheduleEventDetailVO vo = new ScheduleEventDetailVO();
        BeanUtils.copyProperties(event, vo);
        vo.setIsAllDay(event.getIsAllDay() == 1);

        // 获取创建人信息
        ImUser user = userMapper.selectImUserById(event.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        // 获取参与人列表
        List<ImScheduleParticipant> participants = participantMapper.selectByEventId(eventId);
        vo.setParticipants(convertToParticipantVOList(participants));
        vo.setParticipantCount(participants.size());

        // 检查当前用户的参与状态
        if (!event.getUserId().equals(userId)) {
            ImScheduleParticipant myParticipation = participantMapper.selectByEventAndUser(eventId, userId);
            if (myParticipation != null) {
                vo.setParticipantStatus(myParticipation.getStatus());
                vo.setIsAccepted("ACCEPTED".equals(myParticipation.getStatus()));
            }
        }

        // 设置类型名称
        vo.setRecurrenceTypeName(getRecurrenceTypeName(event.getRecurrenceType()));
        vo.setVisibilityName(getVisibilityName(event.getVisibility()));
        vo.setStatusName(getStatusName(event.getStatus()));

        return vo;
    }

    @Override
    public IPage<ScheduleEventDetailVO> getEventPage(ScheduleEventQueryRequest request, Long userId) {
        Page<ImScheduleEvent> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ImScheduleEvent> eventPage = eventMapper.selectEventPage(page, request, userId);

        Page<ScheduleEventDetailVO> resultPage = new Page<>(eventPage.getCurrent(), eventPage.getSize(), eventPage.getTotal());
        List<ScheduleEventDetailVO> voList = eventPage.getRecords().stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public List<ScheduleEventDetailVO> getEventsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Long userId) {
        List<ImScheduleEvent> events = eventMapper.selectByTimeRange(userId, startTime, endTime);
        return events.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void respondToInvite(Long eventId, Long userId, Boolean accepted) {
        ImScheduleEvent event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new BusinessException("日程不存在");
        }

        ImScheduleParticipant participant = participantMapper.selectByEventAndUser(eventId, userId);
        if (participant == null) {
            throw new BusinessException("您不是该日程的参与人");
        }

        participant.setStatus(accepted ? "ACCEPTED" : "DECLINED");
        participant.setResponseTime(LocalDateTime.now());
        participantMapper.updateById(participant);
    }

    @Override
    public List<ScheduleParticipantVO> getParticipants(Long eventId) {
        List<ImScheduleParticipant> participants = participantMapper.selectByEventId(eventId);
        return convertToParticipantVOList(participants);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelEvent(Long eventId, Long userId) {
        ImScheduleEvent event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new BusinessException("日程不存在");
        }
        if (!event.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        event.setStatus(StatusConstants.Task.CANCELLED);
        eventMapper.updateById(event);
    }

    /**
     * 转换为详情VO
     */
    private ScheduleEventDetailVO convertToDetailVO(ImScheduleEvent event) {
        ScheduleEventDetailVO vo = new ScheduleEventDetailVO();
        BeanUtils.copyProperties(event, vo);
        vo.setIsAllDay(event.getIsAllDay() == 1);

        ImUser user = userMapper.selectImUserById(event.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        vo.setParticipantCount(participantMapper.countByEventId(event.getId()));

        vo.setRecurrenceTypeName(getRecurrenceTypeName(event.getRecurrenceType()));
        vo.setVisibilityName(getVisibilityName(event.getVisibility()));
        vo.setStatusName(getStatusName(event.getStatus()));

        return vo;
    }

    /**
     * 转换参与人列表
     */
    private List<ScheduleParticipantVO> convertToParticipantVOList(List<ImScheduleParticipant> participants) {
        if (CollUtil.isEmpty(participants)) {
            return new ArrayList<>();
        }

        return participants.stream()
                .map(p -> {
                    ScheduleParticipantVO vo = new ScheduleParticipantVO();
                    vo.setId(p.getId());
                    vo.setUserId(p.getUserId());
                    vo.setUserName(p.getUserName());
                    vo.setUserAvatar(p.getUserAvatar());
                    vo.setStatus(p.getStatus());
                    vo.setStatusName(getParticipantStatusName(p.getStatus()));
                    vo.setResponseTime(p.getResponseTime());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private String getRecurrenceTypeName(String type) {
        if (type == null) {
            return "不重复";
        } else if ("NONE".equals(type)) {
            return "不重复";
        } else if ("DAILY".equals(type)) {
            return "每天";
        } else if ("WEEKLY".equals(type)) {
            return "每周";
        } else if ("MONTHLY".equals(type)) {
            return "每月";
        } else if ("CUSTOM".equals(type)) {
            return "自定义";
        }
        return type;
    }

    private String getVisibilityName(String visibility) {
        if ("PRIVATE".equals(visibility)) {
            return "私有";
        } else if ("DEPARTMENT".equals(visibility)) {
            return "部门";
        } else if ("PUBLIC".equals(visibility)) {
            return "公开";
        }
        return visibility;
    }

    private String getStatusName(String status) {
        if ("SCHEDULED".equals(status)) {
            return "已安排";
        } else if (StatusConstants.Task.CANCELLED.equals(status)) {
            return "已取消";
        } else if ("COMPLETED".equals(status)) {
            return "已完成";
        }
        return status;
    }

    private String getParticipantStatusName(String status) {
        if (StatusConstants.Approval.PENDING.equals(status)) {
            return "待确认";
        } else if ("ACCEPTED".equals(status)) {
            return "已接受";
        } else if ("DECLINED".equals(status)) {
            return "已拒绝";
        }
        return status;
    }
}
