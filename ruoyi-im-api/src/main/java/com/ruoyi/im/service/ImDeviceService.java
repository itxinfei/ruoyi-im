package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.vo.device.DeviceVO;

import java.util.List;

/**
 * 设备管理服务接口
 *
 * 管理用户的多设备登录，支持设备在线状态查询
 *
 * @author ruoyi
 */
public interface ImDeviceService {

    /**
     * 注册或更新设备
     * 当设备连接时调用，记录设备信息并设置为在线状态
     *
     * @param userId     用户ID
     * @param deviceId   设备ID（客户端生成的UUID）
     * @param deviceType 设备类型（web/ios/android/pc/mac/mini）
     * @param deviceName 设备名称
     * @param clientVersion 客户端版本
     * @param osVersion 操作系统版本
     * @param ipAddress IP地址
     * @return 设备实体
     */
    ImUserDevice registerDevice(Long userId, String deviceId, String deviceType,
                               String deviceName, String clientVersion,
                               String osVersion, String ipAddress);

    /**
     * 更新设备心跳
     * 定期调用以保持设备在线状态
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     */
    void updateHeartbeat(Long userId, String deviceId);

    /**
     * 设置设备离线
     * 当WebSocket连接断开时调用
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     */
    void setDeviceOffline(Long userId, String deviceId);

    /**
     * 获取用户的所有在线设备
     *
     * @param userId 用户ID
     * @return 在线设备列表
     */
    List<ImUserDevice> getOnlineDevices(Long userId);

    /**
     * 获取用户的所有设备
     *
     * @param userId 用户ID
     * @return 设备列表
     */
    List<DeviceVO> getUserDevices(Long userId);

    /**
     * 获取设备信息
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     * @return 设备实体
     */
    ImUserDevice getDevice(Long userId, String deviceId);

    /**
     * 移除设备
     * 用户主动退出登录时调用
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     */
    void removeDevice(Long userId, String deviceId);

    /**
     * 检查用户是否有其他在线设备
     *
     * @param userId   用户ID
     * @param deviceId 排除的设备ID
     * @return 其他在线设备数量
     */
    int countOtherOnlineDevices(Long userId, String deviceId);

    /**
     * 将超时的设备设为离线
     * 定时任务调用
     *
     * @param timeoutMinutes 超时分钟数
     * @return 设为离线的设备数量
     */
    int markTimeoutDevicesOffline(int timeoutMinutes);
}
