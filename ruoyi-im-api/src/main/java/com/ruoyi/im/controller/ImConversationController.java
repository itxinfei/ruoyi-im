package com.ruoyi.im.controller;

import com.ruoyi.im.annotation.RateLimit;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.conversation.ImConversationCreateRequest;
import com.ruoyi.im.dto.conversation.ImConversationUpdateRequest;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.service.ImConversationSyncService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.conversation.ImConversationVO;
import com.ruoyi.im.vo.sync.ConversationSyncResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import java.util.List;

/**
 * 会话控制器
 * 提供会话创建、管理、未读消息统计、会话置顶、免打扰等功能
 *
 * @author ruoyi
 */
@Tag(name = "会话管理", description = "会话创建、管理、未读消息统计、会话置顶、免打扰等功能")
@RestController
@RequestMapping("/api/im/conversations")
@Validated
public class ImConversationController {

    private final ImConversationService imConversationService;
    private final ImConversationSyncService imConversationSyncService;

    /**
     * 构造器注入依赖
     *
     * @param imConversationService 会话服务
     * @param imConversationSyncService 会话同步服务
     */
    public ImConversationController(ImConversationService imConversationService,
                                  ImConversationSyncService imConversationSyncService) {
        this.imConversationService = imConversationService;
        this.imConversationSyncService = imConversationSyncService;
    }

    /**
     * 获取会话列表
     * 查询当前用户的所有会话，包括单聊和群聊会话
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 会话列表，按最后消息时间倒序排列
     * @apiNote 返回的会话信息包含未读消息数、最后消息、会话置顶状态等
     */
    @Operation(summary = "获取会话列表", description = "查询当前用户的所有会话，包括单聊和群聊")
    @GetMapping("/list")
    public Result<List<ImConversationVO>> getUserConversations() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImConversationVO> list = imConversationService.getUserConversations(userId);
        return Result.success(list);
    }

    /**
     * 获取会话详情
     * 查询指定会话的详细信息
     *
     * @param id 会话ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 会话详细信息
     * @apiNote 会话必须是当前用户的会话
     */
    @Operation(summary = "获取会话详情", description = "查询指定会话的详细信息")
    @GetMapping("/{id}")
    public Result<ImConversationVO> getConversationById(@PathVariable @Positive(message = "会话ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        ImConversationVO vo = imConversationService.getConversationById(id, userId);
        return Result.success(vo);
    }

    /**
     * 创建会话
     * 创建一个新的会话，支持单聊和群聊
     *
     * @param request 会话创建请求参数
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 创建结果，包含新会话ID
     * @apiNote 使用 @Valid 注解进行参数校验；会话类型包括PRIVATE(单聊)、GROUP(群聊)
     */
    @Operation(summary = "创建会话", description = "创建一个新的会话，支持单聊和群聊")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PostMapping("/create")
    @RateLimit(key = "conversation_create", time = 60, count = 50, limitType = RateLimit.LimitType.USER)
    public Result<ImConversationVO> createConversation(@Valid @RequestBody ImConversationCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long conversationId = imConversationService.createConversation(request, userId);
        ImConversationVO vo = imConversationService.getConversationById(conversationId, userId);
        return Result.success("创建成功", vo);
    }

    /**
     * 更新会话设置
     * 更新会话的置顶、免打扰等设置
     *
     * @param id 会话ID
     * @param request 会话更新请求参数
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 更新结果
     * @apiNote 使用 @Valid 注解进行参数校验；只能更新自己的会话设置
     */
    @Operation(summary = "更新会话设置", description = "更新会话的置顶、免打扰等设置")
    @PutMapping("/{id}")
    public Result<Void> updateConversation(@PathVariable @Positive(message = "会话ID必须为正数") Long id,
                                         @Valid @RequestBody ImConversationUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imConversationService.updateConversation(id, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除会话
     * 从会话列表中删除指定会话（非物理删除）
     *
     * @param id 会话ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 删除结果
     * @apiNote 删除后会话不再显示在会话列表中，但历史消息仍保留
     */
    @Operation(summary = "删除会话", description = "从会话列表中删除指定会话（非物理删除）")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteConversation(@PathVariable @Positive(message = "会话ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        imConversationService.deleteConversation(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 置顶/取消置顶会话
     *
     * @param id 会话ID
     * @param pinned 是否置顶，true表示置顶，false表示取消置顶
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 置顶的会话会一直显示在会话列表顶部
     */
    @Operation(summary = "置顶/取消置顶会话", description = "设置会话置顶状态")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}/pinned")
    public Result<Void> setPinned(@PathVariable @Positive(message = "会话ID必须为正数") Long id,
                                @RequestParam @NotNull(message = "置顶状态不能为空") Boolean pinned) {
        Long userId = SecurityUtils.getLoginUserId();
        imConversationService.setPinned(id, pinned, userId);
        return Result.success(pinned ? "置顶成功" : "取消置顶成功");
    }

    /**
     * 设置免打扰
     *
     * @param id 会话ID
     * @param muted 是否免打扰，true表示免打扰，false表示取消免打扰
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 设置免打扰后，该会话的消息不会触发通知提醒
     */
    @Operation(summary = "设置免打扰", description = "设置会话免打扰状态")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}/muted")
    public Result<Void> setMuted(@PathVariable @Positive(message = "会话ID必须为正数") Long id,
                               @RequestParam @NotNull(message = "免打扰状态不能为空") Boolean muted) {
        Long userId = SecurityUtils.getLoginUserId();
        imConversationService.setMuted(id, muted, userId);
        return Result.success(muted ? "免打扰设置成功" : "免打扰取消成功");
    }

    /**
     * 搜索会话
     * 根据关键词搜索会话
     *
     * @param keyword 搜索关键词
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 匹配的会话列表
     * @apiNote 搜索会话名称、最近消息内容等
     */
    @Operation(summary = "搜索会话", description = "根据关键词搜索会话")
    @GetMapping("/search")
    @RateLimit(key = "conversation_search", time = 60, count = 30, limitType = RateLimit.LimitType.USER)
    public Result<List<ImConversationVO>> search(@RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImConversationVO> list = imConversationService.searchConversations(keyword, userId);
        return Result.success(list);
    }

    /**
     * 标记会话为已读
     * 将指定会话的所有未读消息标记为已读
     *
     * @param id 会话ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 标记后该会话的未读消息数将变为0
     */
    @Operation(summary = "标记会话为已读", description = "将指定会话的所有未读消息标记为已读")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @PutMapping("/{id}/markAsRead")
    public Result<Void> markAsRead(@PathVariable @Positive(message = "会话ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        imConversationService.markAsRead(userId, id);
        return Result.success("标记已读成功");
    }

    /**
     * 获取未读消息总数
     * 统计当前用户所有会话的未读消息总数
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 未读消息总数
     * @apiNote 用于显示总的未读消息数提醒
     */
    @Operation(summary = "获取未读消息总数", description = "统计当前用户所有会话的未读消息总数")
    @GetMapping("/unreadCount")
    public Result<Integer> getTotalUnreadCount() {
        Long userId = SecurityUtils.getLoginUserId();
        Integer count = imConversationService.getTotalUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 同步会话事件
     * 获取自上次同步以来发生的会话事件（置顶、免打扰、删除、归档、已读等）
     *
     * @param deviceId 设备ID，从请求头获取
     * @param lastSyncTime 上次同步时间戳，可选
     * @return 会话事件列表和新的同步时间戳
     * @apiNote 用于多设备间会话设置同步；首次同步时传0或不传
     */
    @Operation(summary = "同步会话事件", description = "获取自上次同步以来发生的会话事件")
    @GetMapping("/sync")
    public Result<ConversationSyncResponse> syncConversations(
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestParam(required = false) Long lastSyncTime) {
        Long userId = SecurityUtils.getLoginUserId();
        ConversationSyncResponse response = imConversationSyncService.syncConversations(userId, deviceId, lastSyncTime);
        return Result.success(response);
    }

    /**
     * 重置会话同步点
     * 删除指定设备的同步点，下次同步将获取全部事件
     *
     * @param deviceId 设备ID
     * @return 操作结果
     * @apiNote 用于调试或解决同步问题
     */
    @Operation(summary = "重置会话同步点", description = "删除指定设备的同步点，下次同步将获取全部事件")
    @DeleteMapping("/sync")
    public Result<Void> resetSyncPoint(
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        Long userId = SecurityUtils.getLoginUserId();
        imConversationSyncService.resetSyncPoint(userId, deviceId);
        return Result.success("已重置同步点");
    }
}