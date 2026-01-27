package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImAttendance;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImAttendanceMapper;
import com.ruoyi.im.service.ImAttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考勤打卡服务实现
 *
 * @author ruoyi
 */
@Service
public class ImAttendanceServiceImpl implements ImAttendanceService {

    private final ImAttendanceMapper attendanceMapper;

    /**
     * 构造器注入依赖
     *
     * @param attendanceMapper 考勤Mapper
     */
    public ImAttendanceServiceImpl(ImAttendanceMapper attendanceMapper) {
        this.attendanceMapper = attendanceMapper;
    }

    /**
     * 上班时间（9:00）
     */
    private static final LocalTime CHECK_IN_TIME = LocalTime.of(9, 0);

    /**
     * 下班时间（18:00）
     */
    private static final LocalTime CHECK_OUT_TIME = LocalTime.of(18, 0);

    /**
     * 迟到容忍时间（分钟）
     */
    private static final int LATE_TOLERANCE_MINUTES = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImAttendance checkIn(Long userId, String location, String deviceInfo) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 查询今日打卡记录
        ImAttendance attendance = attendanceMapper.selectTodayAttendance(userId, today);

        if (attendance == null) {
            // 创建新的打卡记录
            attendance = new ImAttendance();
            attendance.setUserId(userId);
            attendance.setAttendanceDate(today);
            attendance.setCheckInTime(now);
            attendance.setCheckInTime(now);
            attendance.setCheckInLocation(location);
            attendance.setDeviceInfo(deviceInfo);
            attendance.setCreateTime(now);
            attendance.setUpdateTime(now);

            // 判断是否迟到
            LocalTime checkInTime = now.toLocalTime();
            String status = "NORMAL";
            if (checkInTime.isAfter(CHECK_IN_TIME.plusMinutes(LATE_TOLERANCE_MINUTES))) {
                status = "LATE";
            }
            attendance.setCheckInStatus(status);
            attendance.setAttendanceType("WORK");

            attendanceMapper.insertImAttendance(attendance);
        } else if (attendance.getCheckInTime() == null) {
            // 已有记录但未上班打卡
            attendance.setCheckInTime(now);
            attendance.setCheckInLocation(location);
            attendance.setDeviceInfo(deviceInfo);
            attendance.setUpdateTime(now);

            LocalTime checkInTime = now.toLocalTime();
            String status = "NORMAL";
            if (checkInTime.isAfter(CHECK_IN_TIME.plusMinutes(LATE_TOLERANCE_MINUTES))) {
                status = "LATE";
            }
            attendance.setCheckInStatus(status);

            attendanceMapper.updateImAttendance(attendance);
        } else {
            throw new BusinessException("今日已上班打卡，请勿重复打卡");
        }

        return attendance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImAttendance checkOut(Long userId, String location, String deviceInfo) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 查询今日打卡记录
        ImAttendance attendance = attendanceMapper.selectTodayAttendance(userId, today);

        if (attendance == null) {
            throw new BusinessException("请先进行上班打卡");
        }

        if (attendance.getCheckOutTime() != null) {
            throw new BusinessException("今日已下班打卡，请勿重复打卡");
        }

        // 更新下班打卡信息
        attendance.setCheckOutTime(now);
        attendance.setCheckOutLocation(location);
        attendance.setUpdateTime(now);

        // 判断是否早退
        LocalTime checkOutTime = now.toLocalTime();
        String status = "NORMAL";
        if (checkOutTime.isBefore(CHECK_OUT_TIME.minusMinutes(30))) {
            status = "EARLY";
        }
        attendance.setCheckOutStatus(status);

        // 计算工作时长（分钟）
        if (attendance.getCheckInTime() != null) {
            long minutes = java.time.Duration.between(attendance.getCheckInTime(), now).toMinutes();
            attendance.setWorkMinutes((int) minutes);
        }

        attendanceMapper.updateImAttendance(attendance);

        return attendance;
    }

    @Override
    public ImAttendance getTodayAttendance(Long userId) {
        LocalDate today = LocalDate.now();
        return attendanceMapper.selectTodayAttendance(userId, today);
    }

    @Override
    public ImAttendance getAttendanceById(Long id) {
        return attendanceMapper.selectImAttendanceById(id);
    }

    @Override
    public List<ImAttendance> getAttendanceList(Long userId, LocalDate startDate, LocalDate endDate) {
        return attendanceMapper.selectByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public Map<String, Object> getMonthlyStatistics(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        Map<String, Object> statistics = attendanceMapper.statisticsByUserIdAndDateRange(userId, startDate, endDate);

        // 计算本月工作日天数
        int workDays = calculateWorkDays(year, month);
        statistics.put("workDays", workDays);
        statistics.put("year", year);
        statistics.put("month", month);

        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applySupplement(Long attendanceId, String reason, Long userId) {
        ImAttendance attendance = attendanceMapper.selectImAttendanceById(attendanceId);
        if (attendance == null) {
            throw new BusinessException("打卡记录不存在");
        }

        if (!attendance.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        if ("PENDING".equals(attendance.getApproveStatus())) {
            throw new BusinessException("已有审批中的申请");
        }

        attendance.setApproveStatus("PENDING");
        attendance.setRemark(attendance.getRemark() != null ? attendance.getRemark() + ";补卡原因:" + reason : "补卡原因:" + reason);
        attendance.setUpdateTime(LocalDateTime.now());

        return attendanceMapper.updateImAttendance(attendance) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveSupplement(Long attendanceId, Long approverId, boolean approved, String comment) {
        ImAttendance attendance = attendanceMapper.selectImAttendanceById(attendanceId);
        if (attendance == null) {
            throw new BusinessException("打卡记录不存在");
        }

        if (!"PENDING".equals(attendance.getApproveStatus())) {
            throw new BusinessException("该申请已被处理");
        }

        attendance.setApproverId(approverId);
        attendance.setApproveTime(LocalDateTime.now());
        attendance.setApproveComment(comment);
        attendance.setApproveStatus(approved ? "APPROVED" : "REJECTED");
        attendance.setUpdateTime(LocalDateTime.now());

        // 如果审批通过，需要根据情况补齐打卡记录
        if (approved) {
            if (attendance.getCheckInTime() == null) {
                attendance.setCheckInTime(LocalDateTime.of(attendance.getAttendanceDate(), CHECK_IN_TIME));
                attendance.setCheckInStatus("NORMAL");
            }
            if (attendance.getCheckOutTime() == null) {
                attendance.setCheckOutTime(LocalDateTime.of(attendance.getAttendanceDate(), CHECK_OUT_TIME));
                attendance.setCheckOutStatus("NORMAL");

                // 计算工作时长
                if (attendance.getCheckInTime() != null) {
                    long minutes = java.time.Duration.between(
                            LocalDateTime.of(attendance.getAttendanceDate(), CHECK_IN_TIME),
                            LocalDateTime.of(attendance.getAttendanceDate(), CHECK_OUT_TIME)
                    ).toMinutes();
                    attendance.setWorkMinutes((int) minutes);
                }
            }
        }

        return attendanceMapper.updateImAttendance(attendance) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAttendance(ImAttendance attendance) {
        attendance.setUpdateTime(LocalDateTime.now());
        return attendanceMapper.updateImAttendance(attendance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAttendance(Long id, Long userId) {
        ImAttendance attendance = attendanceMapper.selectImAttendanceById(id);
        if (attendance == null) {
            throw new BusinessException("打卡记录不存在");
        }

        if (!attendance.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        return attendanceMapper.deleteImAttendanceById(id);
    }

    /**
     * 计算指定年月的工作日天数（排除周末）
     *
     * @param year 年份
     * @param month 月份
     * @return 工作日天数
     */
    private int calculateWorkDays(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = date.plusMonths(1).minusDays(1);
        int workDays = 0;

        while (!date.isAfter(endOfMonth)) {
            int dayOfWeek = date.getDayOfWeek().getValue();
            if (dayOfWeek <= 5) { // 1-5 是周一到周五
                workDays++;
            }
            date = date.plusDays(1);
        }

        return workDays;
    }
}
