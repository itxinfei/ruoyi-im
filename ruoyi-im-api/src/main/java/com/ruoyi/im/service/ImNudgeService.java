package com.ruoyi.im.service;

import com.ruoyi.im.dto.nudge.ImNudgeCreateRequest;
import com.ruoyi.im.vo.nudge.ImNudgeVO;

/**
 * 拍一拍服务接口
 *
 * @author ruoyi
 */
public interface ImNudgeService {

    /**
     * 执行拍一拍操作
     *
     * @param request 拍一拍请求
     * @param userId  当前用户ID
     * @return 拍一拍结果
     */
    ImNudgeVO nudge(ImNudgeCreateRequest request, Long userId);

    /**
     * 获取用户拍一拍配置
     *
     * @param userId 用户ID
     * @return 配置信息（不存在则返回默认配置）
     */
    com.ruoyi.im.domain.ImNudgeConfig getUserConfig(Long userId);

    /**
     * 更新用户拍一拍配置
     *
     * @param userId 用户ID
     * @param config 配置信息
     */
    void updateUserConfig(Long userId, com.ruoyi.im.domain.ImNudgeConfig config);
}
