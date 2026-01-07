package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM审批Mapper接口（Admin模块专用）
 */
@Mapper
public interface ImApprovalMapper {
    
    Map<String, Object> getApprovalDetail(Long id);
    
    List<ImApproval> selectApprovalListForAdmin(@Param("status") String status, @Param("userId") Long userId, @Param("title") String title);
    
    List<ImApproval> getMyApprovals(@Param("userId") Long userId);
    
    List<?> getTemplates();
    
    List<?> getActiveTemplates();
    
    void updateTemplateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);
    
    void processApproval(@Param("id") Long id, @Param("action") String action, @Param("comment") String comment, @Param("userId") Long userId);
    
    void cancelApproval(@Param("id") Long id, @Param("userId") Long userId);
    
    Map<String, Object> getApprovalStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
