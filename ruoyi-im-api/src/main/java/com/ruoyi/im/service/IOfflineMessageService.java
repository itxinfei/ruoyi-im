package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessage;

/**
 * 离线消息服务接口
 * <p>
 * 处理用户离线时的消息存储和上线后的推送
 * 确保消息不丢失，提升系统可靠性
 * </p>
 *
 * @author ruoyi
 */
public interface IOfflineMessageService {

    /**
     * 存储离线消息
     * <p>
     * 当用户离线时，将发送给该用户的消息存储到Redis
     * 消息保留7天，过期自动清理
     * </p>
     *
     * @param userId  接收者用户ID
     * @param message 消息对象
     * @return 存储是否成功
     */
    boolean storeOfflineMessage(Long userId, ImMessage message);

    /**
     * 批量存储离线消息
     *
     * @param userIds  接收者用户ID列表
     * @param message  消息对象
     * @return 成功存储的数量
     */
    int batchStoreOfflineMessage(java.util.Collection<Long> userIds, ImMessage message);

    /**
     * 获取用户的离线消息数量
     *
     * @param userId 用户ID
     * @return 离线消息数量
     */
    long getOfflineMessageCount(Long userId);

    /**
     * 获取用户的离线消息列表
     *
     * @param userId 用户ID
     * @param limit  获取数量限制
     * @return 离线消息列表
     */
    java.util.List<ImMessage> getOfflineMessages(Long userId, int limit);

    /**
     * 清空用户的离线消息
     * <p>
     * 用户上线并接收离线消息后调用
     * </p>
     *
     * @param userId 用户ID
     * @return 清空的消息数量
     */
    long clearOfflineMessages(Long userId);

    /**
     * 推送并清空用户的离线消息
     * <p>
     * 用户上线时自动调用，通过WebSocket推送所有离线消息
     * </p>
     *
     * @param userId 用户ID
     * @return 推送的消息数量
     */
    int pushAndClearOfflineMessages(Long userId);

    /**
     * 检查并推送离线消息
     * <p>
     * 用户上线时检查是否有离线消息，有则推送
     * </p>
     *
     * @param userId 用户ID
     */
    void checkAndPushOfflineMessages(Long userId);
}
