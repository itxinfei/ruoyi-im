package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.service.ImFriendRequestService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.domain.ImFriendRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 好友申请控制器
 */
@Tag(name = "好友申请")
@RestController
@RequestMapping("/api/im/friend-requests")
@Validated
public class ImFriendRequestController {

    private final ImFriendRequestService friendRequestService;

    public ImFriendRequestController(ImFriendRequestService friendRequestService) {
        this.friendRequestService = friendRequestService;
    }

    @Operation(summary = "发送好友申请")
    @PostMapping("/send")
    public Result<Long> sendRequest(@Valid @RequestBody ImFriendAddRequest request) {
        return Result.success("申请已发送", friendRequestService.sendFriendRequest(request, SecurityUtils.getLoginUserId()));
    }

    @Operation(summary = "处理好友申请")
    @PostMapping("/{requestId}/handle")
    public Result<Void> handleRequest(@PathVariable @Positive Long requestId, @RequestParam @NotNull Boolean approved) {
        friendRequestService.processFriendRequest(requestId, approved, SecurityUtils.getLoginUserId());
        return Result.success("处理成功");
    }

    @Operation(summary = "获取收到的申请")
    @GetMapping("/received")
    public Result<List<ImFriendRequest>> getReceivedRequests() {
        return Result.success(friendRequestService.getReceivedRequests(SecurityUtils.getLoginUserId()));
    }

    @Operation(summary = "获取待处理申请")
    @GetMapping("/pending")
    public Result<List<ImFriendRequest>> getPendingRequests() {
        return Result.success(friendRequestService.getPendingRequests(SecurityUtils.getLoginUserId()));
    }
}
