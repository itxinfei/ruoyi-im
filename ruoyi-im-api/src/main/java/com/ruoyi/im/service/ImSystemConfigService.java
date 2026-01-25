package com.ruoyi.im.service;

import java.util.Map;

/**
 * 系统配置服务接口
 * 提供系统级别的配置管理，如消息撤回时间限制等
 *
 * @author ruoyi
 */
public interface ImSystemConfigService {

    /**
     * 获取消息撤回时间限制（分钟）
     *
     * @return 撤回时间限制（分钟）
     */
    Integer getMessageRecallTimeLimit();

    /**
     * 设置消息撤回时间限制（分钟）
     *
     * @param minutes 时间限制（分钟）
     */
    void setMessageRecallTimeLimit(Integer minutes);

    /**
     * 获取所有系统配置
     *
     * @return 配置Map
     */
    Map<String, Object> getAllSystemConfigs();

    /**
     * 更新系统配置
     *
     * @param configKey   配置键
     * @param configValue 配置值
     */
    void updateSystemConfig(String configKey, Object configValue);
}
