package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImAuditExportRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 审计导出请求Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImAuditExportRequestMapper extends BaseMapper<ImAuditExportRequest> {

    /**
     * 查询审计导出请求列表（包含申请人和审批人详细信息）
     * 
     * @param requesterId 申请人用户ID（可选）
     * @param approverId 审批人用户ID（可选）
     * @param status 审批状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param limit 限制数量
     * @return 审计导出请求列表
     */
    List<ImAuditExportRequest> selectRequestsWithDetails(@Param("requesterId") Long requesterId,
                                                        @Param("approverId") Long approverId,
                                                        @Param("status") String status,
                                                        @Param("startTime") Date startTime,
                                                        @Param("endTime") Date endTime,
                                                        @Param("limit") Integer limit);

    /**
     * 查询用户的导出请求列表
     * 
     * @param requesterId 申请人用户ID
     * @param limit 限制数量
     * @return 审计导出请求列表
     */
    List<ImAuditExportRequest> selectUserRequests(@Param("requesterId") Long requesterId, @Param("limit") Integer limit);

    /**
     * 查询待审批的导出请求列表
     * 
     * @param limit 限制数量
     * @return 审计导出请求列表
     */
    List<ImAuditExportRequest> selectPendingRequests(@Param("limit") Integer limit);

    /**
     * 查询已完成的导出请求列表
     * 
     * @param requesterId 申请人用户ID（可选）
     * @param limit 限制数量
     * @return 审计导出请求列表
     */
    List<ImAuditExportRequest> selectCompletedRequests(@Param("requesterId") Long requesterId, @Param("limit") Integer limit);

    /**
     * 统计用户导出请求数量
     * 
     * @param requesterId 申请人用户ID
     * @param status 审批状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 请求数量
     */
    int countUserRequests(@Param("requesterId") Long requesterId,
                         @Param("status") String status,
                         @Param("startTime") Date startTime,
                         @Param("endTime") Date endTime);

    /**
     * 统计待审批请求数量
     * 
     * @return 待审批请求数量
     */
    int countPendingRequests();

    /**
     * 更新请求审批状态
     * 
     * @param requestId 请求ID
     * @param status 新状态
     * @param approverId 审批人ID
     * @param approvedTime 审批时间
     * @param approvalComment 审批意见
     * @return 更新数量
     */
    int updateApprovalStatus(@Param("requestId") Long requestId,
                            @Param("status") String status,
                            @Param("approverId") Long approverId,
                            @Param("approvedTime") Date approvedTime,
                            @Param("approvalComment") String approvalComment);

    /**
     * 更新导出完成信息
     * 
     * @param requestId 请求ID
     * @param exportFilePath 导出文件路径
     * @param exportCompletedTime 导出完成时间
     * @return 更新数量
     */
    int updateExportCompleted(@Param("requestId") Long requestId,
                             @Param("exportFilePath") String exportFilePath,
                             @Param("exportCompletedTime") Date exportCompletedTime);

    /**
     * 批量更新请求状态
     * 
     * @param requestIds 请求ID列表
     * @param status 新状态
     * @param approverId 审批人ID
     * @return 更新数量
     */
    int updateStatusBatch(@Param("requestIds") List<Long> requestIds,
                         @Param("status") String status,
                         @Param("approverId") Long approverId);

    /**
     * 查询导出请求统计（按日期分组）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息列表
     */
    List<Object> selectRequestStatistics(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 查询频繁申请用户列表
     * 
     * @param days 统计天数
     * @param minCount 最小申请次数
     * @param limit 限制数量
     * @return 用户统计列表
     */
    List<Object> selectFrequentRequesters(@Param("days") int days,
                                         @Param("minCount") int minCount,
                                         @Param("limit") Integer limit);

    /**
     * 删除过期请求（超过指定天数的已完成请求）
     * 
     * @param days 天数
     * @return 删除数量
     */
    int deleteExpiredRequests(@Param("days") int days);

    /**
     * 删除用户的所有导出请求
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);
}