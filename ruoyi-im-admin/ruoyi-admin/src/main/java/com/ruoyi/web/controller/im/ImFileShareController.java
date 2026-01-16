package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImFileShare;
import com.ruoyi.web.service.ImFileShareService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 文件分享管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/fileShare")
public class ImFileShareController extends BaseController {

    private String prefix = "im/fileShare";

    @Autowired
    private ImFileShareService imFileShareService;

    @RequiresPermissions("im:fileShare:view")
    @GetMapping()
    public String fileShare() {
        return prefix + "/fileShare";
    }

    /**
     * 查询文件分享列表
     */
    @RequiresPermissions("im:fileShare:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImFileShare imFileShare) {
        startPage();
        List<ImFileShare> list = imFileShareService.selectImFileShareList(imFileShare);
        return getDataTable(list);
    }

    /**
     * 获取文件分享统计数据
     */
    @RequiresPermissions("im:fileShare:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imFileShareService.getFileShareStatistics());
    }

    /**
     * 获取文件分享详细信息
     */
    @RequiresPermissions("im:fileShare:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imFileShareService.selectImFileShareById(id));
    }

    /**
     * 新增文件分享
     */
    @RequiresPermissions("im:fileShare:add")
    @Log(title = "文件分享管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImFileShare imFileShare) {
        return toAjax(imFileShareService.insertImFileShare(imFileShare));
    }

    /**
     * 修改文件分享（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:fileShare:edit")
    @Log(title = "文件分享管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@RequestBody ImFileShare imFileShare) {
        return toAjax(imFileShareService.updateImFileShare(imFileShare));
    }

    /**
     * 删除文件分享
     */
    @RequiresPermissions("im:fileShare:remove")
    @Log(title = "文件分享管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imFileShareService.deleteImFileShareByIds(ids));
    }

    /**
     * 清理过期分享
     */
    @RequiresPermissions("im:fileShare:remove")
    @Log(title = "文件分享管理", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    @ResponseBody
    public AjaxResult clean() {
        int count = imFileShareService.cleanExpiredShares();
        return AjaxResult.success("成功清理 " + count + " 条过期分享");
    }

    /**
     * 导出文件分享列表
     */
    @RequiresPermissions("im:fileShare:export")
    @Log(title = "文件分享管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImFileShare imFileShare) {
        List<ImFileShare> list = imFileShareService.selectImFileShareList(imFileShare);
        ExcelUtil<ImFileShare> util = new ExcelUtil<>(ImFileShare.class);
        util.exportExcel(response, list, "文件分享数据");
    }
}
