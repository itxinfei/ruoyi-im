package com.ruoyi.project.im.controller.im;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.project.im.domain.ImConversation;
import com.ruoyi.project.im.domain.ImConversationMember;
import com.ruoyi.project.im.service.ImConversationService;
import com.ruoyi.project.im.service.ImConversationMemberService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 会话管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Controller
@RequestMapping("/im/conversation")
public class ImConversationController extends BaseController {

    private String prefix = "im/conversation";

    @Autowired
    private ImConversationService imConversationService;

    @Autowired
    private ImConversationMemberService imConversationMemberService;

    @RequiresPermissions("im:conversation:view")
    @GetMapping()
    public String conversation() {
        return prefix + "/conversation";
    }

    /**
     * 新增会话页面
     */
    @RequiresPermissions("im:conversation:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改会话页面
     */
    @RequiresPermissions("im:conversation:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("conversation", imConversationService.selectImConversationById(id));
        return prefix + "/edit";
    }

    /**
     * 查询会话列表
     */
    @RequiresPermissions("im:conversation:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImConversation imConversation) {
        startPage();
        List<ImConversation> list = imConversationService.selectImConversationList(imConversation);
        return getDataTable(list);
    }

    /**
     * 会话统计
     */
    @RequiresPermissions("im:conversation:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imConversationService.getConversationStatistics());
    }

    /**
     * 获取会话详细信息
     */
    @RequiresPermissions("im:conversation:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imConversationService.selectImConversationById(id));
    }

    /**
     * 会话成员页面
     */
    @RequiresPermissions("im:conversation:query")
    @GetMapping("/{id}/members")
    public String members(@PathVariable("id") Long conversationId, org.springframework.ui.Model model) {
        model.addAttribute("conversationId", conversationId);
        return prefix + "/members";
    }

    /**
     * 获取会话成员列表（JSON接口）
     */
    @RequiresPermissions("im:conversation:query")
    @GetMapping("/{id}/members/data")
    @ResponseBody
    public AjaxResult getMembers(@PathVariable("id") Long conversationId) {
        List<ImConversationMember> members = imConversationMemberService.selectMembersByConversationId(conversationId);
        return AjaxResult.success(members);
    }

    /**
     * 统计会话成员数量
     */
    @RequiresPermissions("im:conversation:query")
    @GetMapping("/{id}/member-count")
    @ResponseBody
    public AjaxResult getMemberCount(@PathVariable("id") Long conversationId) {
        Integer count = imConversationMemberService.countMembersByConversationId(conversationId);
        return AjaxResult.success(count);
    }

    /**
     * 移除会话成员
     */
    @RequiresPermissions("im:conversation:edit")
    @Log(title = "移除会话成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{conversationId}/members/{userId}")
    @ResponseBody
    public AjaxResult removeMember(@PathVariable("conversationId") Long conversationId,
                                   @PathVariable("userId") Long userId) {
        return toAjax(imConversationMemberService.removeMember(conversationId, userId));
    }

    /**
     * 批量移除会话成员
     */
    @RequiresPermissions("im:conversation:edit")
    @Log(title = "批量移除成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{conversationId}/members/batch")
    @ResponseBody
    public AjaxResult batchRemoveMembers(@PathVariable("conversationId") Long conversationId,
                                        @RequestBody List<Long> userIds) {
        int successCount = 0;
        for (Long userId : userIds) {
            int result = imConversationMemberService.removeMember(conversationId, userId);
            if (result > 0) {
                successCount++;
            }
        }
        return AjaxResult.success("成功移除 " + successCount + " 位成员");
    }

    /**
     * 设置成员角色
     */
    @RequiresPermissions("im:conversation:edit")
    @Log(title = "设置成员角色", businessType = BusinessType.UPDATE)
    @PutMapping("/{conversationId}/members/{userId}/role")
    @ResponseBody
    public AjaxResult updateMemberRole(@PathVariable("conversationId") Long conversationId,
                                      @PathVariable("userId") Long userId,
                                      @RequestParam String role) {
        ImConversationMember member = new ImConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setRole(role);
        return toAjax(imConversationMemberService.updateImConversationMember(member));
    }

    /**
     * 添加会话成员页面
     */
    @RequiresPermissions("im:conversation:edit")
    @GetMapping("/{conversationId}/addMember")
    public String addMemberPage(@PathVariable("conversationId") Long conversationId,
                               org.springframework.ui.ModelMap mmap) {
        mmap.put("conversationId", conversationId);
        return prefix + "/addMember";
    }

    /**
     * 添加会话成员
     */
    @RequiresPermissions("im:conversation:edit")
    @Log(title = "添加会话成员", businessType = BusinessType.INSERT)
    @PostMapping("/{conversationId}/members")
    @ResponseBody
    public AjaxResult addMember(@PathVariable("conversationId") Long conversationId,
                               @RequestParam Long userId,
                               @RequestParam(defaultValue = "MEMBER") String role) {
        ImConversationMember member = new ImConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setRole(role);
        return toAjax(imConversationMemberService.insertImConversationMember(member));
    }

    /**
     * 新增会话
     */
    @RequiresPermissions("im:conversation:add")
    @Log(title = "会话管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImConversation imConversation) {
        return toAjax(imConversationService.insertImConversation(imConversation));
    }

    /**
     * 修改会话（API接口方式）
     */
    @RequiresPermissions("im:conversation:edit")
    @Log(title = "会话管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ResponseBody
    public AjaxResult editApi(@RequestBody ImConversation imConversation) {
        return toAjax(imConversationService.updateImConversation(imConversation));
    }
    
    /**
     * 修改会话（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:conversation:edit")
    @Log(title = "会话管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImConversation imConversation) {
        return toAjax(imConversationService.updateImConversation(imConversation));
    }

    /**
     * 删除会话
     */
    @RequiresPermissions("im:conversation:remove")
    @Log(title = "会话管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imConversationService.deleteImConversationByIds(Convert.toLongArray(ids)));
    }

    /**
     * 导出会话列表
     */
    @RequiresPermissions("im:conversation:export")
    @Log(title = "会话管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImConversation imConversation) {
        List<ImConversation> list = imConversationService.selectImConversationList(imConversation);
        ExcelUtil<ImConversation> util = new ExcelUtil<>(ImConversation.class);
        util.exportExcel(response, list, "会话数据");
    }
}
