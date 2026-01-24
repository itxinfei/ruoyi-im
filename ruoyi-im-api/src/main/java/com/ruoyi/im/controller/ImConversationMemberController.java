package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.dto.conversation.ImConversationMemberUpdateRequest;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.conversation.ImConversationMemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话成员Controller
 *
 * 用于管理用户与会话的关系，包括会话列表、未读消息数、置顶、免打扰等功能
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/conversationMember")
public class ImConversationMemberController {

    @Autowired
    private ImConversationMemberService conversationMemberService;

    /**
     * 查询会话成员列表
     */
    @GetMapping("/list")
    public Result<List<ImConversationMemberVO>> list(ImConversationMember conversationMember) {
        List<ImConversationMemberVO> list = conversationMemberService
                .getConversationMemberList(conversationMember.getUserId());
        return Result.success(list);
    }

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping("/myList")
    public Result<List<ImConversationMemberVO>> getMyConversationList() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImConversationMemberVO> list = conversationMemberService.getConversationMemberList(userId);
        return Result.success(list);
    }

    /**
     * 获取会话成员详细信息
     */
    @GetMapping(value = "/{conversationId}")
    public Result<ImConversationMemberVO> getInfo(@PathVariable("conversationId") Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        ImConversationMemberVO vo = conversationMemberService.getConversationMember(conversationId, userId);
        return Result.success(vo);
    }

    /**
     * 更新会话成员信息
     */
    @PutMapping("/{conversationId}")
    public Result<Void> edit(@PathVariable Long conversationId,
            @RequestBody ImConversationMemberUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        conversationMemberService.updateConversationMember(conversationId, userId, request);
        return Result.success();
    }

    /**
     * 删除会话成员
     */
    @DeleteMapping("/{conversationId}")
    public Result<Void> remove(@PathVariable Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        conversationMemberService.deleteConversationMember(conversationId, userId);
        return Result.success();
    }

    /**
     * 清除未读消息数
     */
    @PutMapping("/{conversationId}/clearUnread")
    public Result<Void> clearUnread(@PathVariable Long conversationId) {
        Long userId = SecurityUtils.getLoginUserId();
        conversationMemberService.clearUnread(conversationId, userId);
        return Result.success();
    }

    /**
     * 切换置顶状态
     */
    @PutMapping("/{conversationId}/togglePin")
    public Result<Void> togglePin(@PathVariable Long conversationId, @RequestParam Integer pinned) {
        Long userId = SecurityUtils.getLoginUserId();
        conversationMemberService.togglePin(conversationId, userId, pinned);
        return Result.success();
    }

    /**
     * 切换免打扰状态
     */
    @PutMapping("/{conversationId}/toggleMute")
    public Result<Void> toggleMute(@PathVariable Long conversationId, @RequestParam Integer muted) {
        Long userId = SecurityUtils.getLoginUserId();
        conversationMemberService.toggleMute(conversationId, userId, muted);
        return Result.success();
    }
}
