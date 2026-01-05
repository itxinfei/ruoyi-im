package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImAuditExportRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计导出请求Service接口
 * 
 * @author ruoyi
 */
public interface ImAuditExportRequestService {
    
    /**
     * 根据ID查询审计导出请求
     * 
     * @param id 审计导出请求ID
     * @return 审计导出请求
     */
    ImAuditExportRequest selectById(Long id);
    
    /**
     * 根据ID查询审计导出请求（兼容旧方法名）
     * 
     * @param id 审计导出请求ID
     * @return 审计导出请求
     */
    default ImAuditExportRequest selectImAuditExportRequestById(Long id) {
        return selectById(id);
    }
    
    /**
     * 根据用户ID查询审计导出请求列表
     * 
     * @param userId 用户ID
     * @return 审计导出请求列表
     */
    default List<ImAuditExportRequest> selectImAuditExportRequestByUserId(Long userId) {
        // 创建查询条件
        ImAuditExportRequest query = new ImAuditExportRequest();
        query.setUserId(userId);
        return selectImAuditExportRequestList(query);
    }
    
    /**
     * 根据状态查询审计导出请求列表
     * 
     * @param status 状态
     * @return 审计导出请求列表
     */
    default List<ImAuditExportRequest> selectImAuditExportRequestByStatus(String status) {
        // 创建查询条件
        ImAuditExportRequest query = new ImAuditExportRequest();
        query.setExportStatus(status);
        return selectImAuditExportRequestList(query);
    }
    
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
    default int createExportRequest(Long userId, LocalDateTime startTime, LocalDateTime endTime, 
                                   String operationType, String targetType, Long targetId, String format) {
        // 创建审计导出请求对象
        ImAuditExportRequest request = new ImAuditExportRequest();
        request.setUserId(userId);
        request.setExportType(operationType); // 导出类型设置为操作类型
        request.setExportStatus("pending"); // 初始状态为待处理
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        
        // 设置导出参数
        StringBuilder params = new StringBuilder();
        params.append("startTime:").append(startTime)
              .append(",endTime:").append(endTime)
              .append(",targetType:").append(targetType)
              .append(",targetId:").append(targetId)
              .append(",format:").append(format);
        request.setExportParams(params.toString());
        
        return insertImAuditExportRequest(request);
    }
    
    /**
     * 更新导出请求状态
     * 
     * @param id 请求ID
     * @param status 状态
     * @param url URL
     * @param errorMsg 错误信息
     * @return 结果
     */
    default int updateExportStatus(Long id, String status, String url, String errorMsg) {
        ImAuditExportRequest request = selectById(id);
        if (request != null) {
            request.setExportStatus(status);
            request.setExportUrl(url);
            request.setUpdateTime(LocalDateTime.now());
            
            // 添加错误信息到导出参数中
            if (errorMsg != null) {
                String params = request.getExportParams();
                if (params != null) {
                    params += ",errorMsg:" + errorMsg;
                } else {
                    params = "errorMsg:" + errorMsg;
                }
                request.setExportParams(params);
            }
            
            return updateImAuditExportRequest(request);
        }
        return 0;
    }
    
    /**
     * 查询审计导出请求列表
     * 
     * @param imAuditExportRequest 查询条件
     * @return 审计导出请求集合
     */
    List<ImAuditExportRequest> selectImAuditExportRequestList(ImAuditExportRequest imAuditExportRequest);
    
    /**
     * 新增审计导出请求
     * 
     * @param imAuditExportRequest 审计导出请求
     * @return 结果
     */
    int insertImAuditExportRequest(ImAuditExportRequest imAuditExportRequest);
    
    /**
     * 新增审计导出请求（兼容方法）
     * 
     * @param imAuditExportRequest 审计导出请求
     * @return 结果
     */
    default int insert(ImAuditExportRequest imAuditExportRequest) {
        return insertImAuditExportRequest(imAuditExportRequest);
    }
    
    /**
     * 修改审计导出请求
     * 
     * @param imAuditExportRequest 审计导出请求
     * @return 结果
     */
    int updateImAuditExportRequest(ImAuditExportRequest imAuditExportRequest);
    
    /**
     * 修改审计导出请求（兼容方法）
     * 
     * @param imAuditExportRequest 审计导出请求
     * @return 结果
     */
    default int update(ImAuditExportRequest imAuditExportRequest) {
        return updateImAuditExportRequest(imAuditExportRequest);
    }
    
    /**
     * 批量删除审计导出请求
     * 
     * @param ids 需要删除的审计导出请求ID
     * @return 结果
     */
    int deleteImAuditExportRequestByIds(Long[] ids);
    
    /**
     * 删除审计导出请求信息
     * 
     * @param id 审计导出请求ID
     * @return 结果
     */
    int deleteImAuditExportRequestById(Long id);
    
    /**
     * 删除审计导出请求信息（兼容方法）
     * 
     * @param id 审计导出请求ID
     * @return 结果
     */
    default int deleteById(Long id) {
        return deleteImAuditExportRequestById(id);
    }
    
    /**
     * 批量删除审计导出请求（兼容方法）
     * 
     * @param ids 需要删除的审计导出请求ID
     * @return 结果
     */
    default int deleteByIds(Long[] ids) {
        return deleteImAuditExportRequestByIds(ids);
    }
}