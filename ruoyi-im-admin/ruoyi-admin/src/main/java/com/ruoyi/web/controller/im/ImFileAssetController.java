package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.web.service.ImFileAssetService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * IM文件资源管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@RestController
@RequestMapping("api/admin/im/file")
public class ImFileAssetController extends BaseController {

    @Autowired
    private ImFileAssetService imFileAssetService;

    /**
     * 查询文件资源列表
     */
    @RequiresPermissions("im:file:list")
    @GetMapping("/list")
    public AjaxResult list(ImFileAsset imFileAsset) {
        startPage();
        List<ImFileAsset> list = imFileAssetService.selectImFileAssetList(imFileAsset);
        return AjaxResult.success(list);
    }

    /**
     * 导出文件资源列表
     */
    @RequiresPermissions("im:file:export")
    @Log(title = "文件资源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImFileAsset imFileAsset) {
        List<ImFileAsset> list = imFileAssetService.selectImFileAssetList(imFileAsset);
        // 导出逻辑
    }

    /**
     * 获取文件资源详细信息
     */
    @RequiresPermissions("im:file:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imFileAssetService.selectImFileAssetById(id));
    }

    /**
     * 新增文件资源
     */
    @RequiresPermissions("im:file:add")
    @Log(title = "文件资源", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImFileAsset imFileAsset) {
        return toAjax(imFileAssetService.insertImFileAsset(imFileAsset));
    }

    /**
     * 修改文件资源
     */
    @RequiresPermissions("im:file:edit")
    @Log(title = "文件资源", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImFileAsset imFileAsset) {
        return toAjax(imFileAssetService.updateImFileAsset(imFileAsset));
    }

    /**
     * 删除文件资源
     */
    @RequiresPermissions("im:file:remove")
    @Log(title = "文件资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imFileAssetService.deleteImFileAssetByIds(ids));
    }

    /**
     * 文件统计
     */
    @RequiresPermissions("im:file:list")
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        return AjaxResult.success(imFileAssetService.getFileStatistics());
    }

    /**
     * 清理无效文件
     */
    @RequiresPermissions("im:file:remove")
    @Log(title = "清理无效文件", businessType = BusinessType.DELETE)
    @DeleteMapping("/clean")
    public AjaxResult cleanInvalidFiles() {
        int count = imFileAssetService.cleanInvalidFiles();
        return AjaxResult.success("清理完成，共清理" + count + "个文件");
    }
}
