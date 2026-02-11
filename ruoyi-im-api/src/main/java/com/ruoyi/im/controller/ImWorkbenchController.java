package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.dto.workbench.TodoCreateRequest;
import com.ruoyi.im.dto.workbench.TodoUpdateRequest;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImNoticeService;
import com.ruoyi.im.service.ImTodoItemService;
import com.ruoyi.im.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
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

    private final ImTodoItemService todoItemService;
    private final ImMessageService messageService;
    private final ImConversationService conversationService;
    private final ImNoticeService noticeService;

    public ImWorkbenchController(ImTodoItemService todoItemService,
                                  ImMessageService messageService,
                                  ImConversationService conversationService,
                                  ImNoticeService noticeService) {
        this.todoItemService = todoItemService;
        this.messageService = messageService;
        this.conversationService = conversationService;
        this.noticeService = noticeService;
    }

    // ==================== 工作台概览 ====================

    /**
     * 获取工作台数据概览
     */
    @Operation(summary = "获取工作台数据概览", description = "获取用户相关的统计数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> overview = new HashMap<>();

        overview.put("todoCount", todoItemService.getUncompletedCount(userId));
        overview.put("unreadMessageCount", conversationService.getTotalUnreadCount(userId));
        overview.put("todayMessageCount", messageService.getTodayMessageCount(userId));
        overview.put("conversationCount", conversationService.getUserConversationCount(userId));
        overview.put("noticeCount", noticeService.getUnreadCount(userId));
        overview.put("approvalCount", todoItemService.getUncompletedCountByType(userId, "APPROVAL"));
        overview.put("dingCount", todoItemService.getUncompletedCountByType(userId, "DING"));
        overview.put("isOnline", com.ruoyi.im.websocket.ImWebSocketEndpoint.isUserOnline(userId));

        return Result.success(overview);
    }

    // ==================== 待办事项管理 ====================

    /**
     * 获取待办列表
     */
    @Operation(summary = "获取待办列表", description = "获取用户的待办事项列表")
    @GetMapping("/todos")
    public Result<List<ImTodoItem>> getTodos() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImTodoItem> todos = todoItemService.getUserTodos(userId);
        return Result.success(todos);
    }

    /**
     * 创建待办
     */
    @Operation(summary = "创建待办", description = "创建新的待办事项")
    @PostMapping("/todos")
    public Result<Long> createTodo(@Valid @RequestBody TodoCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long todoId;
        if (request.getPriority() != null) {
            todoId = todoItemService.createTodoWithPriority(
                    request.getTitle(),
                    request.getDescription(),
                    request.getPriority(),
                    userId);
        } else {
            todoId = todoItemService.createTodo(
                    request.getTitle(),
                    request.getDescription(),
                    request.getType(),
                    request.getRelatedId(),
                    userId);
        }
        return Result.success("创建成功", todoId);
    }

    /**
     * 完成待办
     */
    @Operation(summary = "完成待办", description = "将待办事项标记为已完成")
    @PutMapping("/todos/{id}/complete")
    public Result<Void> completeTodo(@PathVariable @Positive(message = "待办ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        todoItemService.markAsCompleted(id, userId);
        return Result.success("已完成");
    }

    /**
     * 更新待办
     */
    @Operation(summary = "更新待办", description = "更新待办事项的标题和描述")
    @PutMapping("/todos/{id}")
    public Result<Void> updateTodo(
            @PathVariable @Positive(message = "待办ID必须为正数") Long id,
            @Valid @RequestBody TodoUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        todoItemService.updateTodo(id, request.getTitle(), request.getDescription(), userId);
        return Result.success("更新成功");
    }

    /**
     * 删除待办
     */
    @Operation(summary = "删除待办", description = "删除指定的待办事项")
    @DeleteMapping("/todos/{id}")
    public Result<Void> deleteTodo(@PathVariable @Positive(message = "待办ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        todoItemService.deleteTodo(id, userId);
        return Result.success("删除成功");
    }
}