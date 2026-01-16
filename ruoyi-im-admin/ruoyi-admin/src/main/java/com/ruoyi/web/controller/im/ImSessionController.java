package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.ImConversation;
import com.ruoyi.web.service.ImConversationService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * IM会话Controller
 *
 * @author ruoyi
 * @date 2024-01-01
 */
@Controller
@RequestMapping("/im/session")
public class ImSessionController extends BaseController {

    private String prefix = "im/session";

    @Autowired
    private ImConversationService imConversationService;

    /**
     * Session管理页面
     */
    @RequiresPermissions("im:session:list")
    @GetMapping()
    public String session() {
        return prefix + "/session";
    }

    /**
     * 查询IM会话列表
     */
    @RequiresPermissions("im:session:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImConversation imConversation) {
        startPage();
        List<ImConversation> list = imConversationService.selectImConversationList(imConversation);
        return getDataTable(list);
    }

    /**
     * 导出IM会话列表
     */
    @RequiresPermissions("im:session:export")
    @Log(title = "IM会话", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImConversation imConversation) {
        List<ImConversation> list = imConversationService.selectImConversationList(imConversation);
        ExcelUtil<ImConversation> util = new ExcelUtil<ImConversation>(ImConversation.class);
        util.exportExcel(response, list, "IM会话数据");
    }

    /**
     * 获取IM会话详细信息
     */
    @RequiresPermissions("im:session:query")
    @GetMapping(value = "/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(imConversationService.selectImConversationById(id));
    }

    /**
     * 新增IM会话
     */
    @RequiresPermissions("im:session:add")
    @Log(title = "IM会话", businessType = BusinessType.INSERT)
    @PostMapping
    @ResponseBody
    public AjaxResult add(@RequestBody ImConversation imConversation) {
        return toAjax(imConversationService.insertImConversation(imConversation));
    }

    /**
     * 修改IM会话（表单提交方式，用于编辑页面）
     */
    @RequiresPermissions("im:session:edit")
    @Log(title = "IM会话", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(@RequestBody ImConversation imConversation) {
        return toAjax(imConversationService.updateImConversation(imConversation));
    }

    /**
     * 删除IM会话
     */
    @RequiresPermissions("im:session:remove")
    @Log(title = "IM会话", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(imConversationService.deleteImConversationByIds(ids));
    }

    /**
     * 删除会话（POST方式，兼容前端框架）
     */
    @RequiresPermissions("im:session:remove")
    @Log(title = "IM会话", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult removePost(Long[] ids) {
        return toAjax(imConversationService.deleteImConversationByIds(ids));
    }

    /**
     * 获取会话统计信息
     */
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        Map<String, Object> data = imConversationService.getConversationStatistics();
        return AjaxResult.success(data);
    }
}
