package com.ruoyi.im.controller.admin;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.mapper.ImMessageMapper;
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

    private final ImMessageMapper imMessageMapper;

    /**
     * 构造器注入依赖
     *
     * @param imMessageMapper 消息 Mapper
     */
    public ImMessageAdminController(ImMessageMapper imMessageMapper) {
        this.imMessageMapper = imMessageMapper;
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

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 搜索消息
        List<ImMessage> messages = imMessageMapper.searchMessages(
                conversationId,
                keyword,
                messageType,
                senderId,
                startTime,
                endTime,
                false,
                false,
                offset,
                pageSize
        );

        // 统计总数
        int total = imMessageMapper.countSearchResults(
                conversationId,
                keyword,
                messageType,
                senderId,
                startTime,
                endTime,
                false,
                false
        );

        // 返回结果
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
        ImMessage message = imMessageMapper.selectImMessageById(id);
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
        ImMessage message = imMessageMapper.selectImMessageById(id);
        if (message == null) {
            return Result.fail("消息不存在");
        }

        imMessageMapper.deleteImMessageById(id);
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
            ImMessage message = imMessageMapper.selectImMessageById(id);
            if (message == null) {
                result.addFailedItem(id, "消息不存在");
            } else {
                imMessageMapper.deleteImMessageById(id);
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

        // 默认统计最近 7 天
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        if (startTime == null) {
            startTime = endTime.minusDays(7);
        }

        // 统计各类型消息数量
        Map<String, Object> stats = new HashMap<>();

        int totalMessages = imMessageMapper.countSearchResults(null, null, null, null, startTime, endTime, false, false);
        stats.put("totalMessages", totalMessages);

        // 统计文本消息
        int textMessages = imMessageMapper.countSearchResults(null, null, "TEXT", null, startTime, endTime, false, false);
        stats.put("textMessages", textMessages);

        // 统计图片消息
        int imageMessages = imMessageMapper.countSearchResults(null, null, "IMAGE", null, startTime, endTime, false, false);
        stats.put("imageMessages", imageMessages);

        // 统计文件消息
        int fileMessages = imMessageMapper.countSearchResults(null, null, "FILE", null, startTime, endTime, false, false);
        stats.put("fileMessages", fileMessages);

        // 统计已撤回消息
        int revokedMessages = imMessageMapper.countSearchResults(null, null, null, null, startTime, endTime, true, false);
        stats.put("revokedMessages", revokedMessages);

        return Result.success(stats);
    }
}

