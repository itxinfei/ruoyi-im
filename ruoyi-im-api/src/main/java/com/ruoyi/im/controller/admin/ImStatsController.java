package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    private final ImStatsService imStatsService;

    /**
     * 构造器注入依赖
     *
     * @param imStatsService 统计服务
     */
    public ImStatsController(ImStatsService imStatsService) {
        this.imStatsService = imStatsService;
    }

    /**
     * 获取系统概览数据
     *
     * @return 概览统计数据
     */
    @Operation(summary = "获取系统概览", description = "获取用户数、群组数、消息数等概览数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = imStatsService.getOverview();
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
            @Parameter(description = "统计天数") @RequestParam(required = false, defaultValue = "7") Integer days) {
        Map<String, Object> stats = imStatsService.getUserActiveStats(days);
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
            @Parameter(description = "统计天数") @RequestParam(required = false, defaultValue = "7") Integer days) {
        Map<String, Object> stats = imStatsService.getGroupActiveStats(days);
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
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = imStatsService.getMessageStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 获取消息类型统计（管理后台）
     *
     * @param params 查询参数
     * @return 消息类型统计数据
     */
    @Operation(summary = "获取消息类型统计", description = "获取不同类型消息的数量统计")
    @GetMapping("/messages/types")
    public Result<Map<String, Object>> getMessageAdminStats(
            @Parameter(description = "查询参数") @RequestParam(required = false) Map<String, Object> params) {
        Map<String, Object> stats = imStatsService.getMessageAdminStats(params);
        return Result.success(stats);
    }

    /**
     * 获取用户角色统计（管理后台）
     *
     * @return 用户角色统计数据
     */
    @Operation(summary = "获取用户角色统计", description = "获取不同角色的用户数量统计")
    @GetMapping("/users/roles")
    public Result<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = imStatsService.getUserStats();
        return Result.success(stats);
    }
}
