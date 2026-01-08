package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImAttendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤打卡服务接口
 *
 * @author ruoyi
 */
public interface ImAttendanceService {

    /**
     * 上班打卡
     *
     * @param userId 用户ID
     * @param location 打卡位置（JSON格式）
     * @param deviceInfo 设备信息
     * @return 打卡记录
     */
    ImAttendance checkIn(Long userId, String location, String deviceInfo);

    /**
     * 下班打卡
     *
     * @param userId 用户ID
     * @param location 打卡位置（JSON格式）
     * @param deviceInfo 设备信息
     * @return 打卡记录
     */
    ImAttendance checkOut(Long userId, String location, String deviceInfo);

    /**
     * 获取今日打卡状态
     *
     * @param userId 用户ID
     * @return 今日打卡记录
     */
    ImAttendance getTodayAttendance(Long userId);

    /**
     * 获取打卡记录详情
     *
     * @param id 打卡ID
     * @return 打卡记录
     */
    ImAttendance getAttendanceById(Long id);

    /**
     * 获取用户打卡记录列表
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡记录列表
     */
    List<ImAttendance> getAttendanceList(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取月度统计数据
     *
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @return 统计数据
     */
    Map<String, Object> getMonthlyStatistics(Long userId, int year, int month);

    /**
     * 补卡申请
     *
     * @param attendanceId 打卡ID
     * @param reason 补卡原因
     * @param userId 用户ID
     * @return 结果
     */
    boolean applySupplement(Long attendanceId, String reason, Long userId);

    /**
     * 审批补卡申请
     *
     * @param attendanceId 打卡ID
     * @param approverId 审批人ID
     * @param approved 是否通过
     * @param comment 审批意见
     * @return 结果
     */
    boolean approveSupplement(Long attendanceId, Long approverId, boolean approved, String comment);

    /**
     * 更新打卡记录
     *
     * @param attendance 打卡记录
     * @return 结果
     */
    int updateAttendance(ImAttendance attendance);

    /**
     * 删除打卡记录
     *
     * @param id 打卡ID
     * @param userId 用户ID
     * @return 结果
     */
    int deleteAttendance(Long id, Long userId);
}
