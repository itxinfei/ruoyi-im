package com.ruoyi.im.service;

import com.ruoyi.im.dto.attendance.ImAttendanceGroupCreateRequest;
import com.ruoyi.im.vo.attendance.ImAttendanceGroupVO;
import com.ruoyi.im.vo.attendance.ImAttendanceShiftVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤组服务接口
 *
 * @author ruoyi
 */
public interface ImAttendanceGroupService {

    // ==================== 考勤组管理 ====================

    /**
     * 创建考勤组
     *
     * @param request 创建请求
     * @param userId  操作用户ID
     * @return 考勤组ID
     */
    Long createGroup(ImAttendanceGroupCreateRequest request, Long userId);

    /**
     * 更新考勤组
     *
     * @param groupId 考勤组ID
     * @param request 更新请求
     * @param userId  操作用户ID
     */
    void updateGroup(Long groupId, ImAttendanceGroupCreateRequest request, Long userId);

    /**
     * 删除考勤组
     *
     * @param groupId 考勤组ID
     * @param userId  操作用户ID
     */
    void deleteGroup(Long groupId, Long userId);

    /**
     * 获取考勤组详情
     *
     * @param groupId 考勤组ID
     * @return 考勤组VO
     */
    ImAttendanceGroupVO getGroupDetail(Long groupId);

    /**
     * 获取考勤组列表
     *
     * @param userId 用户ID
     * @return 考勤组列表
     */
    List<ImAttendanceGroupVO> getGroupList(Long userId);

    /**
     * 获取用户所在的考勤组
     *
     * @param userId 用户ID
     * @return 考勤组VO
     */
    ImAttendanceGroupVO getUserGroup(Long userId);

    // ==================== 成员管理 ====================

    /**
     * 添加成员
     *
     * @param groupId  考勤组ID
     * @param memberIds 成员ID列表
     * @param userId   操作用户ID
     */
    void addMembers(Long groupId, List<Long> memberIds, Long userId);

    /**
     * 移除成员
     *
     * @param groupId 考勤组ID
     * @param memberIds 成员ID列表
     * @param userId   操作用户ID
     */
    void removeMembers(Long groupId, List<Long> memberIds, Long userId);

    /**
     * 获取考勤组成员列表
     *
     * @param groupId 考勤组ID
     * @return 成员ID列表
     */
    List<Long> getGroupMembers(Long groupId);

    // ==================== 班次管理 ====================

    /**
     * 创建班次
     *
     * @param groupId 考勤组ID
     * @param shiftName 班次名称
     * @param workStartTime 上班时间
     * @param workEndTime 下班时间
     * @param userId 操作用户ID
     * @return 班次ID
     */
    Long createShift(Long groupId, String shiftName, String workStartTime,
                     String workEndTime, Long userId);

    /**
     * 删除班次
     *
     * @param shiftId 班次ID
     * @param userId  操作用户ID
     */
    void deleteShift(Long shiftId, Long userId);

    /**
     * 获取考勤组的班次列表
     *
     * @param groupId 考勤组ID
     * @return 班次列表
     */
    List<ImAttendanceShiftVO> getShiftList(Long groupId);

    // ==================== 排班管理 ====================

    /**
     * 批量排班
     *
     * @param groupId    考勤组ID
     * @param userId     用户ID
     * @param shiftId    班次ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param workDays   工作日（1-7）
     */
    void schedule(Long groupId, Long userId, Long shiftId,
                  LocalDate startDate, LocalDate endDate, List<Integer> workDays);

    /**
     * 获取用户排班
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班信息
     */
    List<ImAttendanceShiftVO> getUserSchedule(Long userId, LocalDate startDate, LocalDate endDate);
}
