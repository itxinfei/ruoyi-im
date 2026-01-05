package com.ruoyi.im.controller;

import com.github.pagehelper.PageInfo;
import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.annotation.RequirePermission;
import com.ruoyi.im.annotation.ImPerformanceMonitor;
import com.ruoyi.im.annotation.ImRateLimit;
import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.group.*;
import com.ruoyi.im.service.ImGroupService;
import com.ruoyi.im.service.ImGroupMemberService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.group.ImGroupMemberVO;
import com.ruoyi.im.vo.group.ImGroupVO;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM群组控制器
 * 
 * 提供群组的增删改查、成员管理、权限设置等功能
 * 支持群组创建、解散、成员添加/移除、管理员设置等操作
 * 
 * @author ruoyi
 */
@Api(tags = "IM群组管理")
@RestController
@RequestMapping("/api/{version}/im/groups")
@ImApiVersion(value = {"v1", "v2"}, description = "群组管理API，支持v1和v2版本")
public class ImGroupController extends BaseController {

    @Autowired
    private ImGroupService imGroupService;
    
    @Autowired
    private ImGroupMemberService imGroupMemberService;
    
    @Autowired
    private ImUserService imUserService;

    /**
     * 获取群组列表
     * 
     * 支持按群组名称、群主名称、状态进行分页查询
     * 
     * @param request 群组查询请求参数
     * @return 分页的群组列表结果
     */
    /**
     * 获取群组列表
     * 
     * 支持按群组名称、群主名称、状态等条件进行分页查询。
     * 返回当前用户有权限查看的所有群组信息。
     * 
     * @param request 群组查询请求参数，包含搜索条件和分页参数
     * @return 分页的群组列表结果，包含群组详细信息
     */
    @ApiOperation(value = "获取群组列表", notes = "支持按群组名称、群主名称、状态进行分页查询")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足")
    })
    @GetMapping
    @RequirePermission(value = "im:group:list", desc = "查看群组列表")
    @ImPerformanceMonitor(value = "群组列表查询", detailed = true, threshold = 800, group = "群组管理")
    @ImRateLimit(key = "group:list", rate = 80, window = 60, algorithm = ImRateLimit.LimitAlgorithm.SLIDING_WINDOW, message = "查询群组列表请求过于频繁，请稍后再试", group = "群组管理")
    public Result<PageResult<ImGroupVO>> listGroups(@Valid @ApiParam("群组查询请求参数") ImGroupQueryRequest request) {
        log.info("获取群组列表请求: groupName={}, ownerName={}, status={}, pageNum={}, pageSize={}", 
                request.getGroupName(), request.getOwnerName(), request.getStatus(), 
                request.getPageNum(), request.getPageSize());
        
        ImGroup query = new ImGroup();
        query.setName(request.getGroupName());
        query.setStatus(request.getStatus());
        query.setOwnerName(request.getOwnerName());
        
        startPage(request.getPageNum(), request.getPageSize());
        List<ImGroup> list = imGroupService.selectList(query);
        
        List<ImGroupVO> groupVOList = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        PageInfo<ImGroup> pageInfo = new PageInfo<>(list);
        PageResult<ImGroupVO> pageResult = new PageResult<>(
                groupVOList,
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
        
        log.info("群组列表查询成功，总数: {}", pageInfo.getTotal());
        return Result.success(pageResult);
    }

    /**
     * 创建群组
     * 
     * 创建新的群组，自动将创建者设为群主并添加为第一个成员
     * 
     * @param request 群组创建请求参数
     * @param bindingResult 验证结果
     * @return 包含操作结果的响应
     */
    /**
     * 创建群组
     * 
     * 创建新的群组并自动将创建者设为群主，同时将群主添加为第一个成员。
     * 群组创建后会初始化群组成员关系，可以立即开始使用。
     * 
     * @param request 群组创建请求参数，包含群组名称、群主ID、公告等信息
     * @param bindingResult 验证结果，用于处理参数校验错误
     * @return 创建成功的群组详细信息
     */
    @ApiOperation(value = "创建群组", notes = "创建新的群组，自动将创建者设为群主并添加为第一个成员")
    @ApiResponses({
        @ApiResponse(code = 200, message = "群组创建成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 500, message = "群组创建失败")
    })
    @PostMapping
    @RequirePermission(value = "im:group:create", desc = "创建群组")
    @ImPerformanceMonitor(value = "创建群组", detailed = true, threshold = 2000, group = "群组管理")
    @ImRateLimit(key = "group:create", rate = 5, window = 300, algorithm = ImRateLimit.LimitAlgorithm.TOKEN_BUCKET, message = "创建群组请求过于频繁，请稍后再试", group = "群组管理")
    public Result<ImGroupVO> createGroup(
            @Valid @ApiParam("群组创建请求参数") @RequestBody ImGroupCreateRequest request, 
            BindingResult bindingResult) {
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        log.info("创建群组请求: name={}, ownerId={}, notice={}", request.getName(), request.getOwnerId(), request.getNotice());
        
        Long currentUserId = getCurrentUserId();
        
        try {
            ImGroup group = new ImGroup();
            group.setName(request.getName());
            group.setOwnerId(request.getOwnerId());
            group.setNotice(request.getNotice());
            group.setAvatar(request.getAvatar() != null ? request.getAvatar() : "/profile/group.png");
            group.setStatus("ACTIVE");
            group.setMemberCount(1); // 创建者本身就是成员
            
            int insertResult = imGroupService.insert(group);
            
            if (insertResult > 0) {
                // 添加群主为群成员
                ImGroupMember member = new ImGroupMember();
                member.setGroupId(group.getId());
                member.setUserId(request.getOwnerId());
                member.setRole("OWNER");
                member.setInviterId(currentUserId);
                imGroupMemberService.insert(member);
                
                ImGroupVO groupVO = convertToVO(group);
                log.info("群组创建成功: groupId={}", group.getId());
                return Result.success("群组创建成功", groupVO);
            } else {
                log.error("群组创建失败: name={}", request.getName());
                return Result.error(500, "群组创建失败");
            }
        } catch (Exception e) {
            log.error("创建群组异常: name={}, error={}", request.getName(), e.getMessage(), e);
            return Result.error(500, "群组创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取群组详情
     * 
     * 根据群组ID获取群组的详细信息
     * 
     * @param groupId 群组ID
     * @return 包含群组信息的响应
     */
    @ApiOperation(value = "获取群组详情", notes = "根据群组ID获取群组的详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "群组不存在")
    })
    @GetMapping("/{groupId}")
    @ImPerformanceMonitor(value = "群组详情查询", threshold = 500, group = "群组管理")
    @ImRateLimit(key = "group:detail", rate = 150, window = 60, message = "查询群组详情请求过于频繁，请稍后再试", group = "群组管理")
    public Result<ImGroupVO> getGroup(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId) {
        log.info("获取群组详情请求: groupId={}", groupId);
        
        ImGroup group = imGroupService.selectImGroupById(groupId);
        if (group == null) {
            log.warn("群组不存在: groupId={}", groupId);
            return Result.error(404, "群组不存在");
        }
        
        ImGroupVO groupVO = convertToVO(group);
        log.info("群组详情查询成功: groupId={}, name={}", groupId, group.getName());
        return Result.success(groupVO);
    }

    /**
     * 更新群组
     * 
     * 更新现有群组的基本信息，如名称、公告、头像等
     * 
     * @param request 群组更新请求参数
     * @param bindingResult 验证结果
     * @return 包含操作结果的响应
     */
    @ApiOperation(value = "更新群组", notes = "更新现有群组的基本信息，如名称、公告、头像等")
    @ApiResponses({
        @ApiResponse(code = 200, message = "群组更新成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "群组不存在"),
        @ApiResponse(code = 500, message = "群组更新失败")
    })
    @PutMapping
    @RequirePermission(value = "im:group:update", desc = "更新群组")
    @ImPerformanceMonitor(value = "更新群组", threshold = 1500, group = "群组管理")
    @ImRateLimit(key = "group:update", rate = 20, window = 60, message = "更新群组请求过于频繁，请稍后再试", group = "群组管理")
    public Result<ImGroupVO> updateGroup(@Valid @RequestBody ImGroupUpdateRequest request, BindingResult bindingResult) {
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        log.info("更新群组请求: groupId={}, name={}, notice={}", request.getId(), request.getName(), request.getNotice());
        
        ImGroup existingGroup = imGroupService.selectImGroupById(request.getId());
        
        if (existingGroup == null) {
            log.warn("群组不存在: groupId={}", request.getId());
            return Result.error(404, "群组不存在");
        }
        
        try {
            // 更新群组信息
            ImGroup group = new ImGroup();
            group.setId(request.getId());
            group.setName(request.getName());
            group.setNotice(request.getNotice());
            group.setAvatar(request.getAvatar());
            
            int updateResult = imGroupService.update(group);
            
            if (updateResult > 0) {
                ImGroup updatedGroup = imGroupService.selectImGroupById(request.getId());
                ImGroupVO groupVO = convertToVO(updatedGroup);
                log.info("群组更新成功: groupId={}", request.getId());
                return Result.success("群组更新成功", groupVO);
            } else {
                log.error("群组更新失败: groupId={}", request.getId());
                return Result.error(500, "群组更新失败");
            }
        } catch (Exception e) {
            log.error("更新群组异常: groupId={}, error={}", request.getId(), e.getMessage(), e);
            return Result.error(500, "更新群组失败: " + e.getMessage());
        }
    }

    /**
     * 解散群组
     * 
     * 根据群组ID解散群组，这是一个不可逆的操作
     * 解散后会删除群组及其所有成员关系
     * 
     * @param groupId 要解散的群组ID
     * @return 包含操作结果的响应
     */
    @ApiOperation(value = "解散群组", notes = "根据群组ID解散群组，这是一个不可逆的操作")
    @ApiResponses({
        @ApiResponse(code = 200, message = "群组解散成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "群组不存在"),
        @ApiResponse(code = 500, message = "群组解散失败")
    })
    @DeleteMapping("/{groupId}")
    @RequirePermission(value = "im:group:delete", desc = "解散群组")
    @ImPerformanceMonitor(value = "解散群组", threshold = 3000, group = "群组管理")
    @ImRateLimit(key = "group:delete", rate = 2, window = 600, algorithm = ImRateLimit.LimitAlgorithm.TOKEN_BUCKET, message = "解散群组请求过于频繁，请稍后再试", group = "群组管理")
    public Result<Void> dissolveGroup(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId) {
        log.info("解散群组请求: groupId={}", groupId);
        
        ImGroup group = imGroupService.selectImGroupById(groupId);
        if (group == null) {
            log.warn("群组不存在: groupId={}", groupId);
            return Result.error(404, "群组不存在");
        }
        
        try {
            int deleted = imGroupService.deleteById(groupId);
            if (deleted > 0) {
                log.info("群组解散成功: groupId={}, name={}", groupId, group.getName());
                return Result.success("群组已解散");
            } else {
                log.error("群组解散失败: groupId={}", groupId);
                return Result.error(500, "群组解散失败");
            }
        } catch (Exception e) {
            log.error("解散群组异常: groupId={}, error={}", groupId, e.getMessage(), e);
            return Result.error(500, "解散群组失败: " + e.getMessage());
        }
    }

    /**
     * 获取群组成员列表
     * 
     * 根据群组ID分页获取所有成员信息，包含用户详细资料
     * 
     * @param groupId 群组ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的群组成员列表结果
     */
    @ApiOperation(value = "获取群组成员列表", notes = "根据群组ID分页获取所有成员信息，包含用户详细资料")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "群组不存在")
    })
    @GetMapping("/{groupId}/members")
    @ImPerformanceMonitor(value = "群组成员查询", threshold = 1000, group = "群组管理")
    @ImRateLimit(key = "group:members", rate = 50, window = 60, message = "查询群组成员请求过于频繁，请稍后再试", group = "群组管理")
    public Result<PageResult<ImGroupMemberVO>> getGroupMembers(
            @PathVariable @Positive(message = "群组ID必须为正数") Long groupId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        log.info("获取群组成员列表请求: groupId={}, pageNum={}, pageSize={}", groupId, pageNum, pageSize);
        
        ImGroup group = imGroupService.selectImGroupById(groupId);
        if (group == null) {
            log.warn("群组不存在: groupId={}", groupId);
            return Result.error(404, "群组不存在");
        }
        
        try {
            // 使用PageHelper进行分页查询
            startPage(pageNum, pageSize);
            List<ImGroupMember> memberList = imGroupMemberService.selectImGroupMemberListByGroupId(groupId);
            
            // 为每个成员获取用户详细信息
            List<ImGroupMemberVO> memberVOList = memberList.stream()
                    .map(this::convertToMemberVO)
                    .collect(Collectors.toList());
            
            PageInfo<ImGroupMember> pageInfo = new PageInfo<>(memberList);
            PageResult<ImGroupMemberVO> pageResult = new PageResult<>(
                    memberVOList,
                    pageInfo.getTotal(),
                    pageInfo.getPageNum(),
                    pageInfo.getPageSize()
            );
            
            log.info("群组成员列表查询成功: groupId={}, 总数={}", groupId, pageInfo.getTotal());
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取群组成员列表异常: groupId={}, error={}", groupId, e.getMessage(), e);
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 添加群组成员
     * 
     * 向指定群组批量添加新成员，被添加的用户将具有普通成员角色
     * 
     * @param groupId 目标群组ID
     * @param request 添加成员请求参数
     * @param bindingResult 验证结果
     * @return 包含操作结果的响应
     */
    @ApiOperation(value = "添加群组成员", notes = "向指定群组批量添加新成员，被添加的用户将具有普通成员角色")
    @ApiResponses({
        @ApiResponse(code = 200, message = "成员添加成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "群组不存在"),
        @ApiResponse(code = 500, message = "成员添加失败")
    })
    @PostMapping("/{groupId}/members")
    @RequirePermission(value = "im:group:member:add", desc = "添加群组成员")
    public Result<List<Long>> addGroupMembers(
            @PathVariable @Positive(message = "群组ID必须为正数") Long groupId,
            @Valid @RequestBody ImGroupMemberAddRequest request,
            BindingResult bindingResult) {
        
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        log.info("添加群组成员请求: groupId={}, userIds={}", groupId, request.getUserIds());
        
        ImGroup group = imGroupService.selectImGroupById(groupId);
        if (group == null) {
            log.warn("群组不存在: groupId={}", groupId);
            return Result.error(404, "群组不存在");
        }
        
        try {
            // 添加群组成员
            Long currentUserId = getCurrentUserId();
            int addResult = imGroupMemberService.addGroupMembers(
                    groupId, 
                    request.getUserIds(), 
                    "MEMBER", 
                    request.getInviterId() != null ? request.getInviterId() : currentUserId
            );
            
            if (addResult > 0) {
                log.info("群组成员添加成功: groupId={}, userIds={}", groupId, request.getUserIds());
                return Result.success("成员添加成功", request.getUserIds());
            } else {
                log.error("群组成员添加失败: groupId={}, userIds={}", groupId, request.getUserIds());
                return Result.error(500, "成员添加失败");
            }
        } catch (Exception e) {
            log.error("添加群组成员异常: groupId={}, error={}", groupId, e.getMessage(), e);
            return Result.error(500, "添加成员失败: " + e.getMessage());
        }
    }

    /**
     * 移除群组成员
     * 
     * 从指定群组中移除指定用户，被移除的用户将不再是群组成员
     * 
     * @param groupId 目标群组ID
     * @param userId 要移除的用户ID
     * @return 包含操作结果的响应
     */
    @ApiOperation(value = "移除群组成员", notes = "从指定群组中移除指定用户，被移除的用户将不再是群组成员")
    @ApiResponses({
        @ApiResponse(code = 200, message = "成员移除成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "群组不存在或成员不存在于群组中"),
        @ApiResponse(code = 500, message = "成员移除失败")
    })
    @DeleteMapping("/{groupId}/members/{userId}")
    @RequirePermission(value = "im:group:member:remove", desc = "移除群组成员")
    public Result<Void> removeGroupMember(
            @PathVariable @Positive(message = "群组ID必须为正数") Long groupId,
            @PathVariable @Positive(message = "用户ID必须为正数") Long userId) {
        
        log.info("移除群组成员请求: groupId={}, userId={}", groupId, userId);
        
        ImGroup group = imGroupService.selectImGroupById(groupId);
        if (group == null) {
            log.warn("群组不存在: groupId={}", groupId);
            return Result.error(404, "群组不存在");
        }
        
        try {
            ImGroupMember member = imGroupMemberService.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            
            if (member == null) {
                log.warn("成员不存在于群组中: groupId={}, userId={}", groupId, userId);
                return Result.error(404, "成员不存在于群组中");
            }
            
            int deleteResult = imGroupMemberService.removeGroupMember(groupId, userId, null);
            
            if (deleteResult > 0) {
                log.info("群组成员移除成功: groupId={}, userId={}", groupId, userId);
                return Result.success("成员移除成功");
            } else {
                log.error("群组成员移除失败: groupId={}, userId={}", groupId, userId);
                return Result.error(500, "成员移除失败");
            }
        } catch (Exception e) {
            log.error("移除群组成员异常: groupId={}, userId={}, error={}", groupId, userId, e.getMessage(), e);
            return Result.error(500, "移除成员失败: " + e.getMessage());
        }
    }

    /**
     * 设置群组管理员
     * 
     * 设置或取消指定用户的群组管理员权限
     * 
     * @param groupId 目标群组ID
     * @param userId 目标用户ID
     * @param isAdmin true表示设置为管理员，false表示取消管理员权限
     * @return 包含操作结果的响应
     *         - code: 200 管理员权限设置成功
     *         - code: 404 群组不存在
     *         - code: 500 管理员权限设置失败
     */
    @PutMapping("/{groupId}/admin")
    public Result<Void> setGroupAdmin(@PathVariable Long groupId, @RequestParam Long userId, @RequestParam Boolean isAdmin) {
        try {
            ImGroup group = imGroupService.selectImGroupById(groupId);
            if (group != null) {
                String newRole = isAdmin ? "ADMIN" : "MEMBER";
                int updateResult = imGroupMemberService.updateGroupMemberRole(groupId, userId, newRole, null);
                
                if (updateResult > 0) {
                    return Result.success("管理员权限设置成功");
                } else {
                    return Result.error(500, "管理员权限设置失败");
                }
            } else {
                return Result.error(404, "群组不存在");
            }
        } catch (Exception e) {
            return Result.error(500, "设置管理员权限失败: " + e.getMessage());
        }
    }

    /**
     * 退出群组
     * 
     * 用户主动退出指定群组，群主不能退出群组只能解散
     * 
     * @param groupId 要退出的群组ID
     * @param userId 要退出的用户ID
     * @return 包含操作结果的响应
     *         - code: 200 退出群组成功
     *         - code: 400 群主不能退出群组
     *         - code: 404 群组不存在或用户不在群组中
     *         - code: 500 退出群组失败
     */
    @PutMapping("/{groupId}/exit")
    public Result<Void> exitGroup(@PathVariable Long groupId, @RequestParam Long userId) {
        try {
            ImGroup group = imGroupService.selectImGroupById(groupId);
            if (group != null) {
                ImGroupMember member = imGroupMemberService.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
                
                if (member != null) {
                    // 群主不能退出群组，只能解散
                    if ("OWNER".equals(member.getRole())) {
                        return Result.error(400, "群主不能退出群组，请解散群组");
                    }
                    
                    int deleteResult = imGroupMemberService.removeGroupMember(groupId, userId, null);
                    
                    if (deleteResult > 0) {
                        return Result.success("已退出群组");
                    } else {
                        return Result.error(500, "退出群组失败");
                    }
                } else {
                    return Result.error(404, "用户不在群组中");
                }
            } else {
                return Result.error(404, "群组不存在");
            }
        } catch (Exception e) {
            return Result.error(500, "退出群组失败: " + e.getMessage());
        }
    }

    /**
     * 修改群昵称
     * 
     * 群成员修改自己在群组中的显示昵称
     * 
     * @param groupId 目标群组ID
     * @param userId 要修改昵称的用户ID
     * @param groupNickname 新的群昵称
     * @return 包含操作结果的响应
     *         - code: 200 群昵称修改成功
     *         - code: 404 用户不在群组中
     *         - code: 500 群昵称修改失败
     */
    @PutMapping("/{groupId}/nickname")
    public Result<Void> modifyGroupNickname(@PathVariable Long groupId, 
                                           @RequestParam Long userId, 
                                           @RequestParam String groupNickname) {
        try {
            ImGroupMember member = imGroupMemberService.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
            if (member != null) {
                member.setGroupNickname(groupNickname);
                int updateResult = imGroupMemberService.update(member);
                
                if (updateResult > 0) {
                    return Result.success("群昵称修改成功");
                } else {
                    return Result.error(500, "群昵称修改失败");
                }
            } else {
                return Result.error(404, "用户不在群组中");
            }
        } catch (Exception e) {
            return Result.error(500, "修改群昵称失败: " + e.getMessage());
        }
    }
}