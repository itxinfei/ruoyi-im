package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.dto.contact.ImFriendUpdateRequest;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 联系人控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/contact")
public class ImContactController {

    @Autowired
    private ImFriendService imFriendService;

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public Result<List<ImUserVO>> search(@RequestParam String keyword,
                                         @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImUserVO> list = imFriendService.searchUsers(keyword, userId);
        return Result.success(list);
    }

    /**
     * 发送好友申请
     */
    @PostMapping("/request/send")
    public Result<Long> sendRequest(@Valid @RequestBody ImFriendAddRequest request,
                                     @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        Long requestId = imFriendService.sendFriendRequest(request, userId);
        return Result.success("发送成功", requestId);
    }

    /**
     * 获取收到的好友申请列表
     */
    @GetMapping("/request/received")
    public Result<List<ImFriendRequest>> getReceivedRequests(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImFriendRequest> list = imFriendService.getReceivedRequests(userId);
        return Result.success(list);
    }

    /**
     * 获取发送的好友申请列表
     */
    @GetMapping("/request/sent")
    public Result<List<ImFriendRequest>> getSentRequests(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImFriendRequest> list = imFriendService.getSentRequests(userId);
        return Result.success(list);
    }

    /**
     * 处理好友申请
     */
    @PostMapping("/request/{id}/handle")
    public Result<Void> handleRequest(@PathVariable Long id,
                                      @RequestParam Boolean approved,
                                      @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imFriendService.handleFriendRequest(id, approved, userId);
        return Result.success(approved ? "已同意" : "已拒绝");
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    public Result<List<ImFriendVO>> getFriendList(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImFriendVO> list = imFriendService.getFriendList(userId);
        return Result.success(list);
    }

    /**
     * 获取分组好友列表
     */
    @GetMapping("/grouped")
    public Result<List<ImContactGroupVO>> getGroupedFriendList(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImContactGroupVO> list = imFriendService.getGroupedFriendList(userId);
        return Result.success(list);
    }

    /**
     * 获取好友详情
     */
    @GetMapping("/{id}")
    public Result<ImFriendVO> getFriendById(@PathVariable Long id,
                                            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        ImFriendVO vo = imFriendService.getFriendById(id, userId);
        return Result.success(vo);
    }

    /**
     * 更新好友信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateFriend(@PathVariable Long id,
                                     @Valid @RequestBody ImFriendUpdateRequest request,
                                     @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imFriendService.updateFriend(id, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFriend(@PathVariable Long id,
                                     @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imFriendService.deleteFriend(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 拉黑/解除拉黑好友
     */
    @PutMapping("/{id}/block")
    public Result<Void> blockFriend(@PathVariable Long id,
                                    @RequestParam Boolean blocked,
                                    @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imFriendService.blockFriend(id, blocked, userId);
        return Result.success(blocked ? "已拉黑" : "已解除拉黑");
    }
}
