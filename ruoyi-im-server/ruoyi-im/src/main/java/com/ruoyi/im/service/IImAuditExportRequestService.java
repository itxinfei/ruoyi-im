package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImAuditExportRequest;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 审计导出请求Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImAuditExportRequestService extends IService<ImAuditExportRequest> {

    /**
     * 提交审计导出请求
     * 
     * @param request 导出请求信息
     * @return 是否成功
     */
    boolean submitExportRequest(ImAuditExportRequest request);

    /**
     * 审批导出请求
     * 
     * @param requestId 请求ID
     * @param approverId 审批人ID
     * @param status 审批状态（APPROVED/REJECTED）
     * @param comment 审批意见
     * @return 是否成功
     */
    boolean approveExportRequest(Long requestId, Long approverId, String status, String comment);

    /**
     * 批量审批导出请求
     * 
     * @param requestIds 请求ID列表
     * @param approverId 审批人ID
     * @param status 审批状态
     * @param comment 审批意见
     * @return 审批数量
     */
    int approveExportRequestBatch(List<Long> requestIds, Long approverId, String status, String comment);

    /**
     * 执行数据导出
     * 
     * @param requestId 请求ID
     * @return 是否成功
     */
    boolean executeExport(Long requestId);

    /**
     * 更新导出完成信息
     * 
     * @param requestId 请求ID
     * @param filePath 导出文件路径
     * @param fileSize 文件大小
     * @return 是否成功
     */
    boolean updateExportCompletion(Long requestId, String filePath, Long fileSize);

    /**
     * 查询导出请求列表
     * 
     * @param requesterId 请求人ID（可选）
     * @param exportType 导出类型（可选）
     * @param status 审批状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 导出请求列表
     */
    List<ImAuditExportRequest> selectExportRequests(Long requesterId, String exportType, String status, Date startTime, Date endTime);

    /**
     * 查询用户的导出请求
     * 
     * @param requesterId 请求人ID
     * @param status 审批状态（可选）
     * @return 导出请求列表
     */
    List<ImAuditExportRequest> selectUserExportRequests(Long requesterId, String status);

    /**
     * 查询待审批的导出请求
     * 
     * @param exportType 导出类型（可选）
     * @param limit 限制数量
     * @return 导出请求列表
     */
    List<ImAuditExportRequest> selectPendingRequests(String exportType, int limit);

    /**
     * 查询已完成的导出请求
     * 
     * @param requesterId 请求人ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 导出请求列表
     */
    List<ImAuditExportRequest> selectCompletedRequests(Long requesterId, Date startTime, Date endTime);

    /**
     * 统计导出请求数量
     * 
     * @param requesterId 请求人ID（可选）
     * @param exportType 导出类型（可选）
     * @param status 审批状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 请求数量
     */
    int countExportRequests(Long requesterId, String exportType, String status, Date startTime, Date endTime);

    /**
     * 获取导出请求详细信息
     * 
     * @param requestId 请求ID
     * @return 导出请求信息
     */
    ImAuditExportRequest getExportRequestDetail(Long requestId);

    /**
     * 撤回导出请求
     * 
     * @param requestId 请求ID
     * @param requesterId 请求人ID
     * @return 是否成功
     */
    boolean withdrawExportRequest(Long requestId, Long requesterId);

    /**
     * 删除导出请求
     * 
     * @param requestId 请求ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean deleteExportRequest(Long requestId, Long operatorId);

    /**
     * 查询导出请求统计信息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> getExportRequestStatistics(Date startTime, Date endTime);

    /**
     * 查询频繁申请导出的用户
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 用户统计列表
     */
    List<Map<String, Object>> selectFrequentRequesters(Date startTime, Date endTime, int limit);

    /**
     * 清理过期的导出请求
     * 
     * @param days 过期天数
     * @return 清理数量
     */
    int cleanExpiredRequests(int days);

    /**
     * 生成导出文件下载URL
     * 
     * @param requestId 请求ID
     * @param userId 用户ID
     * @return 下载URL
     */
    String generateDownloadUrl(Long requestId, Long userId);

    /**
     * 检查用户是否有权限下载导出文件
     * 
     * @param requestId 请求ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasDownloadPermission(Long requestId, Long userId);

    /**
     * 删除用户的所有导出请求
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteUserAllExportRequests(Long userId);

    /**
     * 检查用户是否可以提交新的导出请求
     * 
     * @param userId 用户ID
     * @param exportType 导出类型
     * @return 是否可以提交
     */
    boolean canSubmitNewRequest(Long userId, String exportType);

    /**
     * 获取导出请求的审批历史
     * 
     * @param requestId 请求ID
     * @return 审批历史列表
     */
    List<Map<String, Object>> getApprovalHistory(Long requestId);
}