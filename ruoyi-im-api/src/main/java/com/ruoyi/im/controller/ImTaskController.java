package com.ruoyi.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.task.ImTaskCreateRequest;
import com.ruoyi.im.dto.task.ImTaskQueryRequest;
import com.ruoyi.im.dto.task.ImTaskUpdateRequest;
import com.ruoyi.im.service.ImTaskReminderService;
import com.ruoyi.im.service.ImTaskService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.task.ImTaskDetailVO;
import com.ruoyi.im.vo.task.ImTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务管理控制器
 * 提供任务创建、分配、跟踪、统计等功能
 *
 * @author ruoyi
 */
@Tag(name = "任务管理", description = "任务创建、分配、跟踪、统计等接口")
@RestController
@RequestMapping("/api/im/tasks")
public class ImTaskController {

    private final ImTaskService taskService;
    private final ImTaskReminderService taskReminderService;

    /**
     * 构造器注入依赖
     *
     * @param taskService 任务服务
     * @param taskReminderService 任务提醒服务
     */
    public ImTaskController(ImTaskService taskService,
                            ImTaskReminderService taskReminderService) {
        this.taskService = taskService;
        this.taskReminderService = taskReminderService;
    }

    /**
     * 创建任务
     * 创建新的任务，可分配给指定成员
     *
     * @param request 创建请求
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 创建结果，包含任务ID
     * @apiNote 支持创建子任务，通过parentId指定父任务
     */
    @Operation(summary = "创建任务", description = "创建新的任务，可分配给指定成员")
    @PostMapping("/create")
    public Result<Long> createTask(@Valid @RequestBody ImTaskCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long taskId = taskService.createTask(request, userId);
        return Result.success("创建成功", taskId);
    }

    /**
     * 更新任务
     * 更新任务的基本信息和状态
     *
     * @param request 更新请求
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 更新结果
     * @apiNote 任务负责人和创建人可更新任务信息
     */
    @Operation(summary = "更新任务", description = "更新任务的基本信息和状态")
    @PutMapping
    public Result<Void> updateTask(@Valid @RequestBody ImTaskUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        taskService.updateTask(request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除任务
     * 删除指定任务
     *
     * @param taskId 任务ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 删除结果
     * @apiNote 删除任务时同时删除其子任务
     */
    @Operation(summary = "删除任务", description = "删除指定任务及其子任务")
    @DeleteMapping("/{taskId}")
    public Result<Void> deleteTask(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getLoginUserId();
        taskService.deleteTask(taskId, userId);
        return Result.success("删除成功");
    }

    /**
     * 获取任务详情
     * 查询指定任务的详细信息
     *
     * @param taskId 任务ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 任务详情
     * @apiDetail 包含任务基本信息、子任务、评论、附件等
     */
    @Operation(summary = "获取任务详情", description = "查询指定任务的详细信息")
    @GetMapping("/{taskId}")
    public Result<ImTaskDetailVO> getTaskDetail(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImTaskDetailVO detail = taskService.getTaskDetail(taskId, userId);
        return Result.success(detail);
    }

    /**
     * 分页查询任务列表
     * 按条件分页查询任务列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 关键词（可选）
     * @param status 状态（可选）
     * @param priority 优先级（可选）
     * @param assigneeId 负责人ID（可选）
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 分页结果
     * @apiNote 支持按关键词、状态、优先级、负责人等条件筛选
     */
    @Operation(summary = "分页查询任务列表", description = "按条件分页查询任务列表")
    @GetMapping("/page")
    public Result<IPage<ImTaskVO>> getTaskPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long assigneeId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImTaskQueryRequest request = new ImTaskQueryRequest();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);
        request.setStatus(status);
        request.setPriority(priority);
        request.setAssigneeId(assigneeId);
        IPage<ImTaskVO> page = taskService.getTaskPage(request, userId);
        return Result.success(page);
    }

    /**
     * 获取我的任务列表
     * 查询分配给当前用户的任务列表
     *
     * @param status 任务状态（可选）
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 任务列表
     * @apiNote status可选值：PENDING待办、IN_PROGRESS进行中、COMPLETED已完成
     */
    @Operation(summary = "获取我的任务列表", description = "查询分配给当前用户的任务列表")
    @GetMapping("/my")
    public Result<List<ImTaskVO>> getMyTasks(@RequestParam(required = false) String status) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImTaskVO> list = taskService.getMyTasks(userId, status);
        return Result.success(list);
    }

    /**
     * 获取我创建的任务列表
     * 查询当前用户创建的任务列表
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 任务列表
     */
    @Operation(summary = "获取我创建的任务列表", description = "查询当前用户创建的任务列表")
    @GetMapping("/created")
    public Result<List<ImTaskVO>> getMyCreatedTasks() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImTaskVO> list = taskService.getMyCreatedTasks(userId);
        return Result.success(list);
    }

    /**
     * 分配任务
     * 将任务分配给指定成员
     *
     * @param taskId 任务ID
     * @param assigneeId 负责人ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 只有任务创建人可以分配任务
     */
    @Operation(summary = "分配任务", description = "将任务分配给指定成员")
    @PutMapping("/{taskId}/assign")
    public Result<Void> assignTask(@PathVariable Long taskId,
                                   @RequestParam Long assigneeId) {
        Long userId = SecurityUtils.getLoginUserId();
        taskService.assignTask(taskId, assigneeId, userId);
        return Result.success("分配成功");
    }

    /**
     * 更新任务状态
     * 更新任务的状态
     *
     * @param taskId 任务ID
     * @param status 状态（PENDING、IN_PROGRESS、COMPLETED、CANCELLED、BLOCKED）
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "更新任务状态", description = "更新任务的状态")
    @PutMapping("/{taskId}/status")
    public Result<Void> updateStatus(@PathVariable Long taskId,
                                    @RequestParam String status) {
        Long userId = SecurityUtils.getLoginUserId();
        taskService.updateTaskStatus(taskId, status, userId);
        return Result.success("状态更新成功");
    }

    /**
     * 更新任务进度
     * 更新任务的完成进度
     *
     * @param taskId 任务ID
     * @param percent 完成百分比（0-100）
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "更新任务进度", description = "更新任务的完成进度")
    @PutMapping("/{taskId}/progress")
    public Result<Void> updateProgress(@PathVariable Long taskId,
                                      @RequestParam Integer percent) {
        Long userId = SecurityUtils.getLoginUserId();
        taskService.updateProgress(taskId, percent, userId);
        return Result.success("进度更新成功");
    }

    /**
     * 关注/取消关注任务
     * 切换任务的关注状态
     *
     * @param taskId 任务ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "关注/取消关注任务", description = "切换任务的关注状态")
    @PutMapping("/{taskId}/follow")
    public Result<Void> toggleFollow(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getLoginUserId();
        taskService.toggleFollow(taskId, userId);
        return Result.success("操作成功");
    }

    /**
     * 添加任务评论
     * 为指定任务添加评论
     *
     * @param taskId 任务ID
     * @param content 评论内容
     * @param replyToId 回复的评论ID（可选）
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 评论ID
     */
    @Operation(summary = "添加任务评论", description = "为指定任务添加评论")
    @PostMapping("/{taskId}/comment")
    public Result<Long> addComment(@PathVariable Long taskId,
                                  @RequestParam String content,
                                  @RequestParam(required = false) Long replyToId) {
        Long userId = SecurityUtils.getLoginUserId();
        Long commentId = taskService.addComment(taskId, content, replyToId, userId);
        return Result.success("评论成功", commentId);
    }

    /**
     * 获取任务评论列表
     * 查询指定任务的评论列表
     *
     * @param taskId 任务ID
     * @return 评论列表
     */
    @Operation(summary = "获取任务评论列表", description = "查询指定任务的评论列表")
    @GetMapping("/{taskId}/comments")
    public Result<List<Map<String, Object>>> getComments(@PathVariable Long taskId) {
        List<Map<String, Object>> comments = taskService.getComments(taskId);
        return Result.success(comments);
    }

    /**
     * 获取任务统计信息
     * 获取当前用户的任务统计数据
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 统计信息
     * @apiNote 包含待办、进行中、已完成、逾期等任务数量
     */
    @Operation(summary = "获取任务统计信息", description = "获取当前用户的任务统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Long userId = SecurityUtils.getLoginUserId();
        Map<String, Object> stats = taskService.getTaskStatistics(userId);
        return Result.success(stats);
    }

    /**
     * 批量删除任务
     * 批量删除多个任务
     *
     * @param taskIds 任务ID列表
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "批量删除任务", description = "批量删除多个任务")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> taskIds) {
        Long userId = SecurityUtils.getLoginUserId();
        taskService.batchDelete(taskIds, userId);
        return Result.success("批量删除成功");
    }

    /**
     * 批量更新任务状态
     * 批量更新多个任务的状态
     *
     * @param taskIds 任务ID列表
     * @param status 目标状态
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "批量更新任务状态", description = "批量更新多个任务的状态")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(@RequestBody List<Long> taskIds,
                                         @RequestParam String status) {
        Long userId = SecurityUtils.getLoginUserId();
        taskService.batchUpdateStatus(taskIds, status, userId);
        return Result.success("批量更新成功");
    }

    /**
     * 获取子任务列表
     * 查询指定任务的子任务列表
     *
     * @param parentId 父任务ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 子任务列表
     */
    @Operation(summary = "获取子任务列表", description = "查询指定任务的子任务列表")
    @GetMapping("/subtasks/{parentId}")
    public Result<List<ImTaskVO>> getSubtasks(@PathVariable Long parentId) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImTaskVO> list = taskService.getSubtasks(parentId, userId);
        return Result.success(list);
    }

    /**
     * 复制任务
     * 复制指定任务创建副本
     *
     * @param taskId 任务ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 新任务ID
     */
    @Operation(summary = "复制任务", description = "复制指定任务创建副本")
    @PostMapping("/{taskId}/copy")
    public Result<Long> copyTask(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getLoginUserId();
        Long newTaskId = taskService.copyTask(taskId, userId);
        return Result.success("复制成功", newTaskId);
    }

    // ==================== 任务提醒功能接口 ====================

    /**
     * 设置任务提醒
     * 为任务设置提醒时间和提醒方式
     *
     * @param taskId 任务ID
     * @param remindTime 提醒时间
     * @param remindType 提醒类型（NONE无 EMAIL邮件 SMS短信 DING钉钉）
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 提醒类型为NONE时表示取消提醒
     */
    @Operation(summary = "设置任务提醒", description = "为任务设置提醒时间和提醒方式")
    @PutMapping("/{taskId}/reminder")
    public Result<Void> setReminder(@PathVariable Long taskId,
                                     @RequestParam LocalDateTime remindTime,
                                     @RequestParam(defaultValue = "DING") String remindType) {
        Long userId = SecurityUtils.getLoginUserId();
        taskReminderService.setReminder(taskId, remindTime, remindType);
        return Result.success("提醒已设置");
    }

    /**
     * 取消任务提醒
     * 取消任务的提醒通知
     *
     * @param taskId 任务ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     */
    @Operation(summary = "取消任务提醒", description = "取消任务的提醒通知")
    @DeleteMapping("/{taskId}/reminder")
    public Result<Void> cancelReminder(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getLoginUserId();
        taskReminderService.cancelReminder(taskId);
        return Result.success("提醒已取消");
    }

    /**
     * 发送任务提醒
     * 手动触发发送任务提醒通知
     *
     * @param taskId 任务ID
     * @return 操作结果
     * @apiNote 用于测试或手动触发提醒
     */
    @Operation(summary = "发送任务提醒", description = "手动触发发送任务提醒通知")
    @PostMapping("/{taskId}/reminder/send")
    public Result<Void> sendReminder(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getLoginUserId();
        // 这里需要验证权限（任务创建人或负责人）
        // 简化实现：暂时允许任务负责人手动触发
        taskReminderService.batchSendReminders(java.util.Collections.singletonList(taskId));
        return Result.success("提醒已发送");
    }

    /**
     * 获取我的提醒任务列表
     * 获取当前用户所有设置了提醒的任务
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 提醒任务列表
     * @apiNote 包含分配给用户的任务和用户关注的任务
     */
    @Operation(summary = "获取我的提醒任务", description = "获取当前用户所有设置了提醒的任务")
    @GetMapping("/reminders")
    public Result<List<ImTaskVO>> getMyReminders() {
        Long userId = SecurityUtils.getLoginUserId();
        List<com.ruoyi.im.domain.ImTask> tasks = taskReminderService.getUserReminderTasks(userId);
        List<ImTaskVO> list = new java.util.ArrayList<>();
        for (com.ruoyi.im.domain.ImTask task : tasks) {
            ImTaskVO vo = new ImTaskVO();
            vo.setId(task.getId());
            vo.setTitle(task.getTitle());
            vo.setDescription(task.getDescription());
            vo.setDueDate(task.getDueDate());
            vo.setStatus(task.getStatus());
            vo.setPriority(task.getPriority());
            vo.setCompletionPercent(task.getCompletionPercent());
            list.add(vo);
        }
        return Result.success(list);
    }

    /**
     * 扫描并发送提醒
     * 手动触发扫描并发送任务提醒
     *
     * @return 操作结果，包含发送的提醒数量
     * @apiNote 定时任务会自动执行，此接口用于手动触发
     */
    @Operation(summary = "扫描并发送提醒", description = "手动触发扫描并发送任务提醒")
    @PostMapping("/reminders/scan")
    public Result<Map<String, Object>> scanAndSendReminders() {
        taskReminderService.scanAndSendOverdueReminders();
        return Result.success("提醒扫描完成", java.util.Collections.singletonMap("timestamp", System.currentTimeMillis()));
    }
}
