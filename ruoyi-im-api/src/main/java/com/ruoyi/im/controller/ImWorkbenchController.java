package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.domain.ImTodoItem;
import com.ruoyi.im.dto.todo.ImTodoCreateRequest;
import com.ruoyi.im.service.ImAppUsageService;
import com.ruoyi.im.service.ImApplicationService;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImNoticeService;
import com.ruoyi.im.service.ImTodoItemService;
import com.ruoyi.im.service.ImUserLayoutService;
import com.ruoyi.im.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台控制器
 * 提供工作台相关功能，包括待办事项、数据概览等
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/workbench")
public class ImWorkbenchController {

    private static final Logger log = LoggerFactory.getLogger(ImWorkbenchController.class);

    private final ImTodoItemService todoItemService;
    private final ImMessageService messageService;
    private final ImConversationService conversationService;
    private final ImNoticeService noticeService;
    private final ImApplicationService applicationService;
    private final ImAppUsageService appUsageService;
    private final ImUserLayoutService userLayoutService;

    /**
     * 构造器注入依赖
     *
     * @param todoItemService 待办事项服务
     * @param messageService 消息服务
     * @param conversationService 会话服务
     * @param noticeService 通知服务
     * @param applicationService 应用服务
     * @param appUsageService 应用使用记录服务
     * @param userLayoutService 用户布局配置服务
     */
    public ImWorkbenchController(ImTodoItemService todoItemService,
                                  ImMessageService messageService,
                                  ImConversationService conversationService,
                                  ImNoticeService noticeService,
                                  ImApplicationService applicationService,
                                  ImAppUsageService appUsageService,
                                  ImUserLayoutService userLayoutService) {
        this.todoItemService = todoItemService;
        this.messageService = messageService;
        this.conversationService = conversationService;
        this.noticeService = noticeService;
        this.applicationService = applicationService;
        this.appUsageService = appUsageService;
        this.userLayoutService = userLayoutService;
    }

    /**
     * 获取工作台数据概览
     * 返回用户相关的各种统计数据
     *
     * @return 工作台概览数据
     */
    
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> overview = new HashMap<>();

        // 待办数量
        int todoCount = todoItemService.getUncompletedCount(userId);
        overview.put("todoCount", todoCount);

        // 未读消息数量
        int unreadMessageCount = 0;
        try {
            unreadMessageCount = conversationService.getTotalUnreadCount(userId);
        } catch (Exception e) {
            log.warn("获取未读消息数量失败，userId={}, error={}", userId, e.getMessage());
        }
        overview.put("unreadMessageCount", unreadMessageCount);

        // 今日消息数量
        int todayMessageCount = 0;
        try {
            todayMessageCount = messageService.getTodayMessageCount(userId);
        } catch (Exception e) {
            log.warn("获取未读消息数量失败，userId={}, error={}", userId, e.getMessage());
        }
        overview.put("todayMessageCount", todayMessageCount);

        // 会话数量
        int conversationCount = 0;
        try {
            conversationCount = conversationService.getUserConversationCount(userId);
        } catch (Exception e) {
            log.warn("获取会话数量失败，userId={}, error={}", userId, e.getMessage());
        }
        overview.put("conversationCount", conversationCount);

        // 未读通知数量
        int noticeCount = 0;
        try {
            noticeCount = noticeService.getUnreadCount(userId);
        } catch (Exception e) {
            log.warn("获取通知数量失败，userId={}, error={}", userId, e.getMessage());
        }
        overview.put("noticeCount", noticeCount);

        // 审批数量（待办事项中类型为APPROVAL的数量）
        int approvalCount = 0;
        try {
            approvalCount = todoItemService.getUncompletedCountByType(userId, "APPROVAL");
        } catch (Exception e) {
            log.warn("获取审批数量失败，userId={}, error={}", userId, e.getMessage());
        }
        overview.put("approvalCount", approvalCount);

        // DING消息未读数量
        int dingCount = 0;
        try {
            dingCount = todoItemService.getUncompletedCountByType(userId, "DING");
        } catch (Exception e) {
            log.warn("获取DING消息数量失败，userId={}, error={}", userId, e.getMessage());
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
     * @return 待办事项列表
     */
    
    @GetMapping("/todos")
    public Result<List<ImTodoItem>> getTodos() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImTodoItem> todos = todoItemService.getUserTodos(userId);
        return Result.success(todos);
    }

    /**
     * 创建待办
     * 创建新的待办事项
     *
     * @param request 待办创建请求
     * @return 创建结果，包含待办ID
     */
    
    @PostMapping("/todos")
    public Result<Long> createTodo(@Valid @RequestBody ImTodoCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        
        String title = request.getTitle();
        String description = request.getDescription();
        String type = request.getType();
        if (type == null || type.isEmpty()) {
            type = "TASK";
        }
        Integer priority = request.getPriority();
        
        Long todoId;
        if (priority != null) {
            todoId = todoItemService.createTodoWithPriority(title, description, priority, userId);
        } else {
            todoId = todoItemService.createTodo(title, description, type, null, userId);
        }
        return Result.success("创建成功", todoId);
    }

    /**
     * 完成待办
     * 将待办事项标记为已完成
     *
     * @param id 待办ID
     * @return 操作结果
     */
    
    @PutMapping("/todos/{id}/complete")
    public Result<Void> completeTodo(@PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        todoItemService.markAsCompleted(id, userId);
        return Result.success("已完成");
    }

    /**
     * 删除待办
     * 删除指定的待办事项
     *
     * @param id 待办ID
     * @return 操作结果
     */
    
    @DeleteMapping("/todos/{id}")
    public Result<Void> deleteTodo(@PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
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
     * @return 操作结果
     */
    
    @PutMapping("/todos/{id}")
    public Result<Void> updateTodo(@PathVariable Long id,
                                  @RequestParam String title,
                                  @RequestParam(required = false) String description) {
        Long userId = SecurityUtils.getLoginUserId();
        todoItemService.updateTodo(id, title, description, userId);
        return Result.success("更新成功");
    }

    /**
     * 获取工作台统计数据
     * 返回工作台相关的统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> statistics = new HashMap<>();

        // 待办数量
        int todoCount = todoItemService.getUncompletedCount(userId);
        statistics.put("todoCount", todoCount);

        // 未读消息数量
        int unreadMessageCount = 0;
        try {
            unreadMessageCount = conversationService.getTotalUnreadCount(userId);
        } catch (Exception e) {
            log.warn("获取未读消息数量失败，userId={}, error={}", userId, e.getMessage());
        }
        statistics.put("unreadMessageCount", unreadMessageCount);

        // 今日消息数量
        int todayMessageCount = 0;
        try {
            todayMessageCount = messageService.getTodayMessageCount(userId);
        } catch (Exception e) {
            log.warn("获取今日消息数量失败，userId={}, error={}", userId, e.getMessage());
        }
        statistics.put("todayMessageCount", todayMessageCount);

        // 会话数量
        int conversationCount = 0;
        try {
            conversationCount = conversationService.getUserConversationCount(userId);
        } catch (Exception e) {
            log.warn("获取会话数量失败，userId={}, error={}", userId, e.getMessage());
        }
        statistics.put("conversationCount", conversationCount);

        return Result.success(statistics);
    }

    /**
     * 获取工作台常用应用
     * 返回当前用户可见的常用应用列表
     *
     * @return 常用应用列表
     */
    @GetMapping("/apps")
    public Result<List<ImApplication>> getCommonApps() {
        List<ImApplication> apps = applicationService.getVisibleApplications();
        return Result.success(apps);
    }

    /**
     * 获取工作台应用分类
     * 返回按分类分组的所有可见应用
     *
     * @return 按分类分组的应用列表
     */
    @GetMapping("/apps/category")
    public Result<Map<String, List<ImApplication>>> getAppsByCategory() {
        Map<String, List<ImApplication>> appsByCategory = applicationService.getApplicationsByCategory();
        return Result.success(appsByCategory);
    }

    /**
     * 搜索工作台应用
     * 根据关键词搜索可见应用
     *
     * @param keyword 搜索关键词
     * @return 匹配的应用列表
     */
    @GetMapping("/apps/search")
    public Result<List<ImApplication>> searchApps(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(applicationService.getVisibleApplications());
        }
        List<ImApplication> apps = applicationService.searchApplications(keyword);
        return Result.success(apps);
    }

    /**
     * 获取最近使用的应用
     * 返回当前用户最近访问的应用列表
     *
     * @return 最近使用的应用列表
     */
    @GetMapping("/apps/recent")
    public Result<List<ImApplication>> getRecentApps() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImApplication> recentApps = appUsageService.getRecentApps(userId);
        return Result.success(recentApps);
    }

    /**
     * 记录应用使用
     * 当用户访问某个应用时调用，用于更新使用历史
     *
     * @param appId 应用ID
     * @return 操作结果
     */
    @PostMapping("/apps/record")
    public Result<Void> recordAppUsage(@RequestParam Long appId) {
        Long userId = SecurityUtils.getLoginUserId();
        appUsageService.recordAppUsage(userId, appId);
        return Result.success("记录成功");
    }

    /**
     * 获取工作台布局配置
     * 返回用户保存的自定义布局配置
     *
     * @return 布局配置JSON
     */
    @GetMapping("/layout")
    public Result<String> getLayoutConfig() {
        Long userId = SecurityUtils.getLoginUserId();
        String config = userLayoutService.getLayoutConfig(userId, ImUserLayoutService.LAYOUT_TYPE_WORKBENCH);
        return Result.success(config);
    }

    /**
     * 保存工作台布局配置
     * 保存用户自定义的工作台布局配置
     *
     * @param layoutConfig 布局配置JSON
     * @return 操作结果
     */
    @PostMapping("/layout")
    public Result<Void> saveLayoutConfig(@RequestBody String layoutConfig) {
        Long userId = SecurityUtils.getLoginUserId();
        userLayoutService.saveLayoutConfig(userId, ImUserLayoutService.LAYOUT_TYPE_WORKBENCH, layoutConfig);
        return Result.success("保存成功");
    }

    /**
     * 重置工作台布局配置
     * 删除用户保存的自定义布局，恢复默认
     *
     * @return 操作结果
     */
    @DeleteMapping("/layout")
    public Result<Void> resetLayoutConfig() {
        Long userId = SecurityUtils.getLoginUserId();
        userLayoutService.deleteLayoutConfig(userId, ImUserLayoutService.LAYOUT_TYPE_WORKBENCH);
        return Result.success("重置成功");
    }
}
