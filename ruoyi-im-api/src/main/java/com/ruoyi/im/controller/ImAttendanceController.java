package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImAttendance;
import com.ruoyi.im.service.ImAttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤打卡控制器
 * 提供打卡、打卡记录查询、统计等功能
 *
 * @author ruoyi
 */
@Tag(name = "考勤打卡", description = "考勤打卡、打卡记录、统计等接口")
@RestController
@RequestMapping("/api/im/attendance")
public class ImAttendanceController {

    @Autowired
    private ImAttendanceService attendanceService;

    /**
     * 上班打卡
     *
     * @param location 打卡位置（JSON格式：{"latitude": 0, "longitude": 0, "address": ""}）
     * @param deviceInfo 设备信息
     * @param userId 当前登录用户ID
     * @return 打卡结果
     */
    @Operation(summary = "上班打卡", description = "进行上班打卡操作")
    @PostMapping("/checkIn")
    public Result<ImAttendance> checkIn(@RequestParam(required = false) String location,
                                        @RequestParam(required = false) String deviceInfo,
                                        @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        ImAttendance attendance = attendanceService.checkIn(userId, location, deviceInfo);
        return Result.success("上班打卡成功", attendance);
    }

    /**
     * 下班打卡
     *
     * @param location 打卡位置（JSON格式）
     * @param deviceInfo 设备信息
     * @param userId 当前登录用户ID
     * @return 打卡结果
     */
    @Operation(summary = "下班打卡", description = "进行下班打卡操作")
    @PostMapping("/checkOut")
    public Result<ImAttendance> checkOut(@RequestParam(required = false) String location,
                                         @RequestParam(required = false) String deviceInfo,
                                         @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        ImAttendance attendance = attendanceService.checkOut(userId, location, deviceInfo);
        return Result.success("下班打卡成功", attendance);
    }

    /**
     * 获取今日打卡状态
     *
     * @param userId 当前登录用户ID
     * @return 今日打卡记录
     */
    @Operation(summary = "获取今日打卡状态", description = "获取用户今日的打卡状态")
    @GetMapping("/today")
    public Result<ImAttendance> getTodayStatus(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        ImAttendance attendance = attendanceService.getTodayAttendance(userId);
        if (attendance == null) {
            attendance = new ImAttendance();
        }
        return Result.success(attendance);
    }

    /**
     * 获取打卡记录详情
     *
     * @param id 打卡ID
     * @return 打卡记录
     */
    @Operation(summary = "获取打卡记录详情", description = "根据ID获取打卡记录详情")
    @GetMapping("/{id}")
    public Result<ImAttendance> getDetail(@PathVariable Long id) {
        ImAttendance attendance = attendanceService.getAttendanceById(id);
        return Result.success(attendance);
    }

    /**
     * 获取打卡记录列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 当前登录用户ID
     * @return 打卡记录列表
     */
    @Operation(summary = "获取打卡记录列表", description = "获取指定日期范围内的打卡记录")
    @GetMapping("/list")
    public Result<List<ImAttendance>> getList(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                              @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        List<ImAttendance> list = attendanceService.getAttendanceList(userId, startDate, endDate);
        return Result.success(list);
    }

    /**
     * 获取月度统计
     *
     * @param year 年份
     * @param month 月份
     * @param userId 当前登录用户ID
     * @return 统计数据
     */
    @Operation(summary = "获取月度统计", description = "获取指定月份的考勤统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam int year,
                                                     @RequestParam int month,
                                                     @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        Map<String, Object> statistics = attendanceService.getMonthlyStatistics(userId, year, month);
        return Result.success(statistics);
    }

    /**
     * 补卡申请
     *
     * @param id 打卡ID
     * @param reason 补卡原因
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "补卡申请", description = "申请补卡，需要审批")
    @PostMapping("/{id}/supplement")
    public Result<Void> applySupplement(@PathVariable Long id,
                                        @RequestParam String reason,
                                        @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        attendanceService.applySupplement(id, reason, userId);
        return Result.success("补卡申请已提交");
    }

    /**
     * 审批补卡申请
     *
     * @param id 打卡ID
     * @param approved 是否通过
     * @param comment 审批意见
     * @param approverId 审批人ID
     * @return 操作结果
     */
    @Operation(summary = "审批补卡申请", description = "审批补卡申请")
    @PostMapping("/{id}/approve")
    public Result<Void> approveSupplement(@PathVariable Long id,
                                          @RequestParam boolean approved,
                                          @RequestParam(required = false) String comment,
                                          @RequestHeader(value = "approverId", required = false) Long approverId) {
        if (approverId == null) {
            approverId = 2L; // 开发环境默认审批人
        }
        attendanceService.approveSupplement(id, approverId, approved, comment);
        return Result.success(approved ? "已通过" : "已拒绝");
    }

    /**
     * 删除打卡记录
     *
     * @param id 打卡ID
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除打卡记录", description = "删除指定的打卡记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // 开发环境默认用户
        }
        attendanceService.deleteAttendance(id, userId);
        return Result.success("删除成功");
    }
}
