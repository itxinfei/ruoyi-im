package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.vo.admin.BatchOperationResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 消息管理控制器
 * 提供消息查询、删除、撤回等管理员功能
 *
 * @author ruoyi
 */

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
     * @param senderId 发送者 ID
     * @param conversationId 会话 ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 消息列表
     */
    
    @GetMapping
    public Result<Map<String, Object>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) Long senderId,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        int offset = (pageNum - 1) * pageSize;

        List<ImMessage> messages = imMessageService.adminSearchMessages(
                keyword, messageType, senderId, conversationId, startTime, endTime, offset, pageSize);

        int total = imMessageService.adminCountSearchResults(
                keyword, messageType, senderId, conversationId, startTime, endTime);

        Map<String, Object> data = new HashMap<>();
        data.put("list", messages);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        data.put("pages", (total + pageSize - 1) / pageSize);

        return Result.success(data);
    }

    /**
     * 获取消息详情
     *
     * @param id 消息 ID
     * @return 消息详情
     */
    
    @GetMapping("/{id}")
    public Result<ImMessage> getById(@PathVariable Long id) {
        ImMessage message = imMessageService.adminGetMessageById(id);
        if (message == null) {
            return Result.fail("消息不存在");
        }
        return Result.success(message);
    }

    /**
     * 删除消息
     *
     * @param id 消息 ID
     * @return 操作结果
     */
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ImMessage message = imMessageService.adminGetMessageById(id);
        if (message == null) {
            return Result.fail("消息不存在");
        }
        imMessageService.adminDeleteMessage(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除消息
     *
     * @param ids 消息 ID 列表
     * @return 批量操作结果
     */
    
    @DeleteMapping("/batch")
    public Result<BatchOperationResult> batchDelete(@RequestBody List<Long> ids) {
        BatchOperationResult result = new BatchOperationResult();

        for (Long id : ids) {
            ImMessage message = imMessageService.adminGetMessageById(id);
            if (message == null) {
                result.addFailedItem(id, "消息不存在");
            } else {
                imMessageService.adminDeleteMessage(id);
                result.setSuccessCount(result.getSuccessCount() + 1);
            }
        }

        return Result.success(result);
    }

    /**
     * 获取消息统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (startTime == null) {
            startTime = endTime.minusDays(7);
        }

        Map<String, Object> stats = new HashMap<>();

        int totalMessages = imMessageService.adminCountMessagesByTimeRange(startTime, endTime, null, false);
        stats.put("totalMessages", totalMessages);

        int textMessages = imMessageService.adminCountMessagesByTimeRange(startTime, endTime, "TEXT", false);
        stats.put("textMessages", textMessages);

        int imageMessages = imMessageService.adminCountMessagesByTimeRange(startTime, endTime, "IMAGE", false);
        stats.put("imageMessages", imageMessages);

        int fileMessages = imMessageService.adminCountMessagesByTimeRange(startTime, endTime, "FILE", false);
        stats.put("fileMessages", fileMessages);

        int revokedMessages = imMessageService.adminCountMessagesByTimeRange(startTime, endTime, null, true);
        stats.put("revokedMessages", revokedMessages);

        return Result.success(stats);
    }
}
