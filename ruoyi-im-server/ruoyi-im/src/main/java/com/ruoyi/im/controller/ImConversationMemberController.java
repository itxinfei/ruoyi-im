package com.ruoyi.im.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.service.IImConversationMemberService;

/**
 * IM会话成员Controller
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
@RestController
@RequestMapping("/im/conversationMember")
public class ImConversationMemberController extends BaseController
{
    @Autowired
    private IImConversationMemberService imConversationMemberService;

    /**
     * 查询IM会话成员列表
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:list')")
    @GetMapping("/list")
    public TableDataInfo list(ImConversationMember imConversationMember)
    {
        startPage();
        List<ImConversationMember> list = imConversationMemberService.list();
        return getDataTable(list);
    }

    /**
     * 导出IM会话成员列表
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:export')")
    @Log(title = "IM会话成员", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImConversationMember imConversationMember)
    {
        List<ImConversationMember> list = imConversationMemberService.list();
        ExcelUtil<ImConversationMember> util = new ExcelUtil<ImConversationMember>(ImConversationMember.class);
        util.exportExcel(response, list, "IM会话成员数据");
    }

    /**
     * 获取IM会话成员详细信息
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(imConversationMemberService.getById(id));
    }

    /**
     * 新增IM会话成员
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:add')")
    @Log(title = "IM会话成员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImConversationMember imConversationMember)
    {
        return toAjax(imConversationMemberService.save(imConversationMember) ? 1 : 0);
    }

    /**
     * 修改IM会话成员
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:edit')")
    @Log(title = "IM会话成员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImConversationMember imConversationMember)
    {
        return toAjax(imConversationMemberService.updateById(imConversationMember) ? 1 : 0);
    }

    /**
     * 删除IM会话成员
     */
    @PreAuthorize("@ss.hasPermi('im:conversationMember:remove')")
    @Log(title = "IM会话成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(imConversationMemberService.removeByIds(java.util.Arrays.asList(ids)) ? 1 : 0);
    }

    /**
     * 添加会话成员
     */
    @PostMapping("/add/{conversationId}")
    public AjaxResult addConversationMember(@PathVariable Long conversationId, @RequestBody List<Long> userIds)
    {
        return toAjax(imConversationMemberService.addConversationMembers(conversationId, userIds));
    }

    /**
     * 移除会话成员
     */
    @DeleteMapping("/remove/{conversationId}/{userId}")
    public AjaxResult removeConversationMember(@PathVariable Long conversationId, @PathVariable Long userId)
    {
        return toAjax(imConversationMemberService.removeConversationMembers(conversationId, java.util.Arrays.asList(userId)));
    }

    /**
     * 查询会话成员列表
     */
    @GetMapping("/members/{conversationId}")
    public AjaxResult getConversationMembers(@PathVariable Long conversationId)
    {
        List<ImConversationMember> members = imConversationMemberService.selectConversationMembers(conversationId);
        return success(members);
    }

    /**
     * 检查成员身份
     */
    @GetMapping("/checkMember/{conversationId}")
    public AjaxResult checkConversationMember(@PathVariable Long conversationId)
    {
        boolean isMember = imConversationMemberService.isConversationMember(conversationId, getCurrentUserId());
        return success(isMember);
    }

    /**
     * 获取成员信息
     */
    @GetMapping("/info/{conversationId}/{userId}")
    public AjaxResult getMemberInfo(@PathVariable Long conversationId, @PathVariable Long userId)
    {
        ImConversationMember member = imConversationMemberService.getConversationMember(conversationId, userId);
        return success(member);
    }

    /**
     * 统计会话成员数量
     */
    @GetMapping("/count/{conversationId}")
    public AjaxResult countConversationMembers(@PathVariable Long conversationId)
    {
        int count = imConversationMemberService.countConversationMembers(conversationId);
        return success(count);
    }

    /**
     * 更新最后已读消息
     */
    @PutMapping("/lastRead/{conversationId}")
    public AjaxResult updateLastReadMessage(@PathVariable Long conversationId, @RequestBody ImConversationMember member)
    {
        return toAjax(imConversationMemberService.updateLastReadMessage(conversationId, getCurrentUserId(), member.getLastReadMessageId()) ? 1 : 0);
    }

    /**
     * 设置置顶状态
     */
    @PutMapping("/pin/{conversationId}")
    public AjaxResult setPinnedStatus(@PathVariable Long conversationId, @RequestBody ImConversationMember member)
    {
        return toAjax(imConversationMemberService.setPinnedConversation(conversationId, getCurrentUserId(), member.getPinned()) ? 1 : 0);
    }

    /**
     * 设置免打扰状态
     */
    @PutMapping("/mute/{conversationId}")
    public AjaxResult setMutedStatus(@PathVariable Long conversationId, @RequestBody ImConversationMember member)
    {
        return toAjax(imConversationMemberService.setMutedConversation(conversationId, getCurrentUserId(), member.getMuted()) ? 1 : 0);
    }

    /**
     * 查询用户的会话ID列表
     */
    @GetMapping("/userConversations")
    public AjaxResult getUserConversationIds()
    {
        List<Long> conversationIds = imConversationMemberService.selectUserConversationIds(getCurrentUserId());
        return success(conversationIds);
    }

    /**
     * 统计未读消息数量
     */
    @GetMapping("/unreadCount/{conversationId}")
    public AjaxResult countUnreadMessages(@PathVariable Long conversationId)
    {
        int count = imConversationMemberService.countUserUnreadMessages(getCurrentUserId());
        return success(count);
    }

    /**
     * 查询置顶会话
     */
    @GetMapping("/pinned")
    public AjaxResult getPinnedConversations()
    {
        List<ImConversationMember> pinnedConversations = imConversationMemberService.selectPinnedConversations(getCurrentUserId());
        return success(pinnedConversations);
    }

    /**
     * 批量更新最后已读消息
     */
    @PutMapping("/batchLastRead")
    public AjaxResult batchUpdateLastReadMessage(@RequestBody List<ImConversationMember> members)
    {
        // 需要根据实际业务逻辑实现批量更新
        // 这里暂时返回成功，具体实现需要根据业务需求调整
        return success();
    }

    /**
     * 检查会话是否免打扰
     */
    @GetMapping("/checkMute/{conversationId}")
    public AjaxResult checkConversationMuted(@PathVariable Long conversationId)
    {
        boolean isMuted = imConversationMemberService.isConversationMuted(conversationId, getCurrentUserId());
        return success(isMuted);
    }

    /**
     * 检查会话是否置顶
     */
    @GetMapping("/checkPin/{conversationId}")
    public AjaxResult checkConversationPinned(@PathVariable Long conversationId)
    {
        boolean isPinned = imConversationMemberService.isConversationPinned(conversationId, getCurrentUserId());
        return success(isPinned);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId()
    {
        return getUserId();
    }
}