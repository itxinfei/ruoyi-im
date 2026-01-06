package com.ruoyi.web.controller.im;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.ImSystemStatistics;
import com.ruoyi.system.dto.statistics.ImStatisticsQueryRequest;
import com.ruoyi.system.service.IImStatisticsService;
import com.ruoyi.system.vo.statistics.ImStatisticsVO;
import com.ruoyi.system.vo.statistics.ImUserStatisticsVO;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM数据统计管理控制器
 * 
 * @author ruoyi
 */
@RestController("adminImStatisticsController")
@RequestMapping("/api/admin/im/statistics")
public class ImStatisticsAdminController extends BaseController {

    @Autowired
    private IImStatisticsService imStatisticsService;

    /**
     * 获取系统统计数据
     */
    @RequiresPermissions("im:statistics:system")
    @GetMapping("/system")
    public AjaxResult getSystemStatistics() {
        ImSystemStatistics statistics = imStatisticsService.getSystemStatistics();
        return AjaxResult.success(statistics);
    }

    /**
     * 获取统计列表
     */
    @RequiresPermissions("im:statistics:list")
    @PostMapping("/list")
    public AjaxResult getStatistics(@RequestBody ImStatisticsQueryRequest request) {
        List<ImStatisticsVO> statistics = imStatisticsService.getStatistics(request);
        return AjaxResult.success(statistics);
    }

    /**
     * 获取用户统计数据
     */
    @RequiresPermissions("im:statistics:user")
    @PostMapping("/user")
    public AjaxResult getUserStatistics(@RequestBody ImStatisticsQueryRequest request) {
        List<ImStatisticsVO> statistics = imStatisticsService.getStatistics(request);
        return AjaxResult.success(statistics);
    }

    /**
     * 获取指定用户的详细统计
     */
    @RequiresPermissions("im:statistics:user")
    @GetMapping("/user/{userId}")
    public AjaxResult getUserDetailStatistics(@PathVariable Long userId) {
        ImUserStatisticsVO statistics = imStatisticsService.getUserDetailStatistics(userId);
        return AjaxResult.success(statistics);
    }
}