package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImPushDevice;
import com.ruoyi.im.dto.push.PushDeviceRegisterRequest;

import java.util.List;

/**
 * 推送设备服务接口
 *
 * @author ruoyi
 */
public interface ImPushDeviceService {

    /**
     * 注册推送设备
     *
     * @param request 注册请求
     * @param userId 用户ID
     */
    void registerDevice(PushDeviceRegisterRequest request, Long userId);

    /**
     * 取消设备注册
     *
     * @param deviceToken 设备Token
     * @param userId 用户ID
     */
    void unregisterDevice(String deviceToken, Long userId);

    /**
     * 更新设备活跃时间
     *
     * @param deviceToken 设备Token
     */
    void updateLastActiveTime(String deviceToken);

    /**
     * 获取用户的推送设备列表
     *
     * @param userId 用户ID
     * @return 设备列表
     */
    List<ImPushDevice> getUserDevices(Long userId);

    /**
     * 检测用户是否离线
     * 如果离线则推送消息
     *
     * @param userId 用户ID
     * @param message 消息内容
     */
    void pushIfUserOffline(Long userId, ImMessage message);

    /**
     * 推送消息给指定用户的所有设备
     *
     * @param userId 用户ID
     * @param title 推送标题
     * @param content 推送内容
     * @param messageId 消息ID
     * @param conversationId 会话ID
     */
    void pushToUserDevices(Long userId, String title, String content, Long messageId, Long conversationId);
}
