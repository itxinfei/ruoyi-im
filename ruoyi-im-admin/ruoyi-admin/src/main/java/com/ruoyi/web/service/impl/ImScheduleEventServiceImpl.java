package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImScheduleEvent;
import com.ruoyi.web.mapper.ImScheduleEventMapper;
import com.ruoyi.web.service.ImScheduleEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 日程事件Service实现
 *
 * @author ruoyi
 */
@Service
public class ImScheduleEventServiceImpl implements ImScheduleEventService {

    @Autowired
    private ImScheduleEventMapper imScheduleEventMapper;

    @Override
    public ImScheduleEvent selectImScheduleEventById(Long id) {
        return imScheduleEventMapper.selectImScheduleEventById(id);
    }

    @Override
    public List<ImScheduleEvent> selectImScheduleEventList(ImScheduleEvent imScheduleEvent) {
        return imScheduleEventMapper.selectImScheduleEventList(imScheduleEvent);
    }

    @Override
    public List<ImScheduleEvent> selectEventsByUserId(Long userId) {
        return imScheduleEventMapper.selectEventsByUserId(userId);
    }

    @Override
    public List<ImScheduleEvent> selectEventsByTimeRange(Long userId, String startTime, String endTime) {
        return imScheduleEventMapper.selectEventsByTimeRange(userId, startTime, endTime);
    }

    @Override
    public int insertImScheduleEvent(ImScheduleEvent imScheduleEvent) {
        return imScheduleEventMapper.insertImScheduleEvent(imScheduleEvent);
    }

    @Override
    public int updateImScheduleEvent(ImScheduleEvent imScheduleEvent) {
        return imScheduleEventMapper.updateImScheduleEvent(imScheduleEvent);
    }

    @Override
    public int deleteImScheduleEventByIds(Long[] ids) {
        return imScheduleEventMapper.deleteImScheduleEventByIds(ids);
    }

    @Override
    public int deleteImScheduleEventById(Long id) {
        return imScheduleEventMapper.deleteImScheduleEventById(id);
    }

    @Override
    public int updateEventStatus(Long id, String status) {
        return imScheduleEventMapper.updateEventStatus(id, status);
    }

    @Override
    public int cancelEvent(Long id) {
        return imScheduleEventMapper.updateEventStatus(id, "CANCELLED");
    }

    @Override
    public int completeEvent(Long id) {
        return imScheduleEventMapper.updateEventStatus(id, "COMPLETED");
    }

    @Override
    public Map<String, Object> getEventStatistics() {
        return imScheduleEventMapper.getEventStatistics();
    }

    @Override
    public List<ImScheduleEvent> selectUpcomingEvents(Long userId, int hours) {
        return imScheduleEventMapper.selectUpcomingEvents(userId, hours);
    }

    @Override
    public List<ImScheduleEvent> selectTodayEvents(Long userId) {
        return imScheduleEventMapper.selectTodayEvents(userId);
    }
}
