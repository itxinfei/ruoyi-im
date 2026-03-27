package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImApplication;

import java.util.List;

/**
 * 应用使用记录服务接口
 *
 * @author ruoyi
 */
public interface ImAppUsageService {

    /**
     * 记录应用使用
     * 当用户访问某个应用时调用，更新使用时间
     *
     * @param userId 用户ID
     * @param appId 应用ID
     */
    void recordAppUsage(Long userId, Long appId);

    /**
     * 获取用户最近使用的应用列表
     *
     * @param userId 用户ID
     * @param limit 返回数量限制，默认10个
     * @return 最近使用的应用列表
     */
    List<ImApplication> getRecentApps(Long userId, int limit);

    /**
     * 获取用户最近使用的应用列表（默认10个）
     *
     * @param userId 用户ID
     * @return 最近使用的应用列表
     */
    List<ImApplication> getRecentApps(Long userId);
}
