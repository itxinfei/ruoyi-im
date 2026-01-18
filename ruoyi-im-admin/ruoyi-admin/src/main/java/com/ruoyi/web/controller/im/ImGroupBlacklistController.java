package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImGroupBlacklist;
import com.ruoyi.web.service.ImGroupBlacklistService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 群组黑名单管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Controller
@RequestMapping("/im/group/blacklist")
public class ImGroupBlacklistController extends BaseController {

    private String prefix = "im/group/blacklist";

    @Autowired
    private ImGroupBlacklistService groupBlacklistService;

    /**
     * 群组黑名单管理页面
     */
    @RequiresPermissions("im:group:blacklist:view")
    @GetMapping()
    public String blacklist() {
        return prefix + "/blacklist";
    }

    /**
     * 查询群组黑名单列表
     */
    @RequiresPermissions("im:group:blacklist:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImGroupBlacklist imGroupBlacklist) {
        startPage();
        List<ImGroupBlacklist> list = groupBlacklistService.selectImGroupBlacklistList(imGroupBlacklist);
        return getDataTable(list);
    }

    /**
     * 根据群组ID查询黑名单列表
     */
    @RequiresPermissions("im:group:blacklist:list")
    @GetMapping("/group/{groupId}")
    @ResponseBody
    public AjaxResult listByGroupId(@PathVariable("groupId") Long groupId) {
        List<ImGroupBlacklist> list = groupBlacklistService.selectImGroupBlacklistByGroupId(groupId);
        return AjaxResult.success(list);
    }

    /**
     * 导出群组黑名单列表
     */
    @RequiresPermissions("im:group:blacklist:export")
    @Log(title = "群组黑名单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImGroupBlacklist imGroupBlacklist) {
        List<ImGroupBlacklist> list = groupBlacklistService.selectImGroupBlacklistList(imGroupBlacklist);
        ExcelUtil<ImGroupBlacklist> util = new ExcelUtil<>(ImGroupBlacklist.class);
        util.exportExcel(response, list, "群组黑名单数据");
    }

    /**
     * 获取群组黑名单详细信息
     */
    @RequiresPermissions("im:group:blacklist:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(groupBlacklistService.selectImGroupBlacklistById(id));
    }

    /**
     * 新增群组黑名单页面
     */
    @RequiresPermissions("im:group:blacklist:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存群组黑名单
     */
    @RequiresPermissions("im:group:blacklist:add")
    @Log(title = "群组黑名单", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ImGroupBlacklist imGroupBlacklist) {
        return toAjax(groupBlacklistService.insertImGroupBlacklist(imGroupBlacklist));
    }

    /**
     * 修改群组黑名单页面
     */
    @RequiresPermissions("im:group:blacklist:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        ImGroupBlacklist blacklist = groupBlacklistService.selectImGroupBlacklistById(id);
        mmap.put("blacklist", blacklist);
        return prefix + "/edit";
    }

    /**
     * 修改保存群组黑名单
     */
    @RequiresPermissions("im:group:blacklist:edit")
    @Log(title = "群组黑名单", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ImGroupBlacklist imGroupBlacklist) {
        return toAjax(groupBlacklistService.updateImGroupBlacklist(imGroupBlacklist));
    }

    /**
     * 删除群组黑名单
     */
    @RequiresPermissions("im:group:blacklist:remove")
    @Log(title = "群组黑名单", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(groupBlacklistService.deleteImGroupBlacklistByIds(Convert.toLongArray(ids)));
    }

    /**
     * 解除禁言/拉黑
     */
    @RequiresPermissions("im:group:blacklist:edit")
    @Log(title = "解除禁言/拉黑", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/release")
    @ResponseBody
    public AjaxResult release(@PathVariable("id") Long id) {
        return toAjax(groupBlacklistService.releaseImGroupBlacklist(id));
    }

    /**
     * 检查用户是否在群组黑名单中
     */
    @RequiresPermissions("im:group:blacklist:query")
    @GetMapping("/check")
    @ResponseBody
    public AjaxResult checkUserInBlacklist(@RequestParam("groupId") Long groupId,
                                            @RequestParam("userId") Long userId,
                                            @RequestParam(value = "type", required = false) String type) {
        boolean inBlacklist = groupBlacklistService.checkUserInBlacklist(groupId, userId, type);
        return AjaxResult.success(inBlacklist);
    }

    /**
     * 获取黑名单统计数据
     */
    @RequiresPermissions("im:group:blacklist:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(groupBlacklistService.getBlacklistStatistics());
    }
}
