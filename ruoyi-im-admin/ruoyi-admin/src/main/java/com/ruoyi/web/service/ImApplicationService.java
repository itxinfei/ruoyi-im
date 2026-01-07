package com.ruoyi.web.service;

import com.ruoyi.im.domain.ImApplication;
import java.util.List;
import java.util.Map;

/**
 * IM应用Service接口（Admin模块专用）
 */
public interface ImApplicationService {
    
    List<ImApplication> getApplications(String category);
    
    Map<String, List<ImApplication>> getApplicationsByCategory();
    
    ImApplication getApplicationById(Long id);
    
    Long createApplication(String name, String code, String category, String appType, String appUrl, String icon);
    
    void updateApplication(Long id, String name, String description, String icon);
    
    void deleteApplication(Long id);
    
    void setVisibility(Long id, Boolean isVisible);
    
    Map<String, Object> getApplicationStatistics();
}
