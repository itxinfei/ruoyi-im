package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUserApplication;
import com.ruoyi.im.dto.app.ImAppInstallRequest;

import java.util.List;
import java.util.Map;

/**
 * 用户应用服务接口
 *
 * @author ruoyi
 */
public interface ImUserApplicationService {

    /**
     * 安装应用
     *
     * @param userId 用户ID
     * @param request 安装请求
     * @return 安装记录ID
     */
    Long installApplication(Long userId, ImAppInstallRequest request);

    /**
     * 卸载应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     */
    void uninstallApplication(Long userId, Long appId);

    /**
     * 获取用户已安装的应用列表
     *
     * @param userId 用户ID
     * @return 应用列表
     */
    List<ImUserApplication> getUserApplications(Long userId);

    /**
     * 检查用户是否已安装应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 是否已安装
     */
    boolean isInstalled(Long userId, Long appId);

    /**
     * 更新应用配置
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @param config 配置项
     */
    void updateAppConfig(Long userId, Long appId, Map<String, Object> config);

    /**
     * 获取应用配置
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 配置项
     */
    Map<String, Object> getAppConfig(Long userId, Long appId);

    /**
     * 固定/取消固定应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @param pinned 是否固定
     */
    void setPinned(Long userId, Long appId, Boolean pinned);

    /**
     * 更新应用排序
     *
     * @param userId 用户ID
     * @param appOrders 应用排序映射 (appId -> sortOrder)
     */
    void updateSortOrder(Long userId, Map<Long, Integer> appOrders);

    /**
     * 记录应用使用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     */
    void recordUsage(Long userId, Long appId);

    /**
     * 启用/禁用应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @param enabled 是否启用
     */
    void setEnabled(Long userId, Long appId, Boolean enabled);

    /**
     * 获取应用安装统计
     *
     * @param appId 应用ID
     * @return 统计信息
     */
    Map<String, Object> getAppStatistics(Long appId);
}
