package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImApplicationMapper;
import com.ruoyi.im.service.ImApplicationService;
import com.ruoyi.im.util.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用服务实现
 *
 * @author ruoyi
 */
@Service
public class ImApplicationServiceImpl implements ImApplicationService {

    private final ImApplicationMapper applicationMapper;

    /**
     * 构造器注入依赖
     *
     * @param applicationMapper 应用Mapper
     */
    public ImApplicationServiceImpl(ImApplicationMapper applicationMapper) {
        this.applicationMapper = applicationMapper;
    }

    @Override
    public List<ImApplication> getApplications(String category) {
        ImApplication query = new ImApplication();
        if (category != null && !category.isEmpty()) {
            query.setCategory(category);
        }
        return applicationMapper.selectImApplicationList(query);
    }

    @Override
    public List<ImApplication> getVisibleApplications() {
        return applicationMapper.selectVisibleApplications();
    }

    @Override
    public Map<String, List<ImApplication>> getApplicationsByCategory() {
        List<ImApplication> allApps = getVisibleApplications();
        return allApps.stream().collect(Collectors.groupingBy(ImApplication::getCategory));
    }

    @Override
    public ImApplication getApplicationById(Long appId) {
        ImApplication app = applicationMapper.selectImApplicationById(appId);
        return ValidationUtils.checkExists(app, "应用");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApplication(String name, String code, String category, String appType, String appUrl, String icon) {
        // 检查编码是否已存在
        ImApplication existApp = applicationMapper.selectImApplicationByCode(code);
        if (existApp != null) {
            throw new BusinessException("应用编码已存在");
        }

        ImApplication app = new ImApplication();
        app.setName(name);
        app.setCode(code);
        app.setCategory(category);
        app.setAppType(appType);
        app.setAppUrl(appUrl);
        app.setIcon(icon);
        app.setIsVisible(1);
        app.setIsSystem(0);
        app.setSortOrder(0);
        app.setCreateTime(LocalDateTime.now());
        applicationMapper.insertImApplication(app);
        return app.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApplication(Long appId, String name, String description, String icon) {
        ImApplication app = applicationMapper.selectImApplicationById(appId);
        ValidationUtils.checkExists(app, "应用");
        app.setName(name);
        app.setDescription(description);
        app.setIcon(icon);
        applicationMapper.updateImApplication(app);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApplication(Long appId) {
        ImApplication app = applicationMapper.selectImApplicationById(appId);
        ValidationUtils.checkExists(app, "应用");
        if (app.getIsSystem() == 1) {
            throw new BusinessException("系统应用不能删除");
        }
        applicationMapper.deleteImApplicationById(appId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setVisibility(Long appId, Boolean isVisible) {
        ImApplication app = applicationMapper.selectImApplicationById(appId);
        ValidationUtils.checkExists(app, "应用");
        app.setIsVisible(isVisible ? 1 : 0);
        applicationMapper.updateImApplication(app);
    }
}
