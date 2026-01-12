package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImNoticeService;
import com.ruoyi.im.service.ImTodoItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台控制器
 * 提供工作台相关功能，包括待办事项、数据概览等
 *
 * @author ruoyi
 */
@Tag(name = "工作台管理", description = "工作台数据概览、待办事项管理等接口")
@RestController
@RequestMapping("/api/im/workbench")
public class ImWorkbenchController {

    @Autowired
    private ImTodoItemService todoItemService;

    @Autowired
    private ImMessageService messageService;

    @Autowired
    private ImConversationService conversationService;

    @Autowired(required = false)
    private ImNoticeService noticeService;

    /**
     * 获取工作台数据概览
     * 返回用户相关的各种统计数据
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 工作台概览数据
     */
    @Operation(summary = "获取工作台数据概览", description = "获取用户相关的统计数据，如待办数量、消息数量等")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        Map<String, Object> overview = new HashMap<>();

        // 待办数量
        int todoCount = todoItemService.getUncompletedCount(userId);
        overview.put("todoCount", todoCount);

        // 未读消息数量
        int unreadMessageCount = 0;
        try {
            unreadMessageCount = conversationService.getTotalUnreadCount(userId);
        } catch (Exception e) {
            // 如果获取失败，使用0
        }
        overview.put("unreadMessageCount", unreadMessageCount);

        // 今日消息数量
        int todayMessageCount = 0;
        try {
            todayMessageCount = messageService.getTodayMessageCount(userId);
        } catch (Exception e) {
            // 如果获取失败，使用0
        }
        overview.put("todayMessageCount", todayMessageCount);

        // 会话数量
        int conversationCount = 0;
        try {
            conversationCount = conversationService.getUserConversationCount(userId);
        } catch (Exception e) {
            // 如果获取失败，使用0
        }
        overview.put("conversationCount", conversationCount);

        // 未读通知数量
        int noticeCount = 0;
        if (noticeService != null) {
            try {
                noticeCount = noticeService.getUnreadCount(userId);
            } catch (Exception e) {
                // 如果获取失败，使用0
            }
        }
        overview.put("noticeCount", noticeCount);

        // 审批数量（待办事项中类型为APPROVAL的数量）
        int approvalCount = 0;
        try {
            approvalCount = todoItemService.getUncompletedCountByType(userId, "APPROVAL");
        } catch (Exception e) {
            // 如果获取失败，使用0
        }
        overview.put("approvalCount", approvalCount);

        // DING消息未读数量
        int dingCount = 0;
        try {
            dingCount = todoItemService.getUncompletedCountByType(userId, "DING");
        } catch (Exception e) {
            // 如果获取失败，使用0
        }
        overview.put("dingCount", dingCount);

        // 在线状态
        overview.put("isOnline", com.ruoyi.im.websocket.ImWebSocketEndpoint.isUserOnline(userId));

        return Result.success(overview);
    }

    /**
     * 获取待办列表
     * 返回用户的待办事项列表
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 待办事项列表
     */
    @Operation(summary = "获取待办列表", description = "获取用户的待办事项列表")
    @GetMapping("/todos")
    public Result<List<ImTodoItem>> getTodos(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        List<ImTodoItem> todos = todoItemService.getUserTodos(userId);
        return Result.success(todos);
    }

    /**
     * 创建待办
     * 创建新的待办事项
     *
     * @param title 待办标题
     * @param description 待办描述
     * @param type 待办类型
     * @param relatedId 关联ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 创建结果，包含待办ID
     */
    @Operation(summary = "创建待办", description = "创建新的待办事项")
    @PostMapping("/todos")
    public Result<Long> createTodo(@RequestParam String title,
                                   @RequestParam(required = false) String description,
                                   @RequestParam(required = false, defaultValue = "TASK") String type,
                                   @RequestParam(required = false) Long relatedId,
                                   @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        Long todoId = todoItemService.createTodo(title, description, type, relatedId, userId);
        return Result.success("创建成功", todoId);
    }

    /**
     * 完成待办
     * 将待办事项标记为已完成
     *
     * @param id 待办ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "完成待办", description = "将待办事项标记为已完成")
    @PutMapping("/todos/{id}/complete")
    public Result<Void> completeTodo(@PathVariable Long id,
                                    @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        todoItemService.markAsCompleted(id, userId);
        return Result.success("已完成");
    }

    /**
     * 删除待办
     * 删除指定的待办事项
     *
     * @param id 待办ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "删除待办", description = "删除指定的待办事项")
    @DeleteMapping("/todos/{id}")
    public Result<Void> deleteTodo(@PathVariable Long id,
                                  @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        todoItemService.deleteTodo(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 更新待办
     * 更新待办事项的标题和描述
     *
     * @param id 待办ID
     * @param title 待办标题
     * @param description 待办描述
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "更新待办", description = "更新待办事项的标题和描述")
    @PutMapping("/todos/{id}")
    public Result<Void> updateTodo(@PathVariable Long id,
                                  @RequestParam String title,
                                  @RequestParam(required = false) String description,
                                  @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        todoItemService.updateTodo(id, title, description, userId);
        return Result.success("更新成功");
    }
}