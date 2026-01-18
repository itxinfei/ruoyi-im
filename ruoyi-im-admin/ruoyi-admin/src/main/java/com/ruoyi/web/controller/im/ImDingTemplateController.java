package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImDingTemplate;
import com.ruoyi.web.service.ImDingTemplateService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * DING模板管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Controller
@RequestMapping("/im/dingTemplate")
public class ImDingTemplateController extends BaseController {

    private String prefix = "im/dingTemplate";

    @Autowired
    private ImDingTemplateService dingTemplateService;

    /**
     * DING模板管理页面
     */
    @RequiresPermissions("im:dingTemplate:view")
    @GetMapping()
    public String dingTemplate() {
        return prefix + "/dingTemplate";
    }

    /**
     * 新增DING模板页面
     */
    @RequiresPermissions("im:dingTemplate:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 修改DING模板页面
     */
    @RequiresPermissions("im:dingTemplate:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        ImDingTemplate template = dingTemplateService.selectImDingTemplateById(id);
        mmap.put("template", template);
        return prefix + "/edit";
    }

    /**
     * 查询DING模板列表
     */
    @RequiresPermissions("im:dingTemplate:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImDingTemplate imDingTemplate) {
        startPage();
        List<ImDingTemplate> list = dingTemplateService.selectImDingTemplateList(imDingTemplate);
        return getDataTable(list);
    }

    /**
     * 导出DING模板列表
     */
    @RequiresPermissions("im:dingTemplate:export")
    @Log(title = "DING模板管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImDingTemplate imDingTemplate) {
        List<ImDingTemplate> list = dingTemplateService.selectImDingTemplateList(imDingTemplate);
        ExcelUtil<ImDingTemplate> util = new ExcelUtil<>(ImDingTemplate.class);
        util.exportExcel(response, list, "DING模板数据");
    }

    /**
     * 获取DING模板详细信息
     */
    @RequiresPermissions("im:dingTemplate:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(dingTemplateService.selectImDingTemplateById(id));
    }

    /**
     * 新增DING模板
     */
    @RequiresPermissions("im:dingTemplate:add")
    @Log(title = "DING模板管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(@Validated ImDingTemplate imDingTemplate) {
        if ("1".equals(dingTemplateService.checkTemplateNameUnique(imDingTemplate))) {
            return AjaxResult.error("新增模板'" + imDingTemplate.getName() + "'失败，模板名称已存在");
        }
        return toAjax(dingTemplateService.insertImDingTemplate(imDingTemplate));
    }

    /**
     * 修改DING模板
     */
    @RequiresPermissions("im:dingTemplate:edit")
    @Log(title = "DING模板管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@Validated ImDingTemplate imDingTemplate) {
        if ("1".equals(dingTemplateService.checkTemplateNameUnique(imDingTemplate))) {
            return AjaxResult.error("修改模板'" + imDingTemplate.getName() + "'失败，模板名称已存在");
        }
        return toAjax(dingTemplateService.updateImDingTemplate(imDingTemplate));
    }

    /**
     * 删除DING模板
     */
    @RequiresPermissions("im:dingTemplate:remove")
    @Log(title = "DING模板管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        try {
            return toAjax(dingTemplateService.deleteImDingTemplateByIds(Convert.toLongArray(ids)));
        } catch (Exception e) {
            return AjaxResult.error("删除模板失败，该模板可能正在使用中");
        }
    }

    /**
     * 校验模板名称
     */
    @PostMapping("/checkTemplateNameUnique")
    @ResponseBody
    public String checkTemplateNameUnique(ImDingTemplate imDingTemplate) {
        return dingTemplateService.checkTemplateNameUnique(imDingTemplate);
    }
}
