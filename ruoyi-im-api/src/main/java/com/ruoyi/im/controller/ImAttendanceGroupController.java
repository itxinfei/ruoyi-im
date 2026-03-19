package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.attendance.ImAttendanceGroupCreateRequest;
import com.ruoyi.im.service.ImAttendanceGroupService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.attendance.ImAttendanceGroupVO;
import com.ruoyi.im.vo.attendance.ImAttendanceShiftVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 考勤组控制器
 * 提供考勤组管理、成员管理、排班等功能
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/attendance/group")
public class ImAttendanceGroupController {

    private static final Logger log = LoggerFactory.getLogger(ImAttendanceGroupController.class);

    private final ImAttendanceGroupService attendanceGroupService;

    /**
     * 构造器注入依赖
     *
     * @param attendanceGroupService 考勤组服务
     */
    public ImAttendanceGroupController(ImAttendanceGroupService attendanceGroupService) {
        this.attendanceGroupService = attendanceGroupService;
    }

    // ==================== 考勤组管理 ====================

    /**
     * 创建考勤组
     */
    
    @PostMapping("/create")
    public Result<Long> createGroup(
             @Valid @RequestBody ImAttendanceGroupCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long groupId = attendanceGroupService.createGroup(request, userId);
            return Result.success("创建成功", groupId);
        } catch (Exception e) {
            log.error("创建考勤组失败: userId={}", userId, e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新考勤组
     */
    
    @PutMapping("/{groupId}")
    public Result<Void> updateGroup(
             @PathVariable Long groupId,
             @Valid @RequestBody ImAttendanceGroupCreateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            attendanceGroupService.updateGroup(groupId, request, userId);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新考勤组失败: groupId={}, userId={}", groupId, userId, e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除考勤组
     */
    
    @DeleteMapping("/{groupId}")
    public Result<Void> deleteGroup(
             @PathVariable Long groupId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            attendanceGroupService.deleteGroup(groupId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除考勤组失败: groupId={}, userId={}", groupId, userId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取考勤组详情
     */
    
    @GetMapping("/{groupId}")
    public Result<ImAttendanceGroupVO> getGroupDetail(
             @PathVariable Long groupId) {
        try {
            ImAttendanceGroupVO group = attendanceGroupService.getGroupDetail(groupId);
            return Result.success(group);
        } catch (Exception e) {
            log.error("获取考勤组详情失败: groupId={}", groupId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取考勤组列表
     */
    
    @GetMapping("/list")
    public Result<List<ImAttendanceGroupVO>> getGroupList() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImAttendanceGroupVO> groups = attendanceGroupService.getGroupList(userId);
            return Result.success(groups);
        } catch (Exception e) {
            log.error("获取考勤组列表失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户所在考勤组
     */
    
    @GetMapping("/my")
    public Result<ImAttendanceGroupVO> getUserGroup() {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            ImAttendanceGroupVO group = attendanceGroupService.getUserGroup(userId);
            return Result.success(group);
        } catch (Exception e) {
            log.error("获取用户考勤组失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    // ==================== 成员管理 ====================

    /**
     * 添加成员
     */
    
    @PostMapping("/{groupId}/members/add")
    public Result<Void> addMembers(
             @PathVariable Long groupId,
             @RequestBody List<Long> memberIds) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            attendanceGroupService.addMembers(groupId, memberIds, userId);
            return Result.success("添加成功");
        } catch (Exception e) {
            log.error("添加成员失败: groupId={}, userId={}", groupId, userId, e);
            return Result.fail("添加失败: " + e.getMessage());
        }
    }

    /**
     * 移除成员
     */
    
    @PostMapping("/{groupId}/members/remove")
    public Result<Void> removeMembers(
             @PathVariable Long groupId,
             @RequestBody List<Long> memberIds) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            attendanceGroupService.removeMembers(groupId, memberIds, userId);
            return Result.success("移除成功");
        } catch (Exception e) {
            log.error("移除成员失败: groupId={}, userId={}", groupId, userId, e);
            return Result.fail("移除失败: " + e.getMessage());
        }
    }

    /**
     * 获取考勤组成员列表
     */
    
    @GetMapping("/{groupId}/members")
    public Result<List<Long>> getGroupMembers(
             @PathVariable Long groupId) {
        try {
            List<Long> members = attendanceGroupService.getGroupMembers(groupId);
            return Result.success(members);
        } catch (Exception e) {
            log.error("获取成员列表失败: groupId={}", groupId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    // ==================== 班次管理 ====================

    /**
     * 创建班次
     */
    
    @PostMapping("/{groupId}/shift/create")
    public Result<Long> createShift(
             @PathVariable Long groupId,
             @RequestParam String shiftName,
             @RequestParam String workStartTime,
             @RequestParam String workEndTime) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            Long shiftId = attendanceGroupService.createShift(groupId, shiftName, workStartTime, workEndTime, userId);
            return Result.success("创建成功", shiftId);
        } catch (Exception e) {
            log.error("创建班次失败: groupId={}", groupId, e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    /**
     * 删除班次
     */
    
    @DeleteMapping("/shift/{shiftId}")
    public Result<Void> deleteShift(
             @PathVariable Long shiftId) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            attendanceGroupService.deleteShift(shiftId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除班次失败: shiftId={}", shiftId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取班次列表
     */
    
    @GetMapping("/{groupId}/shifts")
    public Result<List<ImAttendanceShiftVO>> getShiftList(
             @PathVariable Long groupId) {
        try {
            List<ImAttendanceShiftVO> shifts = attendanceGroupService.getShiftList(groupId);
            return Result.success(shifts);
        } catch (Exception e) {
            log.error("获取班次列表失败: groupId={}", groupId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    // ==================== 排班管理 ====================

    /**
     * 批量排班
     */
    
    @PostMapping("/{groupId}/schedule")
    public Result<Void> schedule(
             @PathVariable Long groupId,
             @RequestParam Long userId,
             @RequestParam Long shiftId,
             @RequestParam LocalDate startDate,
             @RequestParam LocalDate endDate,
             @RequestBody List<Integer> workDays) {
        Long operatorId = SecurityUtils.getLoginUserId();

        try {
            attendanceGroupService.schedule(groupId, userId, shiftId, startDate, endDate, workDays);
            return Result.success("排班成功");
        } catch (Exception e) {
            log.error("批量排班失败: groupId={}", groupId, e);
            return Result.fail("排班失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户排班
     */
    
    @GetMapping("/schedule/my")
    public Result<List<ImAttendanceShiftVO>> getUserSchedule(
             @RequestParam LocalDate startDate,
             @RequestParam LocalDate endDate) {
        Long userId = SecurityUtils.getLoginUserId();

        try {
            List<ImAttendanceShiftVO> schedule = attendanceGroupService.getUserSchedule(userId, startDate, endDate);
            return Result.success(schedule);
        } catch (Exception e) {
            log.error("获取用户排班失败: userId={}", userId, e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }
}

