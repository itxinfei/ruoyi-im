package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImGroupAnnouncement;
import com.ruoyi.web.service.ImGroupAnnouncementService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 群公告管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/group/announcement")
public class ImGroupAnnouncementController extends BaseController {

    private String prefix = "im/announcement";

    @Autowired
    private ImGroupAnnouncementService imGroupAnnouncementService;

    @RequiresPermissions("im:announcement:view")
    @GetMapping()
    public String announcement() {
        return prefix + "/announcement";
    }

    /**
     * 获取群公告统计数据
     */
    @RequiresPermissions("im:announcement:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imGroupAnnouncementService.getAnnouncementStatistics());
    }

    /**
     * 新增群公告页面
     */
    @RequiresPermissions("im:announcement:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改群公告页面
     */
    @RequiresPermissions("im:announcement:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, org.springframework.ui.ModelMap mmap) {
        mmap.put("announcement", imGroupAnnouncementService.selectImGroupAnnouncementById(id));
        return prefix + "/edit";
    }

    /**
     * 查询群公告列表
     */
    @RequiresPermissions("im:announcement:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImGroupAnnouncement imGroupAnnouncement) {
        startPage();
        List<ImGroupAnnouncement> list = imGroupAnnouncementService.selectImGroupAnnouncementList(imGroupAnnouncement);
        return getDataTable(list);
    }

    /**
     * 获取群公告详细信息
     */
    @RequiresPermissions("im:announcement:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imGroupAnnouncementService.selectImGroupAnnouncementById(id));
    }

    /**
     * 根据群组ID查询公告列表
     */
    @RequiresPermissions("im:announcement:query")
    @GetMapping("/group/{groupId}")
    @ResponseBody
    public AjaxResult getAnnouncementsByGroupId(@PathVariable("groupId") Long groupId) {
        return AjaxResult.success(imGroupAnnouncementService.selectAnnouncementsByGroupId(groupId));
    }

    /**
     * 查询群组的置顶公告
     */
    @RequiresPermissions("im:announcement:query")
    @GetMapping("/group/{groupId}/pinned")
    @ResponseBody
    public AjaxResult getPinnedAnnouncements(@PathVariable("groupId") Long groupId) {
        return AjaxResult.success(imGroupAnnouncementService.selectPinnedAnnouncementsByGroupId(groupId));
    }

    /**
     * 统计群组公告数量
     */
    @RequiresPermissions("im:announcement:query")
    @GetMapping("/group/{groupId}/count")
    @ResponseBody
    public AjaxResult getAnnouncementCount(@PathVariable("groupId") Long groupId) {
        return AjaxResult.success(imGroupAnnouncementService.countAnnouncementsByGroupId(groupId));
    }

    /**
     * 新增群公告
     */
    @RequiresPermissions("im:announcement:add")
    @Log(title = "群公告管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImGroupAnnouncement imGroupAnnouncement) {
        return toAjax(imGroupAnnouncementService.insertImGroupAnnouncement(imGroupAnnouncement));
    }

    /**
     * 修改群公告
     */
    @RequiresPermissions("im:announcement:edit")
    @Log(title = "群公告管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @ResponseBody
    public AjaxResult edit(@RequestBody ImGroupAnnouncement imGroupAnnouncement) {
        return toAjax(imGroupAnnouncementService.updateImGroupAnnouncement(imGroupAnnouncement));
    }

    /**
     * 修改置顶状态
     */
    @RequiresPermissions("im:announcement:edit")
    @Log(title = "修改公告置顶", businessType = BusinessType.UPDATE)
    @PutMapping("/pin")
    @ResponseBody
    public AjaxResult updatePinnedStatus(@RequestParam Long id, @RequestParam Integer isPinned) {
        return toAjax(imGroupAnnouncementService.updatePinnedStatus(id, isPinned));
    }

    /**
     * 删除群公告
     */
    @RequiresPermissions("im:announcement:remove")
    @Log(title = "群公告管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imGroupAnnouncementService.deleteImGroupAnnouncementByIds(ids));
    }

    /**
     * 根据群组ID删除所有公告
     */
    @RequiresPermissions("im:announcement:remove")
    @Log(title = "删除群组所有公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/group/{groupId}")
    @ResponseBody
    public AjaxResult removeByGroupId(@PathVariable Long groupId) {
        return toAjax(imGroupAnnouncementService.deleteAnnouncementsByGroupId(groupId));
    }

    /**
     * 导出群公告列表
     */
    @RequiresPermissions("im:announcement:export")
    @Log(title = "群公告管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImGroupAnnouncement imGroupAnnouncement) {
        List<ImGroupAnnouncement> list = imGroupAnnouncementService.selectImGroupAnnouncementList(imGroupAnnouncement);
        ExcelUtil<ImGroupAnnouncement> util = new ExcelUtil<>(ImGroupAnnouncement.class);
        util.exportExcel(response, list, "群公告数据");
    }
}
