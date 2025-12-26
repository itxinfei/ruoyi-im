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
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.service.IImGroupMemberService;

/**
 * IM群组成员Controller
 * 
 * @author ruoyi
 * @date 2024-08-09
 */
@RestController
@RequestMapping("/im/groupMember")
public class ImGroupMemberController extends BaseController
{
    @Autowired
    private IImGroupMemberService imGroupMemberService;

    /**
     * 查询IM群组成员列表
     */
    @PreAuthorize("@ss.hasPermi('im:groupMember:list')")
    @GetMapping("/list")
    public TableDataInfo list(ImGroupMember imGroupMember)
    {
        startPage();
        List<ImGroupMember> list = imGroupMemberService.list();
        return getDataTable(list);
    }

    /**
     * 导出IM群组成员列表
     */
    @PreAuthorize("@ss.hasPermi('im:groupMember:export')")
    @Log(title = "IM群组成员", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImGroupMember imGroupMember)
    {
        List<ImGroupMember> list = imGroupMemberService.list();
        ExcelUtil<ImGroupMember> util = new ExcelUtil<ImGroupMember>(ImGroupMember.class);
        util.exportExcel(response, list, "IM群组成员数据");
    }

    /**
     * 获取IM群组成员详细信息
     */
    @PreAuthorize("@ss.hasPermi('im:groupMember:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(imGroupMemberService.getById(id));
    }

    /**
     * 新增IM群组成员
     */
    @PreAuthorize("@ss.hasPermi('im:groupMember:add')")
    @Log(title = "IM群组成员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImGroupMember imGroupMember)
    {
        return toAjax(imGroupMemberService.save(imGroupMember));
    }

    /**
     * 修改IM群组成员
     */
    @PreAuthorize("@ss.hasPermi('im:groupMember:edit')")
    @Log(title = "IM群组成员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImGroupMember imGroupMember)
    {
        return toAjax(imGroupMemberService.updateById(imGroupMember));
    }

    /**
     * 删除IM群组成员
     */
    @PreAuthorize("@ss.hasPermi('im:groupMember:remove')")
    @Log(title = "IM群组成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        // TODO: 批量删除功能暂未实现
        return error("批量删除功能暂未实现");
    }

    /**
     * 添加群成员
     */
    @PostMapping("/add/{groupId}")
    public AjaxResult addGroupMember(@PathVariable Long groupId, @RequestBody List<Long> userIds)
    {
        return toAjax(imGroupMemberService.addGroupMembers(groupId, userIds, getCurrentUserId()));
    }

    /**
     * 移除群成员
     */
    @DeleteMapping("/remove/{groupId}/{userId}")
    public AjaxResult removeGroupMember(@PathVariable Long groupId, @PathVariable Long userId)
    {
        List<Long> userIds = java.util.Arrays.asList(userId);
        return toAjax(imGroupMemberService.removeGroupMembers(groupId, userIds, getCurrentUserId()));
    }

    /**
     * 退出群组
     */
    @PostMapping("/leave/{groupId}")
    public AjaxResult leaveGroup(@PathVariable Long groupId)
    {
        return toAjax(imGroupMemberService.leaveGroup(groupId, getCurrentUserId()));
    }

    /**
     * 查询群成员列表
     */
    @GetMapping("/members/{groupId}")
    public AjaxResult getGroupMembers(@PathVariable Long groupId)
    {
        List<ImGroupMember> members = imGroupMemberService.selectGroupMembers(groupId);
        return success(members);
    }

    /**
     * 查询群管理员
     */
    @GetMapping("/admins/{groupId}")
    public AjaxResult getGroupAdmins(@PathVariable Long groupId)
    {
        List<ImGroupMember> admins = imGroupMemberService.selectGroupAdmins(groupId);
        return success(admins);
    }

    /**
     * 设置管理员
     */
    @PostMapping("/setAdmin/{groupId}/{userId}")
    public AjaxResult setGroupAdmin(@PathVariable Long groupId, @PathVariable Long userId)
    {
        return toAjax(imGroupMemberService.setGroupAdmin(groupId, userId, true, getCurrentUserId()) ? 1 : 0);
    }

    /**
     * 取消管理员
     */
    @PostMapping("/unsetAdmin/{groupId}/{userId}")
    public AjaxResult unsetGroupAdmin(@PathVariable Long groupId, @PathVariable Long userId)
    {
        return toAjax(imGroupMemberService.setGroupAdmin(groupId, userId, false, getCurrentUserId()) ? 1 : 0);
    }

    /**
     * 禁言成员
     */
    @PostMapping("/mute/{groupId}/{userId}")
    public AjaxResult muteMember(@PathVariable Long groupId, @PathVariable Long userId, @RequestBody ImGroupMember member)
    {
        return toAjax(imGroupMemberService.muteGroupMember(groupId, userId, member.getMuteUntil(), getCurrentUserId()) ? 1 : 0);
    }

    /**
     * 取消禁言
     */
    @PostMapping("/unmute/{groupId}/{userId}")
    public AjaxResult unmuteMember(@PathVariable Long groupId, @PathVariable Long userId)
    {
        return toAjax(imGroupMemberService.muteGroupMember(groupId, userId, null, getCurrentUserId()) ? 1 : 0);
    }

    /**
     * 更新群昵称
     */
    @PutMapping("/nickname/{groupId}")
    public AjaxResult updateGroupNickname(@PathVariable Long groupId, @RequestBody ImGroupMember member)
    {
        return toAjax(imGroupMemberService.updateMemberNickname(groupId, getCurrentUserId(), member.getNickname()) ? 1 : 0);
    }

    /**
     * 检查成员身份
     */
    @GetMapping("/checkMember/{groupId}")
    public AjaxResult checkGroupMember(@PathVariable Long groupId)
    {
        boolean isMember = imGroupMemberService.isGroupMember(groupId, getCurrentUserId());
        return success(isMember);
    }

    /**
     * 获取成员信息
     */
    @GetMapping("/info/{groupId}/{userId}")
    public AjaxResult getMemberInfo(@PathVariable Long groupId, @PathVariable Long userId)
    {
        ImGroupMember member = imGroupMemberService.getGroupMember(groupId, userId);
        return success(member);
    }

    /**
     * 统计群成员数量
     */
    @GetMapping("/count/{groupId}")
    public AjaxResult countGroupMembers(@PathVariable Long groupId)
    {
        int count = imGroupMemberService.countGroupMembers(groupId);
        return success(count);
    }

    /**
     * 搜索群成员
     */
    @GetMapping("/search/{groupId}")
    public AjaxResult searchGroupMembers(@PathVariable Long groupId, String keyword)
    {
        List<ImGroupMember> members = imGroupMemberService.searchGroupMembers(groupId, keyword);
        return success(members);
    }

    /**
     * 查询用户的群组ID列表
     */
    @GetMapping("/userGroups")
    public AjaxResult getUserGroupIds()
    {
        List<Long> groupIds = imGroupMemberService.selectUserGroupIds(getCurrentUserId());
        return success(groupIds);
    }

    /**
     * 批量查询用户群组身份
     */
    @PostMapping("/batchQuery")
    public AjaxResult batchQueryUserMemberships(@RequestBody List<Long> groupIds)
    {
        List<ImGroupMember> memberships = imGroupMemberService.selectUserMemberships(getCurrentUserId(), groupIds);
        return success(memberships);
    }

    /**
     * 检查成员是否被禁言
     */
    @GetMapping("/checkMute/{groupId}/{userId}")
    public AjaxResult checkMemberMuted(@PathVariable Long groupId, @PathVariable Long userId)
    {
        boolean isMuted = imGroupMemberService.isMemberMuted(groupId, userId);
        return success(isMuted);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId()
    {
        return getUserId();
    }
}