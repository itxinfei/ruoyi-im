package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImApproval;
import java.util.List;
import java.util.Map;

/**
 * IM审批Service接口（Admin模块专用）
 */
public interface ImApprovalService {
    
    Map<String, Object> getApprovalDetail(Long id);
    
    List<ImApproval> selectApprovalListForAdmin(String status, Long userId, String title);
    
    List<ImApproval> getMyApprovals(Long userId);
    
    List<?> getTemplates();
    
    List<?> getActiveTemplates();
    
    void updateTemplateStatus(Long id, Boolean isActive);
    
    void processApproval(Long id, String action, String comment, Long userId);
    
    void cancelApproval(Long id, Long userId);
    
    Map<String, Object> getApprovalStatistics(String startTime, String endTime);
}
