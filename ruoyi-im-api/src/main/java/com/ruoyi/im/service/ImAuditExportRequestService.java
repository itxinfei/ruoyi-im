package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImAuditExportRequest;
import java.util.List;

/**
 * 审计导出请求Service接口
 * 
 * @author ruoyi
 */
public interface ImAuditExportRequestService extends BaseService<ImAuditExportRequest> {
    
    @Override
    ImAuditExportRequest selectById(Long id);
    
    @Override
    List<ImAuditExportRequest> selectList(ImAuditExportRequest imAuditExportRequest);
    
    @Override
    int insert(ImAuditExportRequest imAuditExportRequest);
    
    @Override
    int update(ImAuditExportRequest imAuditExportRequest);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
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
