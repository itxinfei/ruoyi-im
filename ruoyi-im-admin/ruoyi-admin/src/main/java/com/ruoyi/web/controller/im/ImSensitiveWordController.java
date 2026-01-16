package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImSensitiveWord;
import com.ruoyi.web.service.ImSensitiveWordService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 敏感词管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/sensitiveWord")
public class ImSensitiveWordController extends BaseController {

    private String prefix = "im/sensitiveWord";

    @Autowired
    private ImSensitiveWordService imSensitiveWordService;

    @RequiresPermissions("im:sensitiveWord:view")
    @GetMapping()
    public String sensitiveWord() {
        return prefix + "/sensitiveWord";
    }

    /**
     * 查询敏感词列表
     */
    @RequiresPermissions("im:sensitiveWord:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImSensitiveWord imSensitiveWord) {
        startPage();
        List<ImSensitiveWord> list = imSensitiveWordService.selectImSensitiveWordList(imSensitiveWord);
        return getDataTable(list);
    }

    /**
     * 获取敏感词统计数据
     */
    @RequiresPermissions("im:sensitiveWord:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(imSensitiveWordService.getSensitiveWordStatistics());
    }

    /**
     * 获取敏感词详细信息
     */
    @RequiresPermissions("im:sensitiveWord:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imSensitiveWordService.selectImSensitiveWordById(id));
    }

    /**
     * 新增敏感词
     */
    @RequiresPermissions("im:sensitiveWord:add")
    @Log(title = "敏感词管理", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImSensitiveWord imSensitiveWord) {
        return toAjax(imSensitiveWordService.insertImSensitiveWord(imSensitiveWord));
    }

    /**
     * 修改敏感词（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:sensitiveWord:edit")
    @Log(title = "敏感词管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@RequestBody ImSensitiveWord imSensitiveWord) {
        return toAjax(imSensitiveWordService.updateImSensitiveWord(imSensitiveWord));
    }

    /**
     * 切换敏感词启用状态
     */
    @RequiresPermissions("im:sensitiveWord:edit")
    @Log(title = "敏感词管理", businessType = BusinessType.UPDATE)
    @PutMapping("/toggle/{id}")
    @ResponseBody
    public AjaxResult toggleEnabled(@PathVariable("id") Long id) {
        return toAjax(imSensitiveWordService.toggleEnabled(id));
    }

    /**
     * 批量导入敏感词
     */
    @RequiresPermissions("im:sensitiveWord:import")
    @Log(title = "敏感词管理", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    @ResponseBody
    public AjaxResult importWords(@RequestBody List<String> words) {
        return toAjax(imSensitiveWordService.batchImportWords(words));
    }

    /**
     * 删除敏感词
     */
    @RequiresPermissions("im:sensitiveWord:remove")
    @Log(title = "敏感词管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(imSensitiveWordService.deleteImSensitiveWordByIds(Convert.toLongArray(ids)));
    }

    /**
     * 导出敏感词列表
     */
    @RequiresPermissions("im:sensitiveWord:export")
    @Log(title = "敏感词管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public void export(HttpServletResponse response, ImSensitiveWord imSensitiveWord) {
        List<ImSensitiveWord> list = imSensitiveWordService.selectImSensitiveWordList(imSensitiveWord);
        ExcelUtil<ImSensitiveWord> util = new ExcelUtil<>(ImSensitiveWord.class);
        util.exportExcel(response, list, "敏感词数据");
    }
}
