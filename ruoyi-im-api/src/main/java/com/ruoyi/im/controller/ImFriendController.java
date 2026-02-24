package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 好友管理控制器
 */
@Tag(name = "好友管理")
@RestController
@RequestMapping("/api/im/friends")
@Validated
public class ImFriendController {

    private final ImFriendService friendService;
    private final ImUserService userService;

    public ImFriendController(ImFriendService friendService, ImUserService userService) {
        this.friendService = friendService;
        this.userService = userService;
    }

    @Operation(summary = "搜索非好友用户")
    @GetMapping("/search")
    public Result<List<ImUserVO>> search(@RequestParam @NotBlank String keyword) {
        // TODO: 实现搜索非好友用户逻辑
        return Result.success();
    }

    @Operation(summary = "获取好友列表")
    @GetMapping("/list")
    public Result<List<ImFriendVO>> getFriendList() {
        return Result.success(friendService.getFriendList(SecurityUtils.getLoginUserId()));
    }

    @Operation(summary = "获取好友详情")
    @GetMapping("/{friendId}")
    public Result<ImFriendVO> getDetail(@PathVariable @Positive Long friendId) {
        // TODO: 实现获取详情逻辑
        return Result.success();
    }

    @Operation(summary = "删除好友")
    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(@PathVariable @Positive Long friendId) {
        friendService.deleteFriend(SecurityUtils.getLoginUserId(), friendId);
        return Result.success("删除成功");
    }

    @Operation(summary = "修改好友备注")
    @PutMapping("/{friendId}/remark")
    public Result<Void> updateRemark(@PathVariable @Positive Long friendId, @RequestParam String remark) {
        friendService.updateFriendRemark(SecurityUtils.getLoginUserId(), friendId, remark);
        return Result.success("备注修改成功");
    }

    @Operation(summary = "拉黑/取消拉黑")
    @PutMapping("/{friendId}/block")
    public Result<Void> block(@PathVariable @Positive Long friendId, @RequestParam boolean blocked) {
        // TODO: 实现拉黑逻辑
        return Result.success();
    }

    @Operation(summary = "清空好友列表缓存")
    @PostMapping("/cache/clear")
    public Result<Void> clearCache() {
        // 前端缓存清除，无需后端处理
        return Result.success("缓存已清除");
    }

    @Operation(summary = "获取推荐好友")
    @GetMapping("/recommended")
    public Result<List<ImUserVO>> recommended(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        // TODO: 实现推荐好友逻辑
        return Result.success();
    }
}
