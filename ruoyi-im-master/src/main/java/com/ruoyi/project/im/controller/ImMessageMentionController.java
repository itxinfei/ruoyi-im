package com.ruoyi.project.im.controller.im;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.project.im.domain.ImMessageMention;
import com.ruoyi.project.im.service.ImMessageMentionService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息@记录管理控制器
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Controller
@RequestMapping("/im/message/mention")
public class ImMessageMentionController extends BaseController {

    private String prefix = "im/message/mention";

    @Autowired
    private ImMessageMentionService mentionService;

    /**
     * 消息@记录管理页面
     */
    @RequiresPermissions("im:message:mention:list")
    @GetMapping()
    public String mention() {
        return prefix + "/mention";
    }

    /**
     * 查询消息@记录列表
     */
    @RequiresPermissions("im:message:mention:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImMessageMention imMessageMention) {
        startPage();
        List<ImMessageMention> list = mentionService.selectImMessageMentionList(imMessageMention);
        return getDataTable(list);
    }

    /**
     * 获取@统计数据
     */
    @RequiresPermissions("im:message:mention:list")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        // 调用 Service 获取统计数据
        Map<String, Object> dbStats = mentionService.getMentionStatistics();

        // 转换字段名为驼峰格式（前端期望）
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", dbStats.get("totalCount"));
        stats.put("readCount", dbStats.get("readCount"));
        stats.put("unreadCount", dbStats.get("unreadCount"));
        stats.put("mentionAllCount", dbStats.get("mentionAllCount"));
        return AjaxResult.success(stats);
    }

    /**
     * 获取消息@记录详细信息
     */
    @RequiresPermissions("im:message:mention:query")
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(mentionService.selectImMessageMentionById(id));
    }

    /**
     * 根据消息ID查询@记录列表
     */
    @RequiresPermissions("im:message:mention:list")
    @GetMapping("/message/{messageId}")
    @ResponseBody
    public AjaxResult getByMessageId(@PathVariable("messageId") Long messageId) {
        List<ImMessageMention> list = mentionService.selectImMessageMentionByMessageId(messageId);
        return AjaxResult.success(list);
    }

    /**
     * 导出消息@记录列表
     */
    @RequiresPermissions("im:message:mention:export")
    @Log(title = "消息@记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ImMessageMention imMessageMention) {
        List<ImMessageMention> list = mentionService.selectImMessageMentionList(imMessageMention);
        ExcelUtil<ImMessageMention> util = new ExcelUtil<>(ImMessageMention.class);
        util.exportExcel(response, list, "消息@记录数据");
    }
}
