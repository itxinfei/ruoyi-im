package com.ruoyi.im.service;

import com.ruoyi.im.dto.message.ImMessageSendRequest;

/**
 * 消息重试服务接口
 * <p>
 * 处理消息发送失败后的自动重试逻辑
 * 支持指数退避策略，避免服务端压力过大
 * </p>
 *
 * @author ruoyi
 */
public interface ImMessageRetryService {

    /**
     * 最大重试次数
     */
    int MAX_RETRY_COUNT = 3;

    /**
     * 基础重试延迟（毫秒）
     */
    long BASE_RETRY_DELAY = 1000;

    /**
     * 最大重试延迟（毫秒）
     */
    long MAX_RETRY_DELAY = 10000;

    /**
     * 记录消息发送失败
     *
     * @param clientMsgId 客户端消息ID
     * @param request     发送请求
     * @param userId      用户ID
     * @param errorMessage 错误信息
     */
    void recordSendFailure(String clientMsgId, ImMessageSendRequest request, Long userId, String errorMessage);

    /**
     * 尝试重试发送消息
     *
     * @param clientMsgId 客户端消息ID
     * @return 是否重试成功（true表示成功或已达到最大重试次数，false表示需要稍后重试）
     */
    boolean retrySend(String clientMsgId);

    /**
     * 获取消息重试次数
     *
     * @param clientMsgId 客户端消息ID
     * @return 重试次数
     */
    int getRetryCount(String clientMsgId);

    /**
     * 清除失败记录（消息发送成功后调用）
     *
     * @param clientMsgId 客户端消息ID
     */
    void clearFailureRecord(String clientMsgId);

    /**
     * 计算下次重试延迟时间（指数退避）
     *
     * @param retryCount 当前重试次数
     * @return 延迟时间（毫秒）
     */
    default long calculateRetryDelay(int retryCount) {
        long delay = (long) (BASE_RETRY_DELAY * Math.pow(2, retryCount));
        return Math.min(delay, MAX_RETRY_DELAY);
    }

    /**
     * 检查是否可以重试
     *
     * @param clientMsgId 客户端消息ID
     * @return 是否可以重试
     */
    boolean canRetry(String clientMsgId);

    /**
     * 延迟重试发送消息（异步）
     *
     * @param clientMsgId 客户端消息ID
     */
    void retrySendWithDelay(String clientMsgId);
}
