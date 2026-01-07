package com.ruoyi.web.controller.im;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.ImStatisticsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * IM统计管理控制器（管理后台）
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@RestController
@RequestMapping("api/admin/im/statistics")
public class ImStatisticsAdminController extends BaseController {

    @Autowired
    private ImStatisticsService statisticsService;

    /**
     * 获取系统统计数据总览
     */
    @RequiresPermissions("im:statistics:system")
    @GetMapping("/system")
    public AjaxResult getSystemStatistics() {
        Map<String, Object> statistics = statisticsService.getSystemStatistics();
        return AjaxResult.success(statistics);
    }

    /**
     * 获取用户统计数据
     */
    @RequiresPermissions("im:statistics:user")
    @PostMapping("/user")
    public AjaxResult getUserStatistics(@RequestParam Long userId,
                                     @RequestParam(required = false) String startTime,
                                     @RequestParam(required = false) String endTime) {
        Map<String, Object> statistics = statisticsService.getUserStatistics(userId, startTime, endTime);
        return AjaxResult.success(statistics);
    }

    /**
     * 获取群组统计数据
     */
    @RequiresPermissions("im:statistics:group")
    @PostMapping("/group")
    public AjaxResult getGroupStatistics(@RequestParam Long groupId,
                                     @RequestParam(required = false) String startTime,
                                     @RequestParam(required = false) String endTime) {
        Map<String, Object> statistics = statisticsService.getGroupStatistics(groupId, startTime, endTime);
        return AjaxResult.success(statistics);
    }

    /**
     * 获取消息统计数据
     */
    @RequiresPermissions("im:statistics:message")
    @PostMapping("/message")
    public AjaxResult getMessageStatistics(@RequestParam(required = false) Long conversationId,
                                       @RequestParam(required = false) String startTime,
                                       @RequestParam(required = false) String endTime) {
        Map<String, Object> statistics = statisticsService.getMessageStatistics(conversationId, startTime, endTime);
        return AjaxResult.success(statistics);
    }

    /**
     * 获取文件统计数据
     */
    @RequiresPermissions("im:statistics:file")
    @GetMapping("/file")
    public AjaxResult getFileStatistics() {
        Map<String, Object> statistics = statisticsService.getFileStatistics();
        return AjaxResult.success(statistics);
    }

    /**
     * 获取活跃度排行
     */
    @RequiresPermissions("im:statistics:active")
    @GetMapping("/active-ranking")
    public AjaxResult getActiveRanking(@RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> ranking = statisticsService.getActiveRanking(days);
        return AjaxResult.success(ranking);
    }

    /**
     * 获取实时在线统计
     */
    @RequiresPermissions("im:statistics:online")
    @GetMapping("/online")
    public AjaxResult getOnlineStatistics() {
        Map<String, Object> statistics = statisticsService.getOnlineStatistics();
        return AjaxResult.success(statistics);
    }
}
