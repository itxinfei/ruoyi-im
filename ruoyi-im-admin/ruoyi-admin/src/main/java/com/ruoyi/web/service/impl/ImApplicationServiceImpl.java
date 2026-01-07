package com.ruoyi.web.service.impl;

import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.web.mapper.ImApplicationMapper;
import com.ruoyi.web.service.ImApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * IM应用Service实现（Admin模块专用）
 */
@Service
public class ImApplicationServiceImpl implements ImApplicationService {

    @Autowired
    private ImApplicationMapper applicationMapper;

    @Override
    public List<ImApplication> getApplications(String category) {
        return applicationMapper.getApplications(category);
    }

    @Override
    public Map<String, List<ImApplication>> getApplicationsByCategory() {
        return applicationMapper.getApplicationsByCategory();
    }

    @Override
    public ImApplication getApplicationById(Long id) {
        return applicationMapper.getApplicationById(id);
    }

    @Override
    public Long createApplication(String name, String code, String category, String appType, String appUrl, String icon) {
        ImApplication app = new ImApplication();
        app.setName(name);
        app.setCode(code);
        app.setCategory(category);
        app.setAppType(appType);
        app.setAppUrl(appUrl);
        app.setIcon(icon);
        applicationMapper.insertApplication(app);
        return app.getId();
    }

    @Override
    public void updateApplication(Long id, String name, String description, String icon) {
        applicationMapper.updateApplication(id, name, description, icon);
    }

    @Override
    public void deleteApplication(Long id) {
        applicationMapper.deleteApplication(id);
    }

    @Override
    public void setVisibility(Long id, Boolean isVisible) {
        applicationMapper.setVisibility(id, isVisible);
    }

    @Override
    public Map<String, Object> getApplicationStatistics() {
        return applicationMapper.getApplicationStatistics();
    }
}
