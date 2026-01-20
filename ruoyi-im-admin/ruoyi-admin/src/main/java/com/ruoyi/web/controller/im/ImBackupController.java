package com.ruoyi.web.controller.im;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.web.domain.ImBackup;
import com.ruoyi.web.service.ImBackupService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * IM数据备份管理控制器
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/backup")
public class ImBackupController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ImBackupController.class);

    private String prefix = "im/backup";

    @Autowired
    private ImBackupService backupService;

    /**
     * 备份管理页面
     */
    @RequiresPermissions("im:backup:view")
    @GetMapping()
    public String backup() {
        return prefix + "/backup";
    }

    /**
     * 查询备份记录列表
     */
    @RequiresPermissions("im:backup:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ImBackup backup) {
        List<ImBackup> list = backupService.selectBackupList(backup);
        return getDataTable(list);
    }

    /**
     * 获取备份统计信息
     */
    @RequiresPermissions("im:backup:query")
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        return AjaxResult.success(backupService.getBackupStatistics());
    }

    /**
     * 手动备份
     */
    @RequiresPermissions("im:backup:add")
    @Log(title = "数据备份", businessType = BusinessType.INSERT)
    @PostMapping("/manual")
    @ResponseBody
    public AjaxResult manualBackup(@RequestParam(required = false) String description) {
        String operator = ShiroUtils.getSysUser().getUserName();
        ImBackup backup = backupService.manualBackup(description, operator);
        if ("SUCCESS".equals(backup.getStatus())) {
            return AjaxResult.success("备份成功", backup);
        } else {
            return AjaxResult.error("备份失败: " + backup.getErrorMessage());
        }
    }

    /**
     * 删除备份记录
     */
    @RequiresPermissions("im:backup:remove")
    @Log(title = "删除备份", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable String ids) {
        return toAjax(backupService.deleteBackupByIds(Convert.toLongArray(ids)));
    }

    /**
     * 下载备份文件
     */
    @RequiresPermissions("im:backup:download")
    @GetMapping("/download/{id}")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        String filePath = backupService.getBackupFilePath(id);
        if (filePath != null) {
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    response.setContentType("application/octet-stream");
                    response.setHeader("Content-Disposition",
                        "attachment; filename=" + file.getName());

                    Files.copy(file.toPath(), response.getOutputStream());
                    response.getOutputStream().flush();
                } else {
                    logger.warn("备份文件不存在: {}", filePath);
                }
            } catch (IOException e) {
                logger.error("下载备份文件失败: {}", filePath, e);
            }
        }
    }

    /**
     * 恢复数据
     */
    @RequiresPermissions("im:backup:restore")
    @Log(title = "恢复数据", businessType = BusinessType.OTHER)
    @PostMapping("/restore/{id}")
    @ResponseBody
    public AjaxResult restore(@PathVariable("id") Long id) {
        boolean success = backupService.restoreBackup(id);
        if (success) {
            return AjaxResult.success("数据恢复成功");
        } else {
            return AjaxResult.error("数据恢复失败");
        }
    }

    /**
     * 获取备份详细信息
     */
    @RequiresPermissions("im:backup:query")
    @GetMapping("/info/{id}")
    @ResponseBody
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(backupService.selectBackupById(id));
    }
}
