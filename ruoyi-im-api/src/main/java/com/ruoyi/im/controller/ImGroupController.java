package com.ruoyi.im.controller;

import com.github.pagehelper.PageInfo;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.im.controller.BaseController;
import com.ruoyi.im.annotation.SwaggerTag;
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
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 群组Controller
 * 
 * @author ruoyi
 */
@SwaggerTag(description = "群组管理")
@RestController
@RequestMapping("/api/im/group")
public class ImGroupController extends BaseController {
    
    @Autowired
    private ImGroupService imGroupService;
    
    @Autowired
    private ImGroupMemberService imGroupMemberService;
    
    @Autowired
    private ImUserService imUserService;

    /**
     * 查询群组列表
     */
    @ApiOperation("查询群组列表")
    @GetMapping("/list")
    public Result<PageResult<ImGroupVO>> list(ImGroupQueryRequest request) {
        // 设置分页参数
        startPage(request.getPageNum(), request.getPageSize());
        
        // 查询群组列表
        List<ImGroup> groups = imGroupService.selectImGroupList(request);
        
        // 转换为VO并返回分页结果
        List<ImGroupVO> groupVOs = groups.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return getDataTable(groupVOs);
    }

    /**
     * 获取群组详细信息
     */
    @ApiOperation("获取群组详细信息")
    @GetMapping("/{groupId}")
    public Result<ImGroupVO> getGroup(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId) {
        ImGroup group = imGroupService.selectById(groupId);
        if (group == null) {
            return Result.error(404, "群组不存在");
        }
        
        ImGroupVO groupVO = convertToVO(group);
        return Result.success(groupVO);
    }

    /**
     * 新增群组
     */
    @ApiOperation("新增群组")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ImGroupCreateRequest request, BindingResult bindingResult) {
        // 验证参数
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return validationResult;
        }
        
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 创建群组
        ImGroup group = new ImGroup();
        group.setName(request.getName());
        group.setNotice(request.getNotice());
        group.setOwnerId(currentUserId); // 设置创建者为群主
        group.setAvatar(request.getAvatar());
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        
        int result = imGroupService.insert(group);
        
        if (result > 0) {
            // 添加群主为群成员
            ImGroupMember member = new ImGroupMember();
            member.setGroupId(group.getId());
            member.setUserId(currentUserId);
            member.setRole("owner"); // 群主角色
            member.setJoinedTime(LocalDateTime.now());
            
            imGroupMemberService.insert(member);
            
            return Result.success(201, "群组创建成功", null);
        } else {
            return Result.error("群组创建失败");
        }
    }

    /**
     * 修改群组
     */
    @ApiOperation("修改群组")
    @PutMapping("/{groupId}")
    public Result<Void> edit(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId,
                             @Valid @RequestBody ImGroupUpdateRequest request, BindingResult bindingResult) {
        // 验证参数
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return validationResult;
        }
        
        ImGroup existingGroup = imGroupService.selectById(groupId);
        if (existingGroup == null) {
            return Result.error(404, "群组不存在");
        }
        
        // 检查权限（只有群主才能修改群组信息）
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !existingGroup.getOwnerId().equals(currentUserId)) {
            return Result.error(403, "没有权限修改此群组");
        }
        
        // 更新群组信息
        existingGroup.setName(request.getName());
        existingGroup.setNotice(request.getNotice());
        existingGroup.setAvatar(request.getAvatar());
        existingGroup.setUpdateTime(LocalDateTime.now());
        
        int result = imGroupService.update(existingGroup);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error(500, "群组信息更新失败");
        }
    }

    /**
     * 删除群组
     */
    @ApiOperation("删除群组")
    @DeleteMapping("/{groupId}")
    public Result<Void> delete(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId) {
        ImGroup group = imGroupService.selectById(groupId);
        if (group == null) {
            return Result.error(404, "群组不存在");
        }
        
        // 检查权限（只有群主才能删除群组）
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !group.getOwnerId().equals(currentUserId)) {
            return Result.error(403, "没有权限删除此群组");
        }
        
        int result = imGroupService.deleteById(groupId);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error(500, "群组删除失败");
        }
    }

    /**
     * 解散群组
     */
    @ApiOperation("解散群组")
    @PostMapping("/{groupId}/dissolve")
    public Result<Void> dissolveGroup(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId) {
        ImGroup group = imGroupService.selectById(groupId);
        if (group == null) {
            return Result.error(404, "群组不存在");
        }
        
        // 检查权限（只有群主才能解散群组）
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !group.getOwnerId().equals(currentUserId)) {
            return Result.error(403, "没有权限解散此群组");
        }
        
        // 删除群组及其所有成员
        int groupResult = imGroupService.deleteById(groupId);
        int memberResult = imGroupMemberService.deleteByGroupId(groupId);
        
        if (groupResult > 0) {
            return Result.success();
        } else {
            return Result.error(500, "解散群组失败");
        }
    }

    /**
     * 获取群组成员列表
     */
    @ApiOperation("获取群组成员列表")
    @GetMapping("/{groupId}/members")
    public Result<List<ImGroupMemberVO>> getGroupMembers(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId) {
        // 检查群组是否存在
        ImGroup group = imGroupService.selectById(groupId);
        if (group == null) {
            return Result.error(404, "群组不存在");
        }
        
        // 获取群组成员列表
        List<ImGroupMember> members = imGroupMemberService.selectByGroupId(groupId);
        
        // 查询用户信息并转换为VO
        List<ImGroupMemberVO> memberVOs = members.stream()
            .map(member -> {
                ImGroupMemberVO vo = new ImGroupMemberVO();
                vo.setUserId(member.getUserId());
                vo.setGroupId(member.getGroupId());
                vo.setRole(member.getRole());
                vo.setJoinedTime(member.getJoinedTime());
                
                // 查询用户信息
                ImUser user = imUserService.selectById(member.getUserId());
                if (user != null) {
                    vo.setNickname(user.getNickname());
                    vo.setAvatar(user.getAvatar());
                }
                
                return vo;
            })
            .collect(Collectors.toList());
        
        return Result.success(memberVOs);
    }

    /**
     * 添加群组成员
     */
    @ApiOperation("添加群组成员")
    @PostMapping("/{groupId}/members")
    public Result<Void> addGroupMembers(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId,
                                        @Valid @RequestBody ImGroupMembersRequest request, BindingResult bindingResult) {
        // 验证参数
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return validationResult;
        }
        
        // 检查群组是否存在
        ImGroup group = imGroupService.selectById(groupId);
        if (group == null) {
            return Result.error(404, "群组不存在");
        }
        
        // 检查权限（群主或管理员可以添加成员）
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 检查是否为群主或管理员
        boolean canAddMembers = isGroupOwnerOrAdmin(currentUserId, groupId);
        if (!canAddMembers) {
            return Result.error(403, "没有权限添加群成员");
        }
        
        // 添加群成员
        int addedCount = 0;
        for (Long userId : request.getUserIds()) {
            // 检查用户是否存在
            ImUser user = imUserService.selectById(userId);
            if (user == null) {
                continue; // 跳过不存在的用户
            }
            
            // 检查用户是否已经是群成员
            ImGroupMember existingMember = imGroupMemberService.selectByGroupIdAndUserId(groupId, userId);
            if (existingMember != null) {
                continue; // 跳过已存在的成员
            }
            
            // 添加群成员
            ImGroupMember member = new ImGroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole("member"); // 默认为普通成员
            member.setJoinedTime(LocalDateTime.now());
            
            imGroupMemberService.insert(member);
            addedCount++;
        }
        
        return Result.success();
    }

    /**
     * 移除群组成员
     */
    @ApiOperation("移除群组成员")
    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<Void> removeGroupMember(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId,
                                          @PathVariable @Positive(message = "用户ID必须为正数") Long userId) {
        // 检查群组是否存在
        ImGroup group = imGroupService.selectById(groupId);
        if (group == null) {
            return Result.error(404, "群组不存在");
        }
        
        // 检查用户是否存在
        ImUser user = imUserService.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 检查权限（群主或管理员可以移除成员，或者用户自己退出）
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "用户未登录");
        }
        
        boolean canRemoveMember = currentUserId.equals(userId) || // 用户自己退出
                                  isGroupOwner(currentUserId, groupId) || // 群主操作
                                  isGroupAdmin(currentUserId, groupId); // 管理员操作
                                  
        if (!canRemoveMember) {
            return Result.error(403, "没有权限移除该群成员");
        }
        
        // 检查是否为群主（群主不能被移除，只能解散群组）
        if (group.getOwnerId().equals(userId)) {
            return Result.error(403, "不能移除群主");
        }
        
        int result = imGroupMemberService.deleteByGroupIdAndUserId(groupId, userId);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error(500, "群成员移除失败");
        }
    }

    /**
     * 设置群组管理员
     */
    @ApiOperation("设置群组管理员")
    @PostMapping("/{groupId}/set-admin/{userId}")
    public Result<Void> setGroupAdmin(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId,
                                      @PathVariable @Positive(message = "用户ID必须为正数") Long userId) {
        // 检查权限（只有群主可以设置管理员）
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !isGroupOwner(currentUserId, groupId)) {
            return Result.error(403, "只有群主可以设置管理员");
        }
        
        // 检查用户是否为群成员
        ImGroupMember member = imGroupMemberService.selectByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            return Result.error(404, "用户不是群成员");
        }
        
        // 设置为管理员
        member.setRole("admin");
        member.setUpdateTime(LocalDateTime.now());
        
        int result = imGroupMemberService.update(member);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error(500, "设置群管理员失败");
        }
    }

    /**
     * 取消群组管理员
     */
    @ApiOperation("取消群组管理员")
    @PostMapping("/{groupId}/unset-admin/{userId}")
    public Result<Void> unsetGroupAdmin(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId,
                                        @PathVariable @Positive(message = "用户ID必须为正数") Long userId) {
        // 检查权限（只有群主可以取消管理员）
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !isGroupOwner(currentUserId, groupId)) {
            return Result.error(403, "只有群主可以取消管理员");
        }
        
        // 检查用户是否为群成员
        ImGroupMember member = imGroupMemberService.selectByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            return Result.error(404, "用户不是群成员");
        }
        
        // 取消管理员（设置为普通成员）
        member.setRole("member");
        member.setUpdateTime(LocalDateTime.now());
        
        int result = imGroupMemberService.update(member);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error(500, "取消群管理员失败");
        }
    }

    /**
     * 获取群组统计信息
     */
    @ApiOperation("获取群组统计信息")
    @GetMapping("/{groupId}/stats")
    public Result<Map<String, Object>> getGroupStats(@PathVariable @Positive(message = "群组ID必须为正数") Long groupId) {
        ImGroup group = imGroupService.selectById(groupId);
        if (group == null) {
            return Result.error(404, "群组不存在");
        }
        
        // 获取群成员数量
        int memberCount = imGroupMemberService.countByGroupId(groupId);
        
        // 构建统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("memberCount", memberCount);
        stats.put("memberLimit", group.getMemberLimit());
        stats.put("ownerId", group.getOwnerId());
        stats.put("groupName", group.getName());
        stats.put("description", group.getDescription());
        stats.put("type", group.getType());
        stats.put("createTime", group.getCreateTime());
        
        return Result.success(stats);
    }

    // 工具方法
    private ImGroupVO convertToVO(ImGroup group) {
        ImGroupVO vo = new ImGroupVO();
        vo.setId(group.getId());
        vo.setGroupName(group.getName());
        vo.setGroupDesc(group.getDescription());
        vo.setOwnerId(group.getOwnerId());
        vo.setGroupType(group.getType());
        vo.setMemberLimit(group.getMemberLimit());
        vo.setAvatar(group.getAvatar());
        vo.setCreateTime(group.getCreateTime());
        vo.setUpdateTime(group.getUpdateTime());
        
        // 设置群主信息
        ImUser owner = imUserService.selectById(group.getOwnerId());
        if (owner != null) {
            vo.setOwnerName(owner.getNickname());
            vo.setOwnerAvatar(owner.getAvatar());
        }
        
        return vo;
    }

    private boolean isGroupOwner(Long userId, Long groupId) {
        ImGroup group = imGroupService.selectById(groupId);
        return group != null && group.getOwnerId().equals(userId);
    }

    private boolean isGroupAdmin(Long userId, Long groupId) {
        ImGroupMember member = imGroupMemberService.selectByGroupIdAndUserId(groupId, userId);
        return member != null && "admin".equals(member.getRole());
    }

    private boolean isGroupOwnerOrAdmin(Long userId, Long groupId) {
        return isGroupOwner(userId, groupId) || isGroupAdmin(userId, groupId);
    }
}