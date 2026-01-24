package com.ruoyi.project.im.controller.im;

import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.web.mapper.ImGroupMapper;
import com.ruoyi.web.mapper.ImMessageMapper;
import com.ruoyi.web.mapper.ImUserMapper;
import com.ruoyi.project.im.service.ImDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM系统仪表板控制器
 * 提供首页统计数据接口（含数据可视化功能）
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/dashboard")
public class ImDashboardController extends BaseController {

    private String prefix = "im/dashboard";

    /**
     * IM数据仪表盘页面
     */
    @GetMapping()
    public String dashboard() {
        return prefix + "/dashboard";
    }

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private ImGroupMapper imGroupMapper;

    @Autowired
    private ImMessageMapper imMessageMapper;

    @Autowired
    private ImDashboardService dashboardService;

    // ==================== 原有接口（保持兼容）====================

    /**
     * 获取首页统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @ResponseBody
    public AjaxResult getStatistics() {
        Map<String, Object> data = new HashMap<>();

        data.put("totalUsers", imUserMapper.countTotalUsers());
        data.put("onlineUsers", imUserMapper.countOnlineUsers());
        data.put("activeUsers", imUserMapper.countActiveUsers());
        data.put("todayRegister", imUserMapper.countTodayRegister());
        data.put("disabledUsers", imUserMapper.countDisabledUsers());

        data.put("totalGroups", imGroupMapper.countTotalGroups());
        data.put("activeGroups", imGroupMapper.countActiveGroups());
        data.put("todayCreatedGroups", imGroupMapper.countTodayCreatedGroups());
        data.put("totalGroupMembers", imGroupMapper.countTotalGroupMembers());
        data.put("mutedGroups", imGroupMapper.countMutedGroups());
        data.put("largeGroups", imGroupMapper.countLargeGroups());

        Map<String, Object> messageStats = imMessageMapper.getMessageStatistics();
        data.put("totalMessages", messageStats.get("totalMessages"));
        data.put("todayMessages", messageStats.get("todayMessages"));
        data.put("sensitiveMessages", imMessageMapper.countSensitiveMessages());

        return AjaxResult.success(data);
    }

    /**
     * 获取用户统计
     *
     * @return 用户统计数据
     */
    @GetMapping("/user-statistics")
    @ResponseBody
    public AjaxResult getUserStatistics() {
        Map<String, Object> data = new HashMap<>();

        data.put("totalUsers", imUserMapper.countTotalUsers());
        data.put("onlineUsers", imUserMapper.countOnlineUsers());
        data.put("activeUsers", imUserMapper.countActiveUsers());
        data.put("todayRegister", imUserMapper.countTodayRegister());
        data.put("disabledUsers", imUserMapper.countDisabledUsers());

        return AjaxResult.success(data);
    }

    /**
     * 获取群组统计
     *
     * @return 群组统计数据
     */
    @GetMapping("/group-statistics")
    @ResponseBody
    public AjaxResult getGroupStatistics() {
        Map<String, Object> data = new HashMap<>();

        data.put("totalGroups", imGroupMapper.countTotalGroups());
        data.put("activeGroups", imGroupMapper.countActiveGroups());
        data.put("todayCreatedGroups", imGroupMapper.countTodayCreatedGroups());
        data.put("totalGroupMembers", imGroupMapper.countTotalGroupMembers());
        data.put("mutedGroups", imGroupMapper.countMutedGroups());
        data.put("largeGroups", imGroupMapper.countLargeGroups());

        return AjaxResult.success(data);
    }

    /**
     * 获取消息统计
     *
     * @return 消息统计数据
     */
    @GetMapping("/message-statistics")
    @ResponseBody
    public AjaxResult getMessageStatistics() {
        Map<String, Object> data = imMessageMapper.getMessageStatistics();
        data.put("sensitiveMessages", imMessageMapper.countSensitiveMessages());

        return AjaxResult.success(data);
    }

    // ==================== 新增数据可视化接口 ====================

    /**
     * 1. 获取趋势数据
     * 支持用户注册、消息发送、群组创建、每小时活跃度、每日活跃用户趋势
     *
     * @param type  趋势类型：user/message/group/hourly/daily
     * @param days  查询天数：7/30/90（默认7天）
     * @return 趋势数据
     */
    @GetMapping("/trend")
    @ResponseBody
    public AjaxResult getTrend(@RequestParam(defaultValue = "user") String type,
                               @RequestParam(defaultValue = "7") int days) {
        try {
            List<Map<String, Object>> data = null;

            switch (type) {
                case "user":
                    data = dashboardService.getUserRegistrationTrend(days);
                    break;
                case "message":
                    data = dashboardService.getMessageSendingTrend(days);
                    break;
                case "group":
                    data = dashboardService.getGroupCreationTrend(days);
                    break;
                case "hourly":
                    data = dashboardService.getHourlyActivityTrend(days);
                    break;
                case "daily":
                    data = dashboardService.getDailyActiveUsers(days);
                    break;
                default:
                    return AjaxResult.error("不支持的趋势类型: " + type);
            }

            return AjaxResult.success(data);
        } catch (Exception e) {
            logger.error("获取趋势数据失败", e);
            return AjaxResult.error("获取趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 2. 获取分布数据
     * 支持消息类型、文件类型、用户活跃度、会话类型分布
     *
     * @param type 分布类型：msgType/fileType/userActivity/conversationType
     * @param days 查询天数（可选）
     * @return 分布数据
     */
    @GetMapping("/distribution")
    @ResponseBody
    public AjaxResult getDistribution(@RequestParam(defaultValue = "msgType") String type,
                                      @RequestParam(required = false) Integer days) {
        try {
            List<Map<String, Object>> data = null;

            switch (type) {
                case "msgType":
                    data = dashboardService.getMessageTypeDistribution(days);
                    break;
                case "fileType":
                    data = dashboardService.getFileTypeDistribution(days);
                    break;
                case "userActivity":
                    if (days == null || days <= 0) {
                        days = 30; // 默认30天
                    }
                    data = dashboardService.getUserActivityDistribution(days);
                    break;
                case "conversationType":
                    data = dashboardService.getConversationTypeDistribution();
                    break;
                default:
                    return AjaxResult.error("不支持的分布类型: " + type);
            }

            return AjaxResult.success(data);
        } catch (Exception e) {
            logger.error("获取分布数据失败", e);
            return AjaxResult.error("获取分布数据失败: " + e.getMessage());
        }
    }

    /**
     * 3. 获取对比数据
     * 支持周对比、月对比、每小时对比
     *
     * @param type 对比类型：weekly/monthly/hourly
     * @return 对比数据
     */
    @GetMapping("/comparison")
    @ResponseBody
    public AjaxResult getComparison(@RequestParam(defaultValue = "weekly") String type) {
        try {
            Map<String, Object> data = null;

            switch (type) {
                case "weekly":
                    data = dashboardService.getWeeklyComparison();
                    break;
                case "monthly":
                    data = dashboardService.getMonthlyComparison();
                    break;
                case "hourly":
                    data = dashboardService.getHourlyComparison();
                    break;
                default:
                    return AjaxResult.error("不支持的对比类型: " + type);
            }

            return AjaxResult.success(data);
        } catch (Exception e) {
            logger.error("获取对比数据失败", e);
            return AjaxResult.error("获取对比数据失败: " + e.getMessage());
        }
    }

    /**
     * 4. 获取排行榜数据
     * 支持最活跃用户、最活跃群组、消息发送排行
     *
     * @param type  排行榜类型：users/groups/senders
     * @param days  查询天数（默认7天）
     * @param limit 返回条数（默认10）
     * @return 排行榜数据
     */
    @GetMapping("/ranking")
    @ResponseBody
    public AjaxResult getRanking(@RequestParam(defaultValue = "users") String type,
                                 @RequestParam(defaultValue = "7") int days,
                                 @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> data = null;

            switch (type) {
                case "users":
                    data = dashboardService.getTopActiveUsers(days, limit);
                    break;
                case "groups":
                    data = dashboardService.getTopActiveGroups(days, limit);
                    break;
                case "senders":
                    data = dashboardService.getTopMessageSenders(days, limit);
                    break;
                default:
                    return AjaxResult.error("不支持的排行榜类型: " + type);
            }

            return AjaxResult.success(data);
        } catch (Exception e) {
            logger.error("获取排行榜数据失败", e);
            return AjaxResult.error("获取排行榜数据失败: " + e.getMessage());
        }
    }

    /**
     * 5. 获取实时监控数据
     * 支持实时在线用户数、实时消息流量
     *
     * @param type 监控类型：online/messageFlow
     * @param minutes 最近多少分钟（仅对messageFlow有效，默认30分钟）
     * @return 实时监控数据
     */
    @GetMapping("/realtime")
    @ResponseBody
    public AjaxResult getRealtime(@RequestParam(defaultValue = "online") String type,
                                   @RequestParam(defaultValue = "30") int minutes) {
        try {
            Object data = null;

            switch (type) {
                case "online":
                    data = dashboardService.getRealtimeOnlineCount();
                    break;
                case "messageFlow":
                    data = dashboardService.getRealtimeMessageFlow(minutes);
                    break;
                default:
                    return AjaxResult.error("不支持的监控类型: " + type);
            }

            return AjaxResult.success(data);
        } catch (Exception e) {
            logger.error("获取实时监控数据失败", e);
            return AjaxResult.error("获取实时监控数据失败: " + e.getMessage());
        }
    }

    /**
     * 6. 刷新缓存
     * 清除Dashboard相关缓存，强制重新查询数据库（已禁用缓存功能）
     *
     * @param cacheType 缓存类型：all/trend/distribution/comparison/ranking/realtime/basic（默认all）
     * @return 操作结果
     */
    @PostMapping("/refresh")
    @ResponseBody
    public AjaxResult refreshCache(@RequestParam(defaultValue = "all") String cacheType) {
        try {
            // 缓存功能已禁用，直接返回成功
            // switch (cacheType) {
            //     case "all":
            //         cacheService.clearAll();
            //         break;
            //     case "trend":
            //         cacheService.clearTrendCache();
            //         break;
            //     case "distribution":
            //         cacheService.clearDistributionCache();
            //         break;
            //     case "comparison":
            //         cacheService.clearComparisonCache();
            //         break;
            //     case "ranking":
            //         cacheService.clearRankingCache();
            //         break;
            //     case "realtime":
            //         cacheService.clearRealtimeCache();
            //         break;
            //     case "basic":
            //         cacheService.clearBasicStats();
            //         break;
            //     default:
            //         return AjaxResult.error("不支持的缓存类型: " + cacheType);
            // }

            return AjaxResult.success("数据已刷新（当前未启用缓存）");
        } catch (Exception e) {
            logger.error("刷新数据失败", e);
            return AjaxResult.error("刷新数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取基础统计数据（含缓存）
     *
     * @return 基础统计数据
     */
    @GetMapping("/basic")
    @ResponseBody
    public AjaxResult getBasicStatistics() {
        try {
            Map<String, Object> data = dashboardService.getBasicStatistics();
            return AjaxResult.success(data);
        } catch (Exception e) {
            logger.error("获取基础统计数据失败", e);
            return AjaxResult.error("获取基础统计数据失败: " + e.getMessage());
        }
    }
}
