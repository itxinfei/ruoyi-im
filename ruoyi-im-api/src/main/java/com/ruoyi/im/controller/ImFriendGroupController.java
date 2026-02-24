package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.contact.FriendTagsUpdateRequest;
import com.ruoyi.im.dto.contact.MoveToGroupRequest;
import com.ruoyi.im.service.ImFriendGroupService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 好友分组与标签控制器
 */
@Tag(name = "好友分组")
@RestController
@RequestMapping("/api/im/friend-groups")
@Validated
public class ImFriendGroupController {

    private final ImFriendGroupService friendGroupService;

    public ImFriendGroupController(ImFriendGroupService friendGroupService) {
        this.friendGroupService = friendGroupService;
    }

    @Operation(summary = "获取分组好友列表")
    @GetMapping("/list")
    public Result<List<ImContactGroupVO>> getGroupedFriendList() {
        return Result.success(friendGroupService.getGroupedFriendList(SecurityUtils.getLoginUserId()));
    }

    @Operation(summary = "获取分组名称列表")
    @GetMapping("/names")
    public Result<List<String>> getGroupNames() {
        return Result.success(friendGroupService.getGroupNames(SecurityUtils.getLoginUserId()));
    }

    @Operation(summary = "重命名分组")
    @PutMapping("/rename")
    public Result<Void> renameGroup(@RequestParam String oldName, @RequestParam String newName) {
        friendGroupService.renameGroup(SecurityUtils.getLoginUserId(), oldName, newName);
        return Result.success("重命名成功");
    }

    @Operation(summary = "移动好友到分组")
    @PutMapping("/move")
    public Result<Void> moveFriendsToGroup(@RequestBody @Valid MoveToGroupRequest request) {
        friendGroupService.moveFriendsToGroup(SecurityUtils.getLoginUserId(), request.getFriendIds(), request.getGroupName());
        return Result.success("移动成功");
    }

    @Operation(summary = "更新好友标签")
    @PutMapping("/{friendId}/tags")
    public Result<Void> updateFriendTags(@PathVariable @Positive Long friendId, @RequestBody @Valid FriendTagsUpdateRequest request) {
        friendGroupService.updateFriendTags(SecurityUtils.getLoginUserId(), friendId, request.getTags());
        return Result.success("标签更新成功");
    }
}
