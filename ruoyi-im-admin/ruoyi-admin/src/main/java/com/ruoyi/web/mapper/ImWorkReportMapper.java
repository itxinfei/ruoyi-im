package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImWorkReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * ImWorkReport接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImWorkReportMapper {


    /**
     * 查询工作报告
     *
     * @param id 工作报告主键
     * @return 工作报告
     */
    ImWorkReport selectImWorkReportById(Long id);

    /**
     * 查询工作报告列表
     *
     * @param imWorkReport 工作报告
     * @return 工作报告集合
     */
    List<ImWorkReport> selectImWorkReportList(ImWorkReport imWorkReport);

    /**
     * 查询用户的工作报告
     *
     * @param userId 用户ID
     * @return 报告列表
     */
    List<ImWorkReport> selectReportsByUserId(Long userId);

    /**
     * 新增工作报告
     *
     * @param imWorkReport 工作报告
     * @return 结果
     */
    int insertImWorkReport(ImWorkReport imWorkReport);

    /**
     * 修改工作报告
     *
     * @param imWorkReport 工作报告
     * @return 结果
     */
    int updateImWorkReport(ImWorkReport imWorkReport);

    /**
     * 删除工作报告
     *
     * @param id 工作报告主键
     * @return 结果
     */
    int deleteImWorkReportById(Long id);

    /**
     * 批量删除工作报告
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImWorkReportByIds(Long[] ids);

    /**
     * 更新报告状态
     *
     * @param id 报告ID
     * @param status 状态
     * @return 结果
     */
    int updateReportStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 审批报告
     *
     * @param id 报告ID
     * @param approverId 审批人ID
     * @param status 状态
     * @param remark 备注
     * @return 结果
     */
    int approveReport(@Param("id") Long id, @Param("approverId") Long approverId,
                      @Param("status") String status, @Param("remark") String remark);

    /**
     * 获取工作报告统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getReportStatistics();

    /**
     * 按类型统计报告数量
     *
     * @return 统计列表
     */
    List<Map<String, Object>> countByReportType();

    /**
     * 查询指定日期范围的报告
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告列表
     */
    List<ImWorkReport> selectReportsByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 查询待审批报告
     *
     * @return 报告列表
     */
    List<ImWorkReport> selectPendingApprovalReports();
}
