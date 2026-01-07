package com.ruoyi.web.mapper;

import com.ruoyi.im.domain.ImApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM应用Mapper接口（Admin模块专用）
 */
@Mapper
public interface ImApplicationMapper {
    
    List<ImApplication> getApplications(@Param("category") String category);
    
    Map<String, List<ImApplication>> getApplicationsByCategory();
    
    ImApplication getApplicationById(Long id);
    
    int insertApplication(ImApplication app);
    
    void updateApplication(@Param("id") Long id, @Param("name") String name, @Param("description") String description, @Param("icon") String icon);
    
    void deleteApplication(Long id);
    
    void setVisibility(@Param("id") Long id, @Param("isVisible") Boolean isVisible);
    
    Map<String, Object> getApplicationStatistics();
}
