package com.ruoyi.web.controller.im;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.mapper.ImGroupMapper;
import com.ruoyi.web.mapper.ImMessageMapper;
import com.ruoyi.web.mapper.ImUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * IM系统仪表板控制器
 * 提供首页统计数据接口
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/im/dashboard")
public class ImDashboardController extends BaseController {

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private ImGroupMapper imGroupMapper;

    @Autowired
    private ImMessageMapper imMessageMapper;

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
}
