package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.session.ImSessionUpdateRequest;
import com.ruoyi.im.service.ImSessionService;
import com.ruoyi.im.vo.session.ImSessionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 会话控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/session")
public class ImSessionController {

    @Autowired
    private ImSessionService imSessionService;

    /**
     * 获取当前用户会话列表
     */
    @GetMapping("/list")
    public Result<List<ImSessionVO>> getList(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImSessionVO> list = imSessionService.getSessionList(userId);
        return Result.success(list);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/{id}")
    public Result<ImSessionVO> getById(@PathVariable Long id) {
        ImSessionVO vo = imSessionService.getSessionById(id);
        return Result.success(vo);
    }

    /**
     * 更新会话信息
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                              @Valid @RequestBody ImSessionUpdateRequest request) {
        imSessionService.updateSession(id, request);
        return Result.success("更新成功");
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imSessionService.deleteSession(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 清空未读消息数
     */
    @PutMapping("/{id}/read")
    public Result<Void> clearUnread(@PathVariable Long id,
                                   @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imSessionService.clearUnread(id, userId);
        return Result.success("已清空未读消息");
    }

    /**
     * 置顶/取消置顶会话
     */
    @PutMapping("/{id}/pin")
    public Result<Void> togglePin(@PathVariable Long id,
                                 @RequestParam Integer pinned) {
        imSessionService.togglePin(id, pinned);
        return Result.success(pinned == 1 ? "已置顶" : "已取消置顶");
    }

    /**
     * 免打扰/取消免打扰会话
     */
    @PutMapping("/{id}/mute")
    public Result<Void> toggleMute(@PathVariable Long id,
                                   @RequestParam Integer muted) {
        imSessionService.toggleMute(id, muted);
        return Result.success(muted == 1 ? "已设为免打扰" : "已取消免打扰");
    }
}
