package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImAttendance;
import java.util.List;
import java.util.Map;

/**
 * 考勤记录Service接口
 *
 * @author ruoyi
 */
public interface ImAttendanceService {

    /**
     * 查询考勤记录
     *
     * @param id 考勤记录主键
     * @return 考勤记录
     */
    ImAttendance selectImAttendanceById(Long id);

    /**
     * 查询考勤记录列表
     *
     * @param imAttendance 考勤记录
     * @return 考勤记录集合
     */
    List<ImAttendance> selectImAttendanceList(ImAttendance imAttendance);

    /**
     * 查询用户的考勤记录
     *
     * @param userId 用户ID
     * @return 考勤记录列表
     */
    List<ImAttendance> selectAttendanceByUserId(Long userId);

    /**
     * 查询指定日期范围内的考勤记录
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤记录列表
     */
    List<ImAttendance> selectAttendanceByDateRange(Long userId, String startDate, String endDate);

    /**
     * 新增考勤记录
     *
     * @param imAttendance 考勤记录
     * @return 结果
     */
    int insertImAttendance(ImAttendance imAttendance);

    /**
     * 修改考勤记录
     *
     * @param imAttendance 考勤记录
     * @return 结果
     */
    int updateImAttendance(ImAttendance imAttendance);

    /**
     * 批量删除考勤记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImAttendanceByIds(Long[] ids);

    /**
     * 删除考勤记录信息
     *
     * @param id 考勤记录主键
     * @return 结果
     */
    int deleteImAttendanceById(Long id);

    /**
     * 审批考勤记录
     *
     * @param id 考勤记录ID
     * @param approveStatus 审批状态
     * @param approverId 审批人ID
     * @param approveComment 审批意见
     * @return 结果
     */
    int approveAttendance(Long id, String approveStatus, Long approverId, String approveComment);

    /**
     * 批量审批考勤记录
     *
     * @param ids 考勤记录ID数组
     * @param approveStatus 审批状态
     * @param approverId 审批人ID
     * @param approveComment 审批意见
     * @return 结果
     */
    int batchApproveAttendance(Long[] ids, String approveStatus, Long approverId, String approveComment);

    /**
     * 获取考勤统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getAttendanceStatistics();

    /**
     * 查询今日考勤记录
     *
     * @param userId 用户ID
     * @return 考勤记录列表
     */
    List<ImAttendance> selectTodayAttendance(Long userId);

    /**
     * 查询待审批的考勤记录
     *
     * @return 考勤记录列表
     */
    List<ImAttendance> selectPendingApprovalList();
}
