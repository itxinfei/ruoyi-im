package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImApplication;

import java.util.List;
import java.util.Map;

/**
 * 应用服务接口
 *
 * @author ruoyi
 */
public interface ImApplicationService {

    /**
     * 获取应用列表
     *
     * @param category 分类（可选）
     * @return 应用列表
     */
    List<ImApplication> getApplications(String category);

    /**
     * 获取可见应用列表
     *
     * @return 应用列表
     */
    List<ImApplication> getVisibleApplications();

    /**
     * 按分类获取应用
     *
     * @return 分类应用映射
     */
    Map<String, List<ImApplication>> getApplicationsByCategory();

    /**
     * 获取应用详情
     *
     * @param appId 应用ID
     * @return 应用信息
     */
    ImApplication getApplicationById(Long appId);

    /**
     * 创建应用
     *
     * @param name 名称
     * @param code 编码
     * @param category 分类
     * @param appType 类型
     * @param appUrl 地址
     * @param icon 图标
     * @return 应用ID
     */
    Long createApplication(String name, String code, String category, String appType, String appUrl, String icon);

    /**
     * 更新应用
     *
     * @param appId 应用ID
     * @param name 名称
     * @param description 描述
     * @param icon 图标
     */
    void updateApplication(Long appId, String name, String description, String icon);

    /**
     * 删除应用
     *
     * @param appId 应用ID
     */
    void deleteApplication(Long appId);

    /**
     * 设置应用可见性
     *
     * @param appId 应用ID
     * @param isVisible 是否可见
     */
    void setVisibility(Long appId, Boolean isVisible);
}
