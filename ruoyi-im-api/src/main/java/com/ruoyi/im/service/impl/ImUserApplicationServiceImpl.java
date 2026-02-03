package com.ruoyi.im.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constant.ErrorCode;
import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.domain.ImUserApplication;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImApplicationMapper;
import com.ruoyi.im.mapper.ImUserApplicationMapper;
import com.ruoyi.im.service.ImUserApplicationService;
import com.ruoyi.im.dto.app.ImAppInstallRequest;
import com.ruoyi.im.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户应用服务实现类
 *
 * @author ruoyi
 */
@Service
public class ImUserApplicationServiceImpl implements ImUserApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ImUserApplicationServiceImpl.class);

    @Resource
    private ImUserApplicationMapper userApplicationMapper;

    @Resource
    private ImApplicationMapper applicationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long installApplication(Long userId, ImAppInstallRequest request) {
        // 验证应用是否存在
        ImApplication app = applicationMapper.selectById(request.getAppId());
        if (app == null) {
            throw BusinessException.paramError("应用不存在");
        }

        // 检查是否已安装
        ImUserApplication existing = userApplicationMapper.selectUserApp(userId, request.getAppId());
        if (existing != null) {
            // 如果已存在但被禁用，则重新启用
            if (existing.getIsEnabled() == null || existing.getIsEnabled() != 1) {
                existing.setIsEnabled(1);
                userApplicationMapper.updateById(existing);
                log.info("重新启用应用: userId={}, appId={}", userId, request.getAppId());
                return existing.getId();
            }
            throw BusinessException.dataAlreadyExists();
        }

        // 创建安装记录
        ImUserApplication userApp = new ImUserApplication();
        userApp.setUserId(userId);
        userApp.setAppId(request.getAppId());
        userApp.setIsPinned(Boolean.TRUE.equals(request.getPinned()) ? 1 : 0);
        userApp.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        userApp.setUseCount(0);
        userApp.setIsEnabled(1);

        // 保存应用配置
        if (request.getConfig() != null && !request.getConfig().isEmpty()) {
            userApp.setAppConfig(JSON.toJSONString(request.getConfig()));
        }

        userApplicationMapper.insert(userApp);
        log.info("安装应用成功: userId={}, appId={}", userId, request.getAppId());

        return userApp.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uninstallApplication(Long userId, Long appId) {
        ImUserApplication userApp = userApplicationMapper.selectUserApp(userId, appId);
        if (userApp == null) {
            throw BusinessException.dataNotFound();
        }

        // 检查是否为系统应用，系统应用不允许卸载
        ImApplication app = applicationMapper.selectById(appId);
        if (app != null && app.getIsSystem() != null && app.getIsSystem() == 1) {
            throw new BusinessException(ErrorCode.APP_NOT_EXIST, "系统应用不允许卸载");
        }

        // 软删除：设置为禁用状态
        userApp.setIsEnabled(0);
        userApplicationMapper.updateById(userApp);

        log.info("卸载应用成功: userId={}, appId={}", userId, appId);
    }

    @Override
    public List<ImUserApplication> getUserApplications(Long userId) {
        return userApplicationMapper.selectUserApplications(userId);
    }

    @Override
    public boolean isInstalled(Long userId, Long appId) {
        ImUserApplication userApp = userApplicationMapper.selectUserApp(userId, appId);
        return userApp != null && userApp.getIsEnabled() != null && userApp.getIsEnabled() == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAppConfig(Long userId, Long appId, Map<String, Object> config) {
        ImUserApplication userApp = userApplicationMapper.selectUserApp(userId, appId);
        if (userApp == null) {
            throw BusinessException.dataNotFound();
        }

        userApp.setAppConfig(JSON.toJSONString(config));
        userApplicationMapper.updateById(userApp);

        log.info("更新应用配置成功: userId={}, appId={}", userId, appId);
    }

    @Override
    public Map<String, Object> getAppConfig(Long userId, Long appId) {
        ImUserApplication userApp = userApplicationMapper.selectUserApp(userId, appId);
        if (userApp == null) {
            throw BusinessException.dataNotFound();
        }

        String configJson = userApp.getAppConfig();
        if (StringUtil.isNullOrEmpty(configJson)) {
            return new HashMap<>();
        }

        return JSON.parseObject(configJson);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPinned(Long userId, Long appId, Boolean pinned) {
        ImUserApplication userApp = userApplicationMapper.selectUserApp(userId, appId);
        if (userApp == null) {
            throw BusinessException.dataNotFound();
        }

        userApp.setIsPinned(Boolean.TRUE.equals(pinned) ? 1 : 0);
        userApplicationMapper.updateById(userApp);

        log.info("设置应用固定状态: userId={}, appId={}, pinned={}", userId, appId, pinned);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSortOrder(Long userId, Map<Long, Integer> appOrders) {
        for (Map.Entry<Long, Integer> entry : appOrders.entrySet()) {
            Long appId = entry.getKey();
            Integer sortOrder = entry.getValue();

            ImUserApplication userApp = userApplicationMapper.selectUserApp(userId, appId);
            if (userApp != null) {
                userApp.setSortOrder(sortOrder);
                userApplicationMapper.updateById(userApp);
            }
        }

        log.info("更新应用排序成功: userId={}, count={}", userId, appOrders.size());
    }

    @Override
    public void recordUsage(Long userId, Long appId) {
        userApplicationMapper.updateLastUsedTime(userId, appId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setEnabled(Long userId, Long appId, Boolean enabled) {
        ImUserApplication userApp = userApplicationMapper.selectUserApp(userId, appId);
        if (userApp == null) {
            throw BusinessException.dataNotFound();
        }

        userApp.setIsEnabled(Boolean.TRUE.equals(enabled) ? 1 : 0);
        userApplicationMapper.updateById(userApp);

        log.info("设置应用启用状态: userId={}, appId={}, enabled={}", userId, appId, enabled);
    }

    @Override
    public Map<String, Object> getAppStatistics(Long appId) {
        int installCount = userApplicationMapper.countByAppId(appId);

        // 获取最近7天的使用统计
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LambdaQueryWrapper<ImUserApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImUserApplication::getAppId, appId)
                .ge(ImUserApplication::getLastUsedTime, sevenDaysAgo);
        long activeCount = userApplicationMapper.selectCount(wrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("installCount", installCount);
        stats.put("activeCount", activeCount);
        stats.put("appId", appId);

        return stats;
    }
}
