package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImApplication;
import java.util.List;
import java.util.Map;

/**
 * IM应用Service接口（Admin模块专用）
 */
public interface ImApplicationService {

    /**
     * 查询应用列表
     */
    List<ImApplication> selectImApplicationList(ImApplication imApplication);

    /**
     * 根据ID获取应用
     */
    ImApplication selectImApplicationById(Long id);

    /**
     * 新增应用
     */
    int insertImApplication(ImApplication imApplication);

    /**
     * 修改应用
     */
    int updateImApplication(ImApplication imApplication);

    /**
     * 删除应用
     */
    int deleteImApplicationByIds(Long[] ids);

    List<ImApplication> getApplications(String category);

    Map<String, List<ImApplication>> getApplicationsByCategory();

    Long createApplication(String name, String code, String category, String appType, String appUrl, String icon);

    void updateApplication(Long id, String name, String description, String icon);

    void deleteApplication(Long id);

    void setVisibility(Long id, Boolean isVisible);

    Map<String, Object> getApplicationStatistics();
}
