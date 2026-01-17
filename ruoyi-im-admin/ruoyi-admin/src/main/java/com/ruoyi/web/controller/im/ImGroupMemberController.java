package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImGroupMember;
import com.ruoyi.web.service.ImGroupMemberService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 群组成员管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/member")
public class ImGroupMemberController extends BaseController {

    private String prefix = "im/member";

    @Autowired
    private ImGroupMemberService imGroupMemberService;

    @RequiresPermissions("im:group:member:view")
    @GetMapping()
    public String member() {
        return prefix + "/member";
    }

    /**
     * 获取群组成员统计数据
     */
    @RequiresPermissions("im:group:member:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imGroupMemberService.getMemberStatistics());
    }

    /**
     * 查询群组成员列表
     */
    @RequiresPermissions("im:group:member:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImGroupMember imGroupMember) {
        startPage();
        List<ImGroupMember> list = imGroupMemberService.selectImGroupMemberList(imGroupMember);
        return getDataTable(list);
    }

    /**
     * 获取群组成员详细信息
     */
    @RequiresPermissions("im:group:member:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imGroupMemberService.selectImGroupMemberById(id));
    }

    /**
     * 根据群组ID查询成员列表
     */
    @RequiresPermissions("im:group:member:query")
    @GetMapping("/group/{groupId}")
    @ResponseBody
    public AjaxResult getMembersByGroupId(@PathVariable("groupId") Long groupId) {
        return AjaxResult.success(imGroupMemberService.selectMembersByGroupId(groupId));
    }

    /**
     * 显示特定群组的成员列表页面
     */
    @RequiresPermissions("im:group:member:view")
    @GetMapping("/group/{groupId}/view")
    public String viewMembersByGroupId(@PathVariable("groupId") Long groupId, ModelMap mmap) {
        mmap.put("groupId", groupId);
        return prefix + "/membersByGroup";
    }

    /**
     * 统计群组成员数量
     */
    @RequiresPermissions("im:group:member:query")
    @GetMapping("/group/{groupId}/count")
    @ResponseBody
    public AjaxResult getMemberCount(@PathVariable("groupId") Long groupId) {
        return AjaxResult.success(imGroupMemberService.countMembersByGroupId(groupId));
    }

    /**
     * 查询用户加入的群组列表
     */
    @RequiresPermissions("im:group:member:query")
    @GetMapping("/user/{userId}")
    @ResponseBody
    public AjaxResult getUserGroups(@PathVariable("userId") Long userId) {
        return AjaxResult.success(imGroupMemberService.selectUserGroups(userId));
    }

    /**
     * 新增群组成员
     */
    @RequiresPermissions("im:group:member:add")
    @Log(title = "群组成员管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(ImGroupMember imGroupMember) {
        return toAjax(imGroupMemberService.insertImGroupMember(imGroupMember));
    }

    /**
     * 新增群组成员（带日志记录）
     */
    @RequiresPermissions("im:group:member:add")
    @Log(title = "群组成员管理", businessType = BusinessType.INSERT)
    @PostMapping("/addWithLog")
    @ResponseBody
    public AjaxResult addWithLog(@RequestParam Long groupId, @RequestParam Long userId, @RequestParam String nickname, @RequestParam String operatorName) {
        ImGroupMember member = new ImGroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setNickname(nickname);
        member.setRole("MEMBER");
        return toAjax(imGroupMemberService.insertImGroupMemberWithLog(member, userId, operatorName));
    }

    /**
     * 批量添加群组成员
     */
    @RequiresPermissions("im:group:member:add")
    @Log(title = "批量添加群组成员", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    @ResponseBody
    public AjaxResult addBatch(@RequestBody List<ImGroupMember> members) {
        int successCount = 0;
        for (ImGroupMember member : members) {
            successCount += imGroupMemberService.insertImGroupMember(member);
        }
        return AjaxResult.success("成功添加 " + successCount + " 个成员");
    }

    /**
     * 修改群组成员（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:group:member:edit")
    @Log(title = "群组成员管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImGroupMember imGroupMember) {
        return toAjax(imGroupMemberService.updateImGroupMember(imGroupMember));
    }

    /**
     * 修改成员角色
     */
    @RequiresPermissions("im:group:member:edit")
    @Log(title = "修改成员角色", businessType = BusinessType.UPDATE)
    @PutMapping("/role")
    @ResponseBody
    public AjaxResult updateRole(@RequestParam Long groupId, @RequestParam Long userId, @RequestParam String role) {
        return toAjax(imGroupMemberService.updateMemberRole(groupId, userId, role));
    }

    /**
     * 修改成员角色（带日志记录）
     */
    @RequiresPermissions("im:group:member:edit")
    @Log(title = "修改成员角色", businessType = BusinessType.UPDATE)
    @PostMapping("/roleWithLog")
    @ResponseBody
    public AjaxResult updateRoleWithLog(@RequestParam Long groupId, @RequestParam Long userId, @RequestParam String role, @RequestParam Long operatorId, @RequestParam String operatorName, @RequestParam String targetUserName) {
        return toAjax(imGroupMemberService.updateMemberRoleWithLog(groupId, userId, role, operatorId, operatorName, targetUserName));
    }

    /**
     * 设置成员禁言状态
     */
    @RequiresPermissions("im:group:member:edit")
    @Log(title = "设置成员禁言", businessType = BusinessType.UPDATE)
    @PutMapping("/mute")
    @ResponseBody
    public AjaxResult updateMuteStatus(ImGroupMember imGroupMember) {
        return toAjax(imGroupMemberService.updateImGroupMember(imGroupMember));
    }

    /**
     * 删除群组成员
     */
    @RequiresPermissions("im:group:member:remove")
    @Log(title = "群组成员管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imGroupMemberService.deleteImGroupMemberByIds(Convert.toLongArray(ids)));
    }

    /**
     * 移除群组成员
     */
    @RequiresPermissions("im:group:member:remove")
    @Log(title = "移除群组成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/group/{groupId}/user/{userId}")
    @ResponseBody
    public AjaxResult removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        return toAjax(imGroupMemberService.removeMember(groupId, userId));
    }

    /**
     * 移除群组成员（带日志记录）
     */
    @RequiresPermissions("im:group:member:remove")
    @Log(title = "移除群组成员", businessType = BusinessType.DELETE)
    @PostMapping("/removeWithLog")
    @ResponseBody
    public AjaxResult removeMemberWithLog(@RequestParam Long groupId, @RequestParam Long userId, @RequestParam Long operatorId, @RequestParam String operatorName, @RequestParam String targetUserName) {
        return toAjax(imGroupMemberService.removeMemberWithLog(groupId, userId, operatorId, operatorName, targetUserName));
    }

    /**
     * 导出群组成员列表
     */
    @RequiresPermissions("im:group:member:export")
    @Log(title = "群组成员管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImGroupMember imGroupMember) {
        List<ImGroupMember> list = imGroupMemberService.selectImGroupMemberList(imGroupMember);
        ExcelUtil<ImGroupMember> util = new ExcelUtil<>(ImGroupMember.class);
        util.exportExcel(response, list, "群组成员数据");
    }
}
