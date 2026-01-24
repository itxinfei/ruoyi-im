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
        if (imApplication.getName() == null || imApplication.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("应用名称不能为空");
        }
        if (imApplication.getCode() == null || imApplication.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("应用编码不能为空");
        }
        if (imApplication.getCategory() == null) {
            imApplication.setCategory("CUSTOM");
        }
        if (imApplication.getAppType() == null) {
            imApplication.setAppType("ROUTE");
        }
        if (imApplication.getIsVisible() == null) {
            imApplication.setIsVisible(true);
        }
        if (imApplication.getIsSystem() == null) {
            imApplication.setIsSystem(false);
        }
        if (imApplication.getSortOrder() == null) {
            imApplication.setSortOrder(0);
        }
        return applicationMapper.insertImApplication(imApplication);
    }

    @Override
    public int updateImApplication(ImApplication imApplication) {
        if (imApplication.getId() == null) {
            throw new IllegalArgumentException("应用ID不能为空");
        }
        ImApplication existingApp = applicationMapper.selectImApplicationById(imApplication.getId());
        if (existingApp == null) {
            throw new IllegalArgumentException("应用不存在");
        }
        if (imApplication.getName() == null || imApplication.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("应用名称不能为空");
        }
        if (imApplication.getCode() == null || imApplication.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("应用编码不能为空");
        }
        return applicationMapper.updateImApplication(imApplication);
    }

    @Override
    public int deleteImApplicationByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException("删除ID不能为空");
        }
        for (Long id : ids) {
            ImApplication app = applicationMapper.selectImApplicationById(id);
            if (app == null) {
                throw new IllegalArgumentException("应用不存在，ID：" + id);
            }
            if (app.getIsSystem() != null && app.getIsSystem()) {
                throw new IllegalArgumentException("系统应用不允许删除，应用名称：" + app.getName());
            }
        }
        return applicationMapper.deleteImApplicationByIds(ids);
    }

    @Override
    public List<ImApplication> getApplications(String category) {
        return applicationMapper.getApplications(category);
    }

    @Override
    public Map<String, List<ImApplication>> getApplicationsByCategory() {
        List<ImApplication> allApps = applicationMapper.getApplicationsByCategory();
        Map<String, List<ImApplication>> result = new java.util.HashMap<>();
        for (ImApplication app : allApps) {
            String category = app.getCategory();
            if (category == null) {
                category = "CUSTOM";
            }
            result.computeIfAbsent(category, k -> new java.util.ArrayList<>()).add(app);
        }
        return result;
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
