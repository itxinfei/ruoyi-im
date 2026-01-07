package com.ruoyi.im.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.dto.conversation.ImConversationMemberUpdateRequest;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.vo.conversation.ImConversationMemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 会话成员Controller
 *
 * 用于管理用户与会话的关系，包括会话列表、未读消息数、置顶、免打扰等功能
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/im/conversationMember")
public class ImConversationMemberController extends BaseController {

    @Autowired
    private ImConversationMemberService conversationMemberService;

    /**
     * 查询会话成员列表
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:list')")
    @GetMapping("/list")
    public TableDataInfo list(ImConversationMember conversationMember) {
        startPage();
        List<ImConversationMemberVO> list = conversationMemberService.getConversationMemberList(conversationMember.getUserId());
        return getDataTable(list);
    }

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping("/myList")
    public AjaxResult getMyConversationList() {
        Long userId = getUserId();
        List<ImConversationMemberVO> list = conversationMemberService.getConversationMemberList(userId);
        return AjaxResult.success(list);
    }

    /**
     * 导出会话成员列表
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:export')")
    @Log(title = "会话成员", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImConversationMember conversationMember) {
        List<ImConversationMemberVO> list = conversationMemberService.getConversationMemberList(conversationMember.getUserId());
        ExcelUtil<ImConversationMemberVO> util = new ExcelUtil<>(ImConversationMemberVO.class);
        util.exportExcel(response, list, "会话成员数据");
    }

    /**
     * 获取会话成员详细信息
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:query')")
    @GetMapping(value = "/{conversationId}")
    public AjaxResult getInfo(@PathVariable("conversationId") Long conversationId) {
        Long userId = getUserId();
        ImConversationMemberVO vo = conversationMemberService.getConversationMember(conversationId, userId);
        return AjaxResult.success(vo);
    }

    /**
     * 更新会话成员信息
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:edit')")
    @Log(title = "会话成员", businessType = BusinessType.UPDATE)
    @PutMapping("/{conversationId}")
    public AjaxResult edit(@PathVariable Long conversationId, @RequestBody ImConversationMemberUpdateRequest request) {
        Long userId = getUserId();
        conversationMemberService.updateConversationMember(conversationId, userId, request);
        return AjaxResult.success();
    }

    /**
     * 删除会话成员
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:remove')")
    @Log(title = "会话成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{conversationId}")
    public AjaxResult remove(@PathVariable Long conversationId) {
        Long userId = getUserId();
        conversationMemberService.deleteConversationMember(conversationId, userId);
        return AjaxResult.success();
    }

    /**
     * 清除未读消息数
     */
    @Log(title = "清除未读消息", businessType = BusinessType.UPDATE)
    @PutMapping("/{conversationId}/clearUnread")
    public AjaxResult clearUnread(@PathVariable Long conversationId) {
        Long userId = getUserId();
        conversationMemberService.clearUnread(conversationId, userId);
        return AjaxResult.success();
    }

    /**
     * 切换置顶状态
     */
    @Log(title = "切换置顶状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{conversationId}/togglePin")
    public AjaxResult togglePin(@PathVariable Long conversationId, @RequestParam Integer pinned) {
        Long userId = getUserId();
        conversationMemberService.togglePin(conversationId, userId, pinned);
        return AjaxResult.success();
    }

    /**
     * 切换免打扰状态
     */
    @Log(title = "切换免打扰状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{conversationId}/toggleMute")
    public AjaxResult toggleMute(@PathVariable Long conversationId, @RequestParam Integer muted) {
        Long userId = getUserId();
        conversationMemberService.toggleMute(conversationId, userId, muted);
        return AjaxResult.success();
    }
}
