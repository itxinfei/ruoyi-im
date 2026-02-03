package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员-数据备份管理控制器
 * 提供数据备份、恢复、导出等管理员功能
 *
 * @author ruoyi
 */
@Tag(name = "管理员-数据备份", description = "管理员数据备份管理接口")
@RestController
@RequestMapping("/api/admin/backups")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ImBackupAdminController {

    private final ImBackupService imBackupService;

    /**
     * 构造器注入依赖
     *
     * @param imBackupService 数据备份服务
     */
    public ImBackupAdminController(ImBackupService imBackupService) {
        this.imBackupService = imBackupService;
    }

    /**
     * 获取备份列表
     * 查询所有数据备份记录
     *
     * @return 备份列表
     */
    @Operation(summary = "获取备份列表", description = "管理员查询所有数据备份记录")
    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        List<Map<String, Object>> backups = imBackupService.getBackupList();
        return Result.success(backups);
    }

    /**
     * 获取备份详情
     * 查询指定备份的详细信息
     *
     * @param id 备份ID
     * @return 备份详情
     */
    @Operation(summary = "获取备份详情", description = "管理员查询指定备份的详细信息")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@Parameter(description = "备份ID") @PathVariable Long id) {
        Map<String, Object> detail = imBackupService.getBackupDetail(id);
        return Result.success(detail);
    }

    /**
     * 创建备份
     * 创建当前数据库的完整备份
     *
     * @param description 备份描述（可选）
     * @return 备份结果
     */
    @Operation(summary = "创建备份", description = "管理员创建当前数据库的完整备份")
    @PostMapping
    public Result<Map<String, Object>> create(
            @Parameter(description = "备份描述") @RequestParam(required = false) String description) {
        Map<String, Object> result = imBackupService.createBackup(description);
        return Result.success("备份创建成功", result);
    }

    /**
     * 恢复备份
     * 从指定备份文件恢复数据
     *
     * @param id 备份ID
     * @return 恢复结果
     */
    @Operation(summary = "恢复备份", description = "管理员从指定备份文件恢复数据，此操作会覆盖当前数据")
    @PostMapping("/{id}/restore")
    public Result<Void> restore(@Parameter(description = "备份ID") @PathVariable Long id) {
        imBackupService.restoreBackup(id);
        return Result.success("恢复成功");
    }

    /**
     * 删除备份
     * 删除指定的备份文件
     *
     * @param id 备份ID
     * @return 删除结果
     */
    @Operation(summary = "删除备份", description = "管理员删除指定的备份文件，删除后不可恢复")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "备份ID") @PathVariable Long id) {
        imBackupService.deleteBackup(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除备份
     * 批量删除指定的备份文件
     *
     * @param ids 备份ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除备份", description = "管理员批量删除指定的备份文件")
    @DeleteMapping("/batch")
    public Result<Integer> batchDelete(@RequestBody List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            imBackupService.deleteBackup(id);
            count++;
        }
        return Result.success("批量删除成功，共删除" + count + "个备份", count);
    }

    /**
     * 获取备份统计信息
     * 获取备份相关的统计数据
     *
     * @return 统计信息
     */
    @Operation(summary = "获取备份统计信息", description = "获取备份总数、总大小、最近备份时间等统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = imBackupService.getBackupStatistics();
        return Result.success(stats);
    }

    /**
     * 导出用户数据
     * 导出指定用户的所有数据（消息、好友、群组等）
     *
     * @param userId 用户ID
     * @return 导出结果
     */
    @Operation(summary = "导出用户数据", description = "管理员导出指定用户的所有数据")
    @GetMapping("/export/user/{userId}")
    public Result<Map<String, Object>> exportUserData(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        Map<String, Object> result = imBackupService.exportUserData(userId);
        return Result.success("导出成功", result);
    }
}
