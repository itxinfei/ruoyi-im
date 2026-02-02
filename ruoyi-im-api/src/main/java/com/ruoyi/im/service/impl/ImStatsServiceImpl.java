package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImStatsService;
import com.ruoyi.im.constants.StatusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现
 *
 * @author ruoyi
 */
@Service
public class ImStatsServiceImpl implements ImStatsService {

    private static final Logger logger = LoggerFactory.getLogger(ImStatsServiceImpl.class);

    private final ImUserMapper imUserMapper;
    private final ImGroupMapper imGroupMapper;
    private final ImMessageMapper imMessageMapper;
    private final ImConversationMapper imConversationMapper;

    /**
     * 构造器注入依赖
     */
    public ImStatsServiceImpl(ImUserMapper imUserMapper,
                              ImGroupMapper imGroupMapper,
                              ImMessageMapper imMessageMapper,
                              ImConversationMapper imConversationMapper) {
        this.imUserMapper = imUserMapper;
        this.imGroupMapper = imGroupMapper;
        this.imMessageMapper = imMessageMapper;
        this.imConversationMapper = imConversationMapper;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        try {
            // 用户统计 - 使用 count 查询
            Long totalUsers = imUserMapper.selectCount(new LambdaQueryWrapper<>());
            overview.put("totalUsers", totalUsers);

            // 群组统计 - 使用 count 查询
            Long totalGroups = imGroupMapper.selectCount(new LambdaQueryWrapper<>());
            overview.put("totalGroups", totalGroups);

            // 消息统计 - 使用 count 查询
            Long totalMessages = imMessageMapper.selectCount(new LambdaQueryWrapper<>());
            overview.put("totalMessages", totalMessages);

            // 今日消息数 - 使用数据库条件查询
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            Long todayCount = imMessageMapper.selectCount(
                new LambdaQueryWrapper<ImMessage>()
                    .ge(ImMessage::getCreateTime, todayStart)
            );
            overview.put("todayMessages", todayCount);

            // 活跃用户（最近7天登录）- 使用数据库条件查询
            LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
            Long activeUsers = imUserMapper.selectCount(
                new LambdaQueryWrapper<ImUser>()
                    .ge(ImUser::getLastOnlineTime, weekStart)
            );
            overview.put("activeUsers", activeUsers);

        } catch (Exception e) {
            logger.error("获取系统概览数据失败", e);
            overview.put("error", "统计数据获取失败");
        }

        return overview;
    }

    @Override
    public Map<String, Object> getUserActiveStats(Integer days) {
        Map<String, Object> stats = new HashMap<>();

        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);

            // 活跃用户数（有登录记录）- 使用 count 查询
            Long activeUsers = imUserMapper.selectCount(
                new LambdaQueryWrapper<ImUser>()
                    .ge(ImUser::getLastOnlineTime, since)
            );

            // 新增用户数 - 使用 count 查询
            Long newUsers = imUserMapper.selectCount(
                new LambdaQueryWrapper<ImUser>()
                    .ge(ImUser::getCreateTime, since)
            );

            stats.put("activeUsers", activeUsers);
            stats.put("newUsers", newUsers);
            stats.put("days", days);

        } catch (Exception e) {
            logger.error("获取用户活跃度统计失败", e);
            stats.put("error", "统计数据获取失败");
        }

        return stats;
    }

    @Override
    public Map<String, Object> getGroupActiveStats(Integer days) {
        Map<String, Object> stats = new HashMap<>();

        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);

            // 活跃群组数（通过会话表查询群组类型的会话，且有最近消息的）- 使用 count 查询
            Long activeGroups = imConversationMapper.selectCount(
                new LambdaQueryWrapper<ImConversation>()
                    .eq(ImConversation::getType, StatusConstants.ConversationType.GROUP)
                    .ge(ImConversation::getLastMessageTime, since)
            );

            // 新建群组数 - 使用 count 查询
            Long newGroups = imGroupMapper.selectCount(
                new LambdaQueryWrapper<ImGroup>()
                    .ge(ImGroup::getCreateTime, since)
            );

            stats.put("activeGroups", activeGroups);
            stats.put("newGroups", newGroups);
            stats.put("days", days);

        } catch (Exception e) {
            logger.error("获取群组活跃度统计失败", e);
            stats.put("error", "统计数据获取失败");
        }

        return stats;
    }

    @Override
    public Map<String, Object> getMessageStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(7);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);

            // 日期范围内的消息总数 - 使用 count 查询
            Long messageCount = imMessageMapper.selectCount(
                new LambdaQueryWrapper<ImMessage>()
                    .ge(ImMessage::getCreateTime, start)
                    .le(ImMessage::getCreateTime, end)
            );

            stats.put("messageCount", messageCount);
            stats.put("startDate", startDate);
            stats.put("endDate", endDate);
            stats.put("days", java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);

        } catch (Exception e) {
            logger.error("获取消息统计失败", e);
            stats.put("error", "统计数据获取失败");
        }

        return stats;
    }

    @Override
    public Map<String, Object> getMessageAdminStats(Map<String, Object> params) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 获取天数参数，默认7天
            Integer days = 7;
            if (params != null && params.get("days") != null) {
                Object daysObj = params.get("days");
                if (daysObj instanceof Number) {
                    days = ((Number) daysObj).intValue();
                } else if (daysObj instanceof String) {
                    try {
                        days = Integer.parseInt((String) daysObj);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // 计算起始时间
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);

            // 使用数据库 count 查询统计各类型消息
            Long totalMessages = imMessageMapper.selectCount(
                new LambdaQueryWrapper<ImMessage>()
                    .ge(ImMessage::getCreateTime, startTime)
            );

            Long textMessages = imMessageMapper.selectCount(
                new LambdaQueryWrapper<ImMessage>()
                    .ge(ImMessage::getCreateTime, startTime)
                    .eq(ImMessage::getType, "TEXT")
            );

            Long imageMessages = imMessageMapper.selectCount(
                new LambdaQueryWrapper<ImMessage>()
                    .ge(ImMessage::getCreateTime, startTime)
                    .eq(ImMessage::getType, "IMAGE")
            );

            Long fileMessages = imMessageMapper.selectCount(
                new LambdaQueryWrapper<ImMessage>()
                    .ge(ImMessage::getCreateTime, startTime)
                    .eq(ImMessage::getType, "FILE")
            );

            stats.put("totalMessages", totalMessages);
            stats.put("textMessages", textMessages);
            stats.put("imageMessages", imageMessages);
            stats.put("fileMessages", fileMessages);

        } catch (Exception e) {
            logger.error("获取消息类型统计失败", e);
            stats.put("error", "统计数据获取失败");
        }

        return stats;
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 总用户数 - 使用 count 查询
            Long total = imUserMapper.selectCount(new LambdaQueryWrapper<>());
            stats.put("total", total);

            // 超级管理员数 - 使用 count 查询
            Long superAdminCount = imUserMapper.selectCount(
                new LambdaQueryWrapper<ImUser>()
                    .eq(ImUser::getRole, "SUPER_ADMIN")
            );
            stats.put("superAdminCount", superAdminCount);

            // 管理员数 - 使用 count 查询
            Long adminCount = imUserMapper.selectCount(
                new LambdaQueryWrapper<ImUser>()
                    .eq(ImUser::getRole, "ADMIN")
            );
            stats.put("adminCount", adminCount);

            // 普通用户数 - 使用 count 查询
            Long userCount = imUserMapper.selectCount(
                new LambdaQueryWrapper<ImUser>()
                    .and(wrapper -> wrapper
                        .eq(ImUser::getRole, "USER")
                        .or()
                        .isNull(ImUser::getRole)
                    )
            );
            stats.put("userCount", userCount);

        } catch (Exception e) {
            logger.error("获取用户角色统计失败", e);
            stats.put("error", "统计数据获取失败");
        }

        return stats;
    }
}
