package com.ruoyi.web.controller.im;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.ImFileAsset;
import com.ruoyi.web.service.IImFileAssetService;

/**
 * 文件资源Controller
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/api/admin/im/file")
public class ImFileAssetController extends BaseController
{
    @Autowired
    private IImFileAssetService imFileAssetService;

    /**
     * 查询文件资源列表
     */
    @RequiresPermissions("im:file:list")
    @GetMapping("/list")
    public TableDataInfo list(ImFileAsset imFileAsset)
    {
        startPage();
        List<ImFileAsset> list = imFileAssetService.selectImFileAssetList(imFileAsset);
        return getDataTable(list);
    }

    /**
     * 导出文件资源列表
     */
    @RequiresPermissions("im:file:export")
    @Log(title = "文件资源", businessType = com.ruoyi.common.enums.BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImFileAsset imFileAsset)
    {
        List<ImFileAsset> list = imFileAssetService.selectImFileAssetList(imFileAsset);
        ExcelUtil<ImFileAsset> util = new ExcelUtil<ImFileAsset>(ImFileAsset.class);
        util.exportExcel(response, list, "文件资源数据");
    }

    /**
     * 获取文件资源详细信息
     */
    @RequiresPermissions("im:file:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(imFileAssetService.selectImFileAssetById(id));
    }

    /**
     * 新增文件资源
     */
    @RequiresPermissions("im:file:add")
    @Log(title = "文件资源", businessType = com.ruoyi.common.enums.BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ImFileAsset imFileAsset)
    {
        return toAjax(imFileAssetService.insertImFileAsset(imFileAsset));
    }

    /**
     * 修改文件资源
     */
    @RequiresPermissions("im:file:edit")
    @Log(title = "文件资源", businessType = com.ruoyi.common.enums.BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ImFileAsset imFileAsset)
    {
        return toAjax(imFileAssetService.updateImFileAsset(imFileAsset));
    }

    /**
     * 删除文件资源
     */
    @RequiresPermissions("im:file:remove")
    @Log(title = "文件资源", businessType = com.ruoyi.common.enums.BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(imFileAssetService.deleteImFileAssetByIds(ids));
    }
}