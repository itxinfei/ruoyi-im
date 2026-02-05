package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUserSyncPoint;
import com.ruoyi.im.vo.message.ImMessageVO;
import com.ruoyi.im.vo.message.SyncMessageResponse;

import java.util.List;

/**
 * 消息同步服务接口
 *
 * 参考野火IM的同步机制，实现消息同步功能。
 * 支持断线重连后的消息补发、多端同步等场景。
 *
 * @author ruoyi
 */
public interface ImMessageSyncService {

    /**
     * 获取用户的同步点
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 同步点实体（不存在则返回null）
     */
    ImUserSyncPoint getSyncPoint(Long userId, String deviceId);

    /**
     * 初始化或获取用户设备的同步点
     * 如果不存在则创建，存在则返回
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 同步点实体
     */
    ImUserSyncPoint initSyncPoint(Long userId, String deviceId);

    /**
     * 同步消息
     * 获取从lastSyncTime之后的新消息
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param lastSyncTime 上次同步时间戳（毫秒）
     * @param limit 单次同步最大条数
     * @return 同步响应（包含消息列表和新的同步点）
     */
    SyncMessageResponse syncMessages(Long userId, String deviceId, Long lastSyncTime, Integer limit);

    /**
     * 更新同步点
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param lastSyncTime 最后同步时间
     * @param lastMessageId 最后消息ID
     */
    void updateSyncPoint(Long userId, String deviceId, Long lastSyncTime, Long lastMessageId);

    /**
     * 获取用户的所有设备同步点
     *
     * @param userId 用户ID
     * @return 同步点列表
     */
    List<ImUserSyncPoint> getUserSyncPoints(Long userId);

    /**
     * 删除设备的同步点
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    void deleteSyncPoint(Long userId, String deviceId);

    /**
     * 获取用户的最大同步时间
     * 用于确定该用户所有设备中最早的同步点
     *
     * @param userId 用户ID
     * @return 最小同步时间
     */
    Long getMinSyncTime(Long userId);
}
