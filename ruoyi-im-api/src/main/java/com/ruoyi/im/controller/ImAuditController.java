package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.service.ImAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志控制器
 * 提供操作日志查询、统计、导出等功能
 *
 * @author ruoyi
 */
@Tag(name = "审计日志", description = "审计日志查询、统计、导出等接口")
@RestController
@RequestMapping("/api/im/audit")
public class ImAuditController {

    @Autowired
    private ImAuditService imAuditService;

    /**
     * 获取审计日志列表
     * 分页查询审计日志，支持按用户、操作类型、操作结果等条件筛选
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 用户ID（可选）
     * @param operationType 操作类型（可选）
     * @param operationResult 操作结果（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     * @apiNote 支持按时间范围、用户、操作类型等多种条件组合查询
     */
    @Operation(summary = "获取审计日志列表", description = "分页查询审计日志，支持按用户、操作类型、操作结果等条件筛选")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationResult,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        Map<String, Object> result = imAuditService.getAuditLogList(pageNum, pageSize, userId, operationType, operationResult, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 获取审计日志详情
     * 根据日志ID查询详细的操作日志信息
     *
     * @param id 日志ID
     * @return 日志详情
     * @apiNote 包含完整的请求参数和响应数据
     */
    @Operation(summary = "获取审计日志详情", description = "根据日志ID查询详细的操作日志信息")
    @GetMapping("/{id}")
    public Result<ImAuditLog> getById(@PathVariable Long id) {
        ImAuditLog log = imAuditService.getAuditLogById(id);
        return Result.success(log);
    }

    /**
     * 获取审计统计信息
     * 统计指定时间范围内的操作日志数据
     *
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计信息
     * @apiNote 包含总操作数、成功/失败数、各模块操作数等统计
     */
    @Operation(summary = "获取审计统计信息", description = "统计指定时间范围内的操作日志数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        Map<String, Object> stats = imAuditService.getStatistics(startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 获取用户操作日志
     * 查询指定用户的操作日志
     *
     * @param userId 用户ID
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 用户操作日志列表
     * @apiNote 用于查询特定用户在指定时间范围内的所有操作记录
     */
    @Operation(summary = "获取用户操作日志", description = "查询指定用户的操作日志")
    @GetMapping("/user/{userId}")
    public Result<List<ImAuditLog>> getUserLogs(
            @PathVariable Long userId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        List<ImAuditLog> logs = imAuditService.getUserLogs(userId, startTime, endTime);
        return Result.success(logs);
    }

    /**
     * 删除过期日志
     * 删除指定日期之前的审计日志
     *
     * @param beforeDate 删除此日期之前的日志
     * @return 删除结果
     * @apiNote 此操作需要管理员权限，删除后不可恢复
     */
    @Operation(summary = "删除过期日志", description = "删除指定日期之前的审计日志")
    @DeleteMapping("/clean")
    public Result<Integer> deleteExpiredLogs(@RequestParam LocalDateTime beforeDate) {
        int count = imAuditService.deleteExpiredLogs(beforeDate);
        return Result.success("删除成功，共删除" + count + "条记录", count);
    }
}
