package com.ruoyi.im.service;

/**
 * 用户布局配置服务接口
 *
 * @author ruoyi
 */
public interface ImUserLayoutService {

    /** 布局类型 - 工作台 */
    String LAYOUT_TYPE_WORKBENCH = "WORKBENCH";

    /**
     * 获取用户的布局配置
     *
     * @param userId 用户ID
     * @param layoutType 布局类型
     * @return 布局配置JSON，如果不存在返回null
     */
    String getLayoutConfig(Long userId, String layoutType);

    /**
     * 保存用户的布局配置
     *
     * @param userId 用户ID
     * @param layoutType 布局类型
     * @param layoutConfig 布局配置JSON
     */
    void saveLayoutConfig(Long userId, String layoutType, String layoutConfig);

    /**
     * 删除用户的布局配置
     *
     * @param userId 用户ID
     * @param layoutType 布局类型
     */
    void deleteLayoutConfig(Long userId, String layoutType);
}
