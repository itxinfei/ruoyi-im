package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.util.TestDataInitializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试数据管理控制器
 * 用于初始化和管理测试数据
 *
 * @author ruoyi
 */
@Tag(name = "测试数据管理", description = "用于初始化测试数据的接口")
@RestController
@RequestMapping("/api/im/test-data")
public class ImTestDataController {

    private final TestDataInitializer testDataInitializer;

    public ImTestDataController(TestDataInitializer testDataInitializer) {
        this.testDataInitializer = testDataInitializer;
    }

    /**
     * 初始化好友关系数据
     * 为张三、李四添加所有其他用户为好友
     */
    @Operation(summary = "初始化好友关系", description = "为张三、李四添加所有其他用户为好友")
    @PostMapping("/init-friends")
    public Result<Map<String, Object>> initFriendData() {
        Map<String, Object> result = new HashMap<>();

        try {
            testDataInitializer.initFriendData();
            result.put("success", true);
            result.put("message", "好友关系数据初始化成功");
            result.put("tip", "请调用 POST /api/im/contact/cache/clear 清除缓存后刷新页面");
            return Result.success("初始化成功", result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "初始化失败: " + e.getMessage());
            return Result.success("初始化失败", result);
        }
    }

    /**
     * 获取好友关系统计
     */
    @Operation(summary = "获取好友统计", description = "查看当前数据库中的好友关系数据")
    @GetMapping("/friend-stats")
    public Result<Map<String, Object>> getFriendStatistics() {
        // 这里可以添加统计逻辑
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "请在后端日志中查看详细统计");
        stats.put("action", "已调用 printFriendStatistics()，请查看后端日志");
        return Result.success(stats);
    }

    /**
     * 打印好友关系统计到日志
     */
    @Operation(summary = "打印好友统计到日志", description = "在控制台打印好友关系统计")
    @PostMapping("/print-stats")
    public Result<Void> printStatistics() {
        testDataInitializer.printFriendStatistics();
        return Result.success("已打印到后端日志");
    }
}
