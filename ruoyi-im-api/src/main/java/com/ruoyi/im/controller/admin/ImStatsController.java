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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-数据统计控制器
 * 提供系统数据统计、活跃度分析等功能
 *
 * @author ruoyi
 */
@Tag(name = "管理员-数据统计", description = "管理员数据统计接口")
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
    @Operation(summary = "获取系统概览", description = "获取用户数、群组数、消息数等概览数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();

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
        List<ImMessage> todayMessages = imMessageMapper.selectImMessageList(new ImMessage());
        long todayCount = todayMessages.stream()
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

        return Result.success(overview);
    }

    /**
     * 获取用户活跃度统计
     *
     * @param days 统计天数，默认7天
     * @return 活跃度数据
     */
    @Operation(summary = "获取用户活跃度", description = "获取指定天数内的用户活跃度统计")
    @GetMapping("/users/active")
    public Result<Map<String, Object>> getUserActiveStats(
            @RequestParam(required = false, defaultValue = "7") Integer days) {

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
    @Operation(summary = "获取群组活跃度", description = "获取指定天数内的群组活跃度统计")
    @GetMapping("/groups/active")
    public Result<Map<String, Object>> getGroupActiveStats(
            @RequestParam(required = false, defaultValue = "7") Integer days) {

        LocalDateTime since = LocalDateTime.now().minusDays(days);

        // 活跃群组数（通过会话表查询群组类型的会话，且有最近消息的）
        List<ImConversation> allConversations = imConversationMapper.selectImConversationList(new ImConversation());
        long activeGroups = allConversations.stream()
                .filter(c -> "GROUP".equals(c.getType()))
                .filter(c -> c.getLastMessageTime() != null && !c.getLastMessageTime().isBefore(since))
                .count();

        // 新建群组数
        List<ImGroup> allGroups = imGroupMapper.selectImGroupList(new ImGroup());
        long newGroups = allGroups.stream()
                .filter(g -> g.getCreateTime() != null && !g.getCreateTime().isBefore(since))
                .count();

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
    @Operation(summary = "获取消息统计", description = "获取指定日期范围内的消息统计")
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

        // 日期范围内的消息总数
        List<ImMessage> allMessages = imMessageMapper.selectImMessageList(new ImMessage());
        long messageCount = allMessages.stream()
                .filter(m -> m.getCreateTime() != null)
                .filter(m -> !m.getCreateTime().isBefore(start) && !m.getCreateTime().isAfter(end))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("messageCount", messageCount);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("days", java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);

        return Result.success(stats);
    }
}
