package com.ruoyi.im.controller;

import com.github.pagehelper.PageInfo;
import com.ruoyi.common.annotation.RequirePermission;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.annotation.SwaggerTag;
import com.ruoyi.im.annotation.ImPerformanceMonitor;
import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.utils.SecurityUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM会话控制器
 * 
 * 提供会话管理的RESTful接口，包括会话列表获取、私聊/群聊会话创建、
 * 会话详情查询、会话设置更新等功能
 * 
 * @author ruoyi
 */
@Api(tags = "会话管理")
@RestController
@RequestMapping("/api/{version}/im/session")
@ImApiVersion(value = {"v1", "v2"}, description = "会话管理API，支持v1和v2版本")
public class ImSessionController extends BaseController {

    @Autowired
    private ImConversationService imConversationService;
    
    @Autowired
    private ImConversationMemberService imConversationMemberService;

    /**
     * 获取会话列表
     * 
     * 分页获取当前用户的所有会话列表，支持按会话类型（私聊/群聊）进行过滤。
     * 返回的会话信息包含会话基本信息以及当前用户在每个会话中的个人设置（置顶、免打扰、未读数等）。
     * 
     * @param type 会话类型过滤参数，支持 "PRIVATE"（私聊）和 "GROUP"（群聊），为空时返回所有类型
     * @return 分页的会话列表结果，包含会话信息和用户个人设置
     */
    @ApiOperation(value = "获取会话列表", notes = "分页获取当前用户的所有会话列表，支持按类型过滤")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @GetMapping("/list")
    @RequirePermission(value = "im:session:list", desc = "查看会话列表")
    @ImPerformanceMonitor(value = "会话列表查询", detailed = true, threshold = 600, group = "会话管理")
    @ImRateLimit(key = "session:list", rate = 200, window = 60, algorithm = ImRateLimit.LimitAlgorithm.SLIDING_WINDOW, message = "查询会话列表请求过于频繁，请稍后再试", group = "会话管理")
    public Result<PageResult<Map<String, Object>>> listSessions(
            @ApiParam("会话类型过滤，PRIVATE表示私聊，GROUP表示群聊，为空时返回所有类型") 
            @RequestParam(required = false) String type) {
        Long currentUserId = SecurityUtils.getLoginUserId();
        
        startPage();
        // 使用新添加的支持type过滤的方法
        List<ImConversation> list = imConversationService.selectImConversationListByUserId(currentUserId, type);
        
        // 为每个会话添加成员信息和未读数
        List<Map<String, Object>> sessionList = list.stream()
            .map(conv -> {
                Map<String, Object> session = new HashMap<>();
                session.put("id", conv.getId());
                session.put("type", conv.getType());
                session.put("targetId", conv.getTargetId());
                session.put("lastMessageId", conv.getLastMessageId());
                session.put("createTime", conv.getCreateTime());
                session.put("updateTime", conv.getUpdateTime());
                session.put("name", conv.getName());
                session.put("avatar", conv.getAvatar());
                
                // 获取当前用户在此会话中的信息
                ImConversationMember member = imConversationMemberService
                    .selectImConversationMemberByConversationIdAndUserId(conv.getId(), currentUserId);
                if (member != null) {
                    session.put("isPinned", member.getIsPinned());
                    session.put("isMuted", member.getIsMuted());
                    session.put("unreadCount", member.getUnreadCount());
                }
                
                return session;
            })
            .collect(Collectors.toList());
        
        return getDataTable(sessionList);
    }

    /**
     * 创建会话
     * 
     * 创建新的私聊或群聊会话。如果是私聊会话，会检查是否已存在，
     * 如果已存在则返回现有会话；如果不存在则创建新会话。
     * 群聊会话基于已存在的群组创建。
     * 
     * @param sessionData 会话创建请求参数，包含type（会话类型）和targetId（目标ID）
     * @return 创建成功的会话信息
     */
    @ApiOperation(value = "创建会话", notes = "创建私聊或群聊会话，私聊支持重复检查，群聊基于群组创建")
    @ApiResponses({
        @ApiResponse(code = 200, message = "会话创建成功"),
        @ApiResponse(code = 400, message = "参数验证失败或不支持的会话类型"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 500, message = "会话创建失败")
    })
    @PostMapping
    @RequirePermission(value = "im:session:create", desc = "创建会话")
    @ImPerformanceMonitor(value = "创建会话", threshold = 800, group = "会话管理")
    @ImRateLimit(key = "session:create", rate = 30, window = 60, message = "创建会话请求过于频繁，请稍后再试", group = "会话管理")
    public Result<ImConversation> createSession(
            @Valid @ApiParam("会话创建请求参数，包含type和targetId") @RequestBody Map<String, Object> sessionData) {
        String type = (String) sessionData.get("type");
        Object targetIdObj = sessionData.get("targetId");
        if (targetIdObj == null) {
            return Result.error("目标ID不能为空");
        }
        Long targetId = Long.valueOf(targetIdObj.toString());
        
        Long currentUserId = SecurityUtils.getLoginUserId();
        
        ImConversation conversation;
        if ("PRIVATE".equals(type)) {
            // 创建私聊会话
            conversation = imConversationService.createPrivateConversation(currentUserId, targetId);
        } else if ("GROUP".equals(type)) {
            // 创建群聊会话
            conversation = imConversationService.createGroupConversation(targetId);
        } else {
            return Result.error(400, "不支持的会话类型");
        }
        
        if (conversation != null) {
            return Result.success(conversation);
        } else {
            return Result.error("会话创建失败");
        }
    }

    /**
     * 获取会话详情
     * 
     * 根据会话ID获取会话的详细信息，包括会话基本信息和当前用户在此会话中的个人设置。
     * 只有作为会话成员的用户才能查看会话详情。
     * 
     * @param sessionId 会话ID，用于标识要查询的具体会话
     * @return 会话详细信息，包含会话基本信息和用户个人设置
     */
    @ApiOperation(value = "获取会话详情", notes = "根据会话ID获取会话详细信息和用户个人设置")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "会话不存在或无权限访问")
    })
    @GetMapping("/{sessionId}")
    @RequirePermission(value = "im:session:get", desc = "查看会话详情")
    @ImPerformanceMonitor(value = "会话详情查询", threshold = 400, group = "会话管理")
    @ImRateLimit(key = "session:detail", rate = 300, window = 60, message = "查询会话详情请求过于频繁，请稍后再试", group = "会话管理")
    public Result<Map<String, Object>> getSession(
            @ApiParam("会话ID，用于标识要查询的具体会话") @PathVariable Long sessionId) {
        ImConversation conversation = imConversationService.selectImConversationById(sessionId);
        if (conversation != null) {
            Long currentUserId = SecurityUtils.getLoginUserId();
            
            Map<String, Object> session = new HashMap<>();
            session.put("id", conversation.getId());
            session.put("type", conversation.getType());
            session.put("targetId", conversation.getTargetId());
            session.put("lastMessageId", conversation.getLastMessageId());
            session.put("createTime", conversation.getCreateTime());
            session.put("updateTime", conversation.getUpdateTime());
            session.put("name", conversation.getName());
            session.put("avatar", conversation.getAvatar());
            
            // 获取当前用户在此会话中的信息
            ImConversationMember member = imConversationMemberService
                .selectImConversationMemberByConversationIdAndUserId(sessionId, currentUserId);
            if (member != null) {
                session.put("isPinned", member.getIsPinned());
                session.put("isMuted", member.getIsMuted());
                session.put("unreadCount", member.getUnreadCount());
            }
            
            return Result.success(session);
        } else {
            return Result.error(404, "会话不存在");
        }
    }

    /**
     * 更新会话设置
     * 
     * 更新当前用户在指定会话中的个人设置，包括置顶和免打扰功能。
     * 只有会话成员才能更新自己的会话设置。
     * 
     * @param sessionData 会话设置更新请求参数，包含会话ID、置顶状态、免打扰状态等
     * @return 操作结果，空内容表示成功
     */
    @ApiOperation(value = "更新会话设置", notes = "更新用户在会话中的置顶、免打扰等个人设置")
    @ApiResponses({
        @ApiResponse(code = 200, message = "设置更新成功"),
        @ApiResponse(code = 400, message = "参数验证失败或会话ID为空"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "会话不存在或非会话成员")
    })
    @PutMapping
    @RequirePermission(value = "im:session:update", desc = "更新会话设置")
    @ImPerformanceMonitor(value = "更新会话设置", threshold = 500, group = "会话管理")
    @ImRateLimit(key = "session:update", rate = 100, window = 60, message = "更新会话设置请求过于频繁，请稍后再试", group = "会话管理")
    public Result<Void> updateSession(
            @Valid @ApiParam("会话设置更新请求参数，包含会话ID、置顶状态、免打扰状态") 
            @RequestBody Map<String, Object> sessionData) {
        Object idObj = sessionData.get("id");
        if (idObj == null) {
            return Result.error("会话ID不能为空");
        }
        Long sessionId = Long.valueOf(idObj.toString());
        Boolean isPinned = (Boolean) sessionData.get("isPinned");
        Boolean isMuted = (Boolean) sessionData.get("isMuted");
        
        Long currentUserId = SecurityUtils.getLoginUserId();
        
        // 更新当前用户的会话设置（在成员表中）
        ImConversationMember member = imConversationMemberService
            .selectImConversationMemberByConversationIdAndUserId(sessionId, currentUserId);
            
        if (member != null) {
            if (isPinned != null) member.setIsPinned(isPinned);
            if (isMuted != null) member.setIsMuted(isMuted);
            imConversationMemberService.updateImConversationMember(member);
            return Result.success();
        } else {
            return Result.error("会话成员不存在");
        }
    }
}
