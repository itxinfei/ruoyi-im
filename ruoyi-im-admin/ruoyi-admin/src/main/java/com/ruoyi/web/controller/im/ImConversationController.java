package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImConversation;
import com.ruoyi.web.domain.ImConversationMember;
import com.ruoyi.web.service.ImConversationService;
import com.ruoyi.web.service.ImConversationMemberService;
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
     * 获取会话详细信息
     */
    @RequiresPermissions("im:conversation:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imConversationService.selectImConversationById(id));
    }

    /**
     * 获取会话成员列表
     */
    @RequiresPermissions("im:conversation:query")
    @GetMapping("/{id}/members")
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
     * 新增会话
     */
    @RequiresPermissions("im:conversation:add")
    @Log(title = "会话管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImConversation imConversation) {
        return toAjax(imConversationService.insertImConversation(imConversation));
    }

    /**
     * 修改会话
     */
    @RequiresPermissions("im:conversation:edit")
    @Log(title = "会话管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ResponseBody
    public AjaxResult edit(@RequestBody ImConversation imConversation) {
        return toAjax(imConversationService.updateImConversation(imConversation));
    }

    /**
     * 删除会话
     */
    @RequiresPermissions("im:conversation:remove")
    @Log(title = "会话管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imConversationService.deleteImConversationByIds(ids));
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

    /**
     * 会话统计
     */
    @RequiresPermissions("im:conversation:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imConversationService.getConversationStatistics());
    }
}
