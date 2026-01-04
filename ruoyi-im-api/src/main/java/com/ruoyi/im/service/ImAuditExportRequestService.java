package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImAuditExportRequest;
import java.util.List;

/**
 * 审计导出请求Service接口
 * 
 * @author ruoyi
 */
public interface ImAuditExportRequestService {
    /**
     * 查询审计导出请求
     * 
     * @param id 审计导出请求ID
     * @return 审计导出请求
     */
    public ImAuditExportRequest selectImAuditExportRequestById(Long id);

    /**
     * 查询审计导出请求列表
     * 
     * @param imAuditExportRequest 审计导出请求
     * @return 审计导出请求集合
     */
    public List<ImAuditExportRequest> selectImAuditExportRequestList(ImAuditExportRequest imAuditExportRequest);

    /**
     * 新增审计导出请求
     * 
     * @param imAuditExportRequest 审计导出请求
     * @return 结果
     */
    public int insertImAuditExportRequest(ImAuditExportRequest imAuditExportRequest);

    /**
     * 修改审计导出请求
     * 
     * @param imAuditExportRequest 审计导出请求
     * @return 结果
     */
    public int updateImAuditExportRequest(ImAuditExportRequest imAuditExportRequest);

    /**
     * 批量删除审计导出请求
     * 
     * @param ids 需要删除的审计导出请求ID
     * @return 结果
     */
    public int deleteImAuditExportRequestByIds(Long[] ids);

    /**
     * 删除审计导出请求信息
     * 
     * @param id 审计导出请求ID
     * @return 结果
     */
    public int deleteImAuditExportRequestById(Long id);
    
    /**
     * 根据用户ID查询审计导出请求列表
     * 
     * @param userId 用户ID
     * @return 审计导出请求集合
     */
    public List<ImAuditExportRequest> selectImAuditExportRequestByUserId(Long userId);
    
    /**
     * 根据状态查询审计导出请求列表
     * 
     * @param status 状态
     * @return 审计导出请求集合
     */
    public List<ImAuditExportRequest> selectImAuditExportRequestByStatus(String status);
    
    /**
     * 根据用户ID和状态查询审计导出请求列表
     * 
     * @param userId 用户ID
     * @ @param status 状态
     * @return 审计导出请求集合
     */
    public List<ImAuditExportRequest> selectImAuditExportRequestByUserIdAndStatus(Long userId, String status);
    
    /**
     * 创建导出请求
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param operationType 操作类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param format 格式
     * @return 结果
     */
    public int createExportRequest(Long userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime, String operationType, String targetType, Long targetId, String format);
    
    /**
     * 更新导出请求状态
     * 
     * @param id 审计导出请求ID
     * @param status 状态
     * @param filePath 文件路径
     * @param errorMessage 错误信息
     * @return 结果
     */
    public int updateExportStatus(Long id, String status, String filePath, String errorMessage);
    
    /**
     * 删除指定时间之前的审计导出请求
     * 
     * @param beforeTime 时间
     * @return 结果
     */
    public int deleteImAuditExportRequestByBeforeTime(java.time.LocalDateTime beforeTime);
    
    /**
     * 处理导出请求
     * 
     * @param id 审计导出请求ID
     * @return 结果
     */
    public int processExportRequest(Long id);
}
