package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImApplicationMapper;
import com.ruoyi.im.service.ImApplicationService;
import com.ruoyi.im.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ImApplicationServiceImpl.class);

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
        log.info("创建应用: name={}, code={}, category={}", name, code, category);
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
        log.info("应用创建成功: appId={}, code={}", app.getId(), code);
        return app.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApplication(Long appId, String name, String description, String icon) {
        log.info("更新应用: appId={}, name={}", appId, name);
        ImApplication app = applicationMapper.selectImApplicationById(appId);
        ValidationUtils.checkExists(app, "应用");
        app.setName(name);
        app.setDescription(description);
        app.setIcon(icon);
        applicationMapper.updateImApplication(app);
        log.info("应用更新成功: appId={}", appId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApplication(Long appId) {
        log.info("删除应用: appId={}", appId);
        ImApplication app = applicationMapper.selectImApplicationById(appId);
        ValidationUtils.checkExists(app, "应用");
        if (app.getIsSystem() == 1) {
            throw new BusinessException("系统应用不能删除");
        }
        applicationMapper.deleteImApplicationById(appId);
        log.info("应用删除成功: appId={}, code={}", appId, app.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setVisibility(Long appId, Boolean isVisible) {
        log.info("设置应用可见性: appId={}, isVisible={}", appId, isVisible);
        ImApplication app = applicationMapper.selectImApplicationById(appId);
        ValidationUtils.checkExists(app, "应用");
        app.setIsVisible(isVisible ? 1 : 0);
        applicationMapper.updateImApplication(app);
        log.info("应用可见性设置成功: appId={}, isVisible={}", appId, isVisible);
    }
}
