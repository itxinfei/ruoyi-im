package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * IM考勤记录数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM考勤记录管理相关的数据库操作</p>
 * <p>主要功能包括：考勤记录的增删改查、考勤审批、按日期范围查询、考勤统计等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImAttendanceMapper {


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
    List<ImAttendance> selectAttendanceByDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

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
     * 删除考勤记录
     *
     * @param id 考勤记录主键
     * @return 结果
     */
    int deleteImAttendanceById(Long id);

    /**
     * 批量删除考勤记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImAttendanceByIds(Long[] ids);

    /**
     * 更新审批状态
     *
     * @param id 考勤记录ID
     * @param approveStatus 审批状态
     * @param approverId 审批人ID
     * @param approveComment 审批意见
     * @return 结果
     */
    int updateApproveStatus(@Param("id") Long id,
                           @Param("approveStatus") String approveStatus,
                           @Param("approverId") Long approverId,
                           @Param("approveComment") String approveComment);

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
