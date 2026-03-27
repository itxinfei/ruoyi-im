package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImAppUsage;
import com.ruoyi.im.domain.ImApplication;
import com.ruoyi.im.mapper.ImAppUsageMapper;
import com.ruoyi.im.mapper.ImApplicationMapper;
import com.ruoyi.im.service.ImAppUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用使用记录服务实现
 *
 * @author ruoyi
 */
@Service
public class ImAppUsageServiceImpl implements ImAppUsageService {

    @Autowired
    private ImAppUsageMapper appUsageMapper;

    @Autowired
    private ImApplicationMapper applicationMapper;

    /** 保留的最大记录数 */
    private static final int MAX_KEEP_RECORDS = 100;

    @Override
    public void recordAppUsage(Long userId, Long appId) {
        if (userId == null || appId == null) {
            return;
        }

        // 删除该用户对该应用的历史记录
        appUsageMapper.deleteByUserIdAndAppId(userId, appId);

        // 新增新的使用记录
        ImAppUsage appUsage = new ImAppUsage();
        appUsage.setUserId(userId);
        appUsage.setAppId(appId);
        appUsage.setUsageTime(LocalDateTime.now());
        appUsageMapper.insertImAppUsage(appUsage);

        // 清理旧记录，保持记录数在限制内
        appUsageMapper.deleteOldRecordsByUserId(userId, MAX_KEEP_RECORDS);
    }

    @Override
    public List<ImApplication> getRecentApps(Long userId, int limit) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Long> recentAppIds = appUsageMapper.selectRecentAppIdsByUserId(userId, limit);
        if (recentAppIds == null || recentAppIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 按返回的顺序获取应用详情
        return recentAppIds.stream()
                .map(applicationMapper::selectImApplicationById)
                .filter(app -> app != null && app.getIsVisible() == 1)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImApplication> getRecentApps(Long userId) {
        return getRecentApps(userId, 10);
    }
}
