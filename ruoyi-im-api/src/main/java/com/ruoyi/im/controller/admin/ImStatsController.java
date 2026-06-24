package com.ruoyi.im.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-数据统计控制器
 * 提供系统数据统计、活跃度分析等功能
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ImStatsController {

    private final ImUserMapper imUserMapper;
    private final ImGroupMapper imGroupMapper;
    private final ImMessageMapper imMessageMapper;
    private final ImConversationMapper imConversationMapper;

    /**
     * 构造器注入依赖
     *
     * @param imUserMapper 用户Mapper
     * @param imGroupMapper 群组Mapper
     * @param imMessageMapper 消息Mapper
     * @param imConversationMapper 会话Mapper
     */
    public ImStatsController(ImUserMapper imUserMapper,
                              ImGroupMapper imGroupMapper,
                              ImMessageMapper imMessageMapper,
                              ImConversationMapper imConversationMapper) {
        this.imUserMapper = imUserMapper;
        this.imGroupMapper = imGroupMapper;
        this.imMessageMapper = imMessageMapper;
        this.imConversationMapper = imConversationMapper;
    }

    /**
     * 获取系统概览数据
     *
     * @return 概览统计数据
     */
    
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 用户统计（使用SQL COUNT）
        int totalUsers = imUserMapper.countImUsers();
        overview.put("totalUsers", totalUsers);

        // 群组统计（使用SQL COUNT）
        long totalGroups = imGroupMapper.selectCount(
                new LambdaQueryWrapper<ImGroup>().eq(ImGroup::getIsDeleted, 0));
        overview.put("totalGroups", totalGroups);

        // 消息统计（使用SQL COUNT）
        int totalMessages = imMessageMapper.countSearchResults(
                null, null, null, null, null, null, false, false);
        overview.put("totalMessages", totalMessages);

        // 今日消息数（使用SQL WHERE条件）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        int todayMessages = imMessageMapper.countSearchResults(
                null, null, null, null, todayStart, todayEnd, false, false);
        overview.put("todayMessages", todayMessages);

        // 活跃用户（最近7天登录，使用SQL COUNT）
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        ImUser queryUser = new ImUser();
        queryUser.setLastOnlineTime(weekStart);
        int activeUsers = imUserMapper.selectImUserCount(queryUser);
        overview.put("activeUsers", activeUsers);

        return Result.success(overview);
    }

    /**
     * 获取用户活跃度统计
     *
     * @param days 统计天数，默认7天
     * @return 活跃度数据
     */
    
    @GetMapping("/users/active")
    public Result<Map<String, Object>> getUserActiveStats(
            @RequestParam(required = false, defaultValue = "7") Integer days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        // 活跃用户数（使用SQL COUNT）
        ImUser activeQuery = new ImUser();
        activeQuery.setLastOnlineTime(since);
        long activeUsers = imUserMapper.selectImUserCount(activeQuery);

        // 新增用户数（使用SQL COUNT）
        ImUser newQuery = new ImUser();
        newQuery.setCreateTime(since);
        long newUsers = imUserMapper.selectImUserCount(newQuery);

        Map<String, Object> stats = new HashMap<>();
        stats.put("activeUsers", activeUsers);
        stats.put("newUsers", newUsers);
        stats.put("days", days);

        return Result.success(stats);
    }

    /**
     * 获取群组活跃度统计
     *
     * @param days 统计天数，默认7天
     * @return 活跃度数据
     */
    
    @GetMapping("/groups/active")
    public Result<Map<String, Object>> getGroupActiveStats(
            @RequestParam(required = false, defaultValue = "7") Integer days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        // 活跃群组数（使用SQL COUNT）
        long activeGroups = imConversationMapper.selectCount(
                new LambdaQueryWrapper<ImConversation>()
                        .eq(ImConversation::getType, "GROUP")
                        .isNotNull(ImConversation::getLastMessageTime)
                        .ge(ImConversation::getLastMessageTime, since));

        // 新建群组数（使用SQL COUNT）
        long newGroups = imGroupMapper.selectCount(
                new LambdaQueryWrapper<ImGroup>()
                        .isNotNull(ImGroup::getCreateTime)
                        .ge(ImGroup::getCreateTime, since));

        Map<String, Object> stats = new HashMap<>();
        stats.put("activeGroups", activeGroups);
        stats.put("newGroups", newGroups);
        stats.put("days", days);

        return Result.success(stats);
    }

    /**
     * 获取消息统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 消息统计数据
     */
    
    @GetMapping("/messages")
    public Result<Map<String, Object>> getMessageStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);

        // 日期范围内的消息总数（使用SQL COUNT）
        int messageCount = imMessageMapper.countSearchResults(
                null, null, null, null, start, end, false, false);

        Map<String, Object> stats = new HashMap<>();
        stats.put("messageCount", messageCount);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("days", java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);

        return Result.success(stats);
    }
}
