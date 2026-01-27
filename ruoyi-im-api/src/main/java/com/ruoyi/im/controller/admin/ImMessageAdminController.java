package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.vo.message.ImMessageSearchResultVO;
import com.ruoyi.im.vo.message.ImMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员-消息管理控制器
 * 提供消息查询、删除、撤回等管理员功能
 *
 * @author ruoyi
 */
@Tag(name = "管理员-消息管理", description = "管理员消息管理接口")
@RestController
@RequestMapping("/api/admin/messages")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class ImMessageAdminController {

    private final ImMessageService imMessageService;

    /**
     * 构造器注入依赖
     *
     * @param imMessageService 消息服务
     */
    public ImMessageAdminController(ImMessageService imMessageService) {
        this.imMessageService = imMessageService;
    }

    /**
     * 搜索消息列表（分页）
     *
     * @param keyword 搜索关键词
     * @param messageType 消息类型
     * @param senderId 发送者ID
     * @param conversationId 会话ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 消息列表
     */
    @Operation(summary = "搜索消息", description = "管理员搜索消息，支持多条件筛选")
    @GetMapping
    public Result<ImMessageSearchResultVO> search(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "消息类型") @RequestParam(required = false) String messageType,
            @Parameter(description = "发送者ID") @RequestParam(required = false) Long senderId,
            @Parameter(description = "会话ID") @RequestParam(required = false) Long conversationId,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        ImMessageSearchResultVO result = imMessageService.searchMessages(
                conversationId, keyword, messageType, senderId, startTime, endTime,
                pageNum, pageSize, false, false, null);

        return Result.success(result);
    }

    /**
     * 获取消息详情
     *
     * @param id 消息ID
     * @return 消息详情
     */
    @Operation(summary = "获取消息详情", description = "管理员获取指定消息的详细信息")
    @GetMapping("/{id}")
    public Result<ImMessageVO> getById(@Parameter(description = "消息ID") @PathVariable Long id) {
        ImMessageVO message = imMessageService.getMessageById(id);
        if (message == null) {
            return Result.fail("消息不存在");
        }
        return Result.success(message);
    }

    /**
     * 删除消息
     *
     * @param id 消息ID
     * @return 操作结果
     */
    @Operation(summary = "删除消息", description = "管理员删除指定消息")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "消息ID") @PathVariable Long id) {
        imMessageService.adminDeleteMessage(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除消息
     *
     * @param ids 消息ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除消息", description = "管理员批量删除消息")
    @DeleteMapping("/batch")
    public Result<Map<String, Integer>> batchDelete(
            @Parameter(description = "消息ID列表") @RequestBody List<Long> ids) {
        Map<String, Integer> result = imMessageService.adminBatchDeleteMessages(ids);
        return Result.success(result);
    }

    /**
     * 获取消息统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @Operation(summary = "获取消息统计", description = "获取指定时间范围内的消息统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Map<String, Object> stats = imMessageService.getMessageStats(startTime, endTime);
        return Result.success(stats);
    }
}
