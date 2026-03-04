package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImAuditLog;
import com.ruoyi.im.service.ImAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 审计日志控制器
 * 提供后台操作审计页面的查询接口
 * 支持按操作人/模块/时间查询，可查看对象、动作、结果、时间
 *
 * @author ruoyi
 */
@Tag(name = "管理员 - 审计日志", description = "后台审计日志管理接口")
@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ImAuditAdminController {

    private final ImAuditService imAuditService;

    public ImAuditAdminController(ImAuditService imAuditService) {
        this.imAuditService = imAuditService;
    }

    /**
     * 获取审计日志列表
     * 支持按操作人/操作类型/操作结果/时间范围筛选
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param userId 用户ID（可选）
     * @param operationType 操作类型（可选）
     * @param operationResult 操作结果（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    @Operation(summary = "获取审计日志列表", description = "分页查询审计日志，支持按操作人/模块/时间筛选")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "操作结果") @RequestParam(required = false) String operationResult,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        Map<String, Object> result = imAuditService.getAuditLogList(
                pageNum, pageSize, userId, operationType, operationResult, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 获取审计日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    @Operation(summary = "获取审计日志详情", description = "根据日志ID查询详细信息")
    @GetMapping("/{id}")
    public Result<ImAuditLog> getById(
            @Parameter(description = "日志ID") @PathVariable Long id) {
        ImAuditLog log = imAuditService.getAuditLogById(id);
        return Result.success(log);
    }

    /**
     * 获取审计统计信息
     *
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计信息
     */
    @Operation(summary = "获取审计统计信息", description = "获取操作总数、成功/失败数等统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        Map<String, Object> stats = imAuditService.getStatistics(startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 获取操作类型列表
     * 用于前端筛选下拉框
     *
     * @return 操作类型列表
     */
    @Operation(summary = "获取操作类型列表", description = "获取所有可用的操作类型")
    @GetMapping("/operation-types")
    public Result<List<String>> getOperationTypes() {
        List<String> types = Arrays.asList(
                "LOGIN", "LOGOUT", "SEND_MESSAGE", "DELETE_MESSAGE", "RECALL_MESSAGE",
                "CREATE_GROUP", "UPDATE_GROUP", "DELETE_GROUP", "JOIN_GROUP", "LEAVE_GROUP",
                "ADD_FRIEND", "DELETE_FRIEND", "UPDATE_USER", "DELETE_USER",
                "CREATE_CONVERSATION", "DELETE_CONVERSATION", "UPDATE_CONVERSATION"
        );
        return Result.success(types);
    }

    /**
     * 获取目标类型列表
     * 用于前端筛选下拉框
     *
     * @return 目标类型列表
     */
    @Operation(summary = "获取目标类型列表", description = "获取所有可用的目标类型")
    @GetMapping("/target-types")
    public Result<List<String>> getTargetTypes() {
        List<String> types = Arrays.asList(
                "USER", "MESSAGE", "GROUP", "CONVERSATION", "FRIEND", "FILE", "DOCUMENT"
        );
        return Result.success(types);
    }

    /**
     * 删除过期日志
     *
     * @param beforeDate 删除此日期之前的日志
     * @return 删除结果
     */
    @Operation(summary = "删除过期日志", description = "清理指定日期之前的审计日志")
    @DeleteMapping("/clean")
    public Result<Integer> deleteExpiredLogs(
            @Parameter(description = "删除此日期之前的日志") @RequestParam LocalDateTime beforeDate) {
        int count = imAuditService.deleteExpiredLogs(beforeDate);
        return Result.success("删除成功，共删除" + count + "条记录", count);
    }
}
