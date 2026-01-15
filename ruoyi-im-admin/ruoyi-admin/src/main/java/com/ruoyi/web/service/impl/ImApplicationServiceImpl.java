package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImApplication;
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
    public List<ImApplication> selectImApplicationList(ImApplication imApplication) {
        return applicationMapper.selectImApplicationList(imApplication);
    }

    @Override
    public ImApplication selectImApplicationById(Long id) {
        return applicationMapper.selectImApplicationById(id);
    }

    @Override
    public int insertImApplication(ImApplication imApplication) {
        return applicationMapper.insertImApplication(imApplication);
    }

    @Override
    public int updateImApplication(ImApplication imApplication) {
        return applicationMapper.updateImApplication(imApplication);
    }

    @Override
    public int deleteImApplicationByIds(Long[] ids) {
        return applicationMapper.deleteImApplicationByIds(ids);
    }

    @Override
    public List<ImApplication> getApplications(String category) {
        return applicationMapper.getApplications(category);
    }

    @Override
    public Map<String, List<ImApplication>> getApplicationsByCategory() {
        return applicationMapper.getApplicationsByCategory();
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
        applicationMapper.insertImApplication(app);
        return app.getId();
    }

    @Override
    public void updateApplication(Long id, String name, String description, String icon) {
        ImApplication app = new ImApplication();
        app.setId(id);
        app.setName(name);
        app.setDescription(description);
        app.setIcon(icon);
        applicationMapper.updateImApplication(app);
    }

    @Override
    public void deleteApplication(Long id) {
        applicationMapper.deleteImApplicationByIds(new Long[]{id});
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
