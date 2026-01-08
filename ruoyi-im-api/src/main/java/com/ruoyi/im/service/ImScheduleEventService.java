package com.ruoyi.im.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.dto.schedule.ScheduleEventCreateRequest;
import com.ruoyi.im.dto.schedule.ScheduleEventQueryRequest;
import com.ruoyi.im.vo.schedule.ScheduleEventDetailVO;
import com.ruoyi.im.vo.schedule.ScheduleParticipantVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程事件服务
 */
public interface ImScheduleEventService {

    /**
     * 创建日程事件
     *
     * @param request 创建请求
     * @param userId  用户ID
     * @return 日程ID
     */
    Long createEvent(ScheduleEventCreateRequest request, Long userId);

    /**
     * 更新日程事件
     *
     * @param eventId 日程ID
     * @param request 更新请求
     * @param userId   用户ID
     */
    void updateEvent(Long eventId, ScheduleEventCreateRequest request, Long userId);

    /**
     * 删除日程事件
     *
     * @param eventId 日程ID
     * @param userId   用户ID
     */
    void deleteEvent(Long eventId, Long userId);

    /**
     * 获取日程事件详情
     *
     * @param eventId 日程ID
     * @param userId   用户ID
     * @return 日程详情
     */
    ScheduleEventDetailVO getEventDetail(Long eventId, Long userId);

    /**
     * 分页查询日程事件列表
     *
     * @param request 查询请求
     * @param userId  用户ID
     * @return 日程列表
     */
    IPage<ScheduleEventDetailVO> getEventPage(ScheduleEventQueryRequest request, Long userId);

    /**
     * 获取指定时间范围内的日程
     *
     * @param startTime 开始时间
     * @param endTime  结束时间
     * @param userId    用户ID
     * @return 日程列表
     */
    List<ScheduleEventDetailVO> getEventsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Long userId);

    /**
     * 回复参与邀请
     *
     * @param eventId 日程ID
     * @param userId   用户ID
     * @param accepted 是否接受
     */
    void respondToInvite(Long eventId, Long userId, Boolean accepted);

    /**
     * 获取参与人列表
     *
     * @param eventId 日程ID
     * @return 参与人列表
     */
    List<ScheduleParticipantVO> getParticipants(Long eventId);

    /**
     * 取消日程
     *
     * @param eventId 日程ID
     * @param userId   用户ID
     */
    void cancelEvent(Long eventId, Long userId);
}
