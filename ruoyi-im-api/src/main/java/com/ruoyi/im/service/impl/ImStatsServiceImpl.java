package com.ruoyi.im.service.impl;

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
            // 用户统计
            int totalUsers = imUserMapper.countImUsers();
            overview.put("totalUsers", totalUsers);

            // 群组统计
            List<ImGroup> allGroups = imGroupMapper.selectImGroupList(new ImGroup());
            overview.put("totalGroups", allGroups.size());

            // 消息统计
            List<ImMessage> allMessages = imMessageMapper.selectImMessageList(new ImMessage());
            overview.put("totalMessages", allMessages.size());

            // 今日消息数
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            long todayCount = allMessages.stream()
                    .filter(m -> m.getCreateTime() != null && !m.getCreateTime().isBefore(todayStart))
                    .count();
            overview.put("todayMessages", todayCount);

            // 活跃用户（最近7天登录）
            LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
            List<ImUser> allUsers = imUserMapper.selectImUserList(new ImUser());
            long activeUsers = allUsers.stream()
                    .filter(u -> u.getLastOnlineTime() != null && !u.getLastOnlineTime().isBefore(weekStart))
                    .count();
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

            // 活跃用户数（有登录记录）
            List<ImUser> allUsers = imUserMapper.selectImUserList(new ImUser());
            long activeUsers = allUsers.stream()
                    .filter(u -> u.getLastOnlineTime() != null && !u.getLastOnlineTime().isBefore(since))
                    .count();

            // 新增用户数
            long newUsers = allUsers.stream()
                    .filter(u -> u.getCreateTime() != null && !u.getCreateTime().isBefore(since))
                    .count();

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

            // 活跃群组数（通过会话表查询群组类型的会话，且有最近消息的）
            List<ImConversation> allConversations = imConversationMapper.selectImConversationList(new ImConversation());
            long activeGroups = allConversations.stream()
                    .filter(c -> StatusConstants.ConversationType.GROUP.equals(c.getType()))
                    .filter(c -> c.getLastMessageTime() != null && !c.getLastMessageTime().isBefore(since))
                    .count();

            // 新建群组数
            List<ImGroup> allGroups = imGroupMapper.selectImGroupList(new ImGroup());
            long newGroups = allGroups.stream()
                    .filter(g -> g.getCreateTime() != null && !g.getCreateTime().isBefore(since))
                    .count();

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

            // 日期范围内的消息总数
            List<ImMessage> allMessages = imMessageMapper.selectImMessageList(new ImMessage());
            long messageCount = allMessages.stream()
                    .filter(m -> m.getCreateTime() != null)
                    .filter(m -> !m.getCreateTime().isBefore(start) && !m.getCreateTime().isAfter(end))
                    .count();

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

            // 查询指定天数内的消息
            LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(ImMessage::getCreateTime, startTime);

            List<ImMessage> messages = imMessageMapper.selectList(queryWrapper);

            // 总消息数
            stats.put("totalMessages", messages.size());

            // 文本消息数
            long textMessages = messages.stream()
                    .filter(m -> "TEXT".equals(m.getType()))
                    .count();
            stats.put("textMessages", textMessages);

            // 图片消息数
            long imageMessages = messages.stream()
                    .filter(m -> "IMAGE".equals(m.getType()))
                    .count();
            stats.put("imageMessages", imageMessages);

            // 文件消息数
            long fileMessages = messages.stream()
                    .filter(m -> "FILE".equals(m.getType()))
                    .count();
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
            List<ImUser> allUsers = imUserMapper.selectImUserList(new ImUser());

            // 总用户数
            stats.put("total", allUsers.size());

            // 超级管理员数
            long superAdminCount = allUsers.stream()
                    .filter(u -> "SUPER_ADMIN".equals(u.getRole()))
                    .count();
            stats.put("superAdminCount", superAdminCount);

            // 管理员数
            long adminCount = allUsers.stream()
                    .filter(u -> "ADMIN".equals(u.getRole()))
                    .count();
            stats.put("adminCount", adminCount);

            // 普通用户数
            long userCount = allUsers.stream()
                    .filter(u -> "USER".equals(u.getRole()) || u.getRole() == null)
                    .count();
            stats.put("userCount", userCount);

        } catch (Exception e) {
            logger.error("获取用户角色统计失败", e);
            stats.put("error", "统计数据获取失败");
        }

        return stats;
    }
}
