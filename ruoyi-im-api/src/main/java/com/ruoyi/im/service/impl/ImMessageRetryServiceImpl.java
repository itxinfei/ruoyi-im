package com.ruoyi.im.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.dto.message.ImMessageSendRequest;
import com.ruoyi.im.service.ImMessageRetryService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.util.ExceptionHandlerUtil;
import com.ruoyi.im.util.ImRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.concurrent.*;

/**
 * 消息重试服务实现
 * <p>
 * 使用Redis存储失败消息和重试次数
 * 支持指数退避策略，避免服务端压力过大
 * </p>
 *
 * @author ruoyi
 */
@Service
public class ImMessageRetryServiceImpl implements ImMessageRetryService {

    private static final Logger log = LoggerFactory.getLogger(ImMessageRetryServiceImpl.class);

    private static final String RETRY_KEY_PREFIX = "message:retry:";
    private static final String RETRY_COUNT_PREFIX = "message:retry:count:";
    private static final String RETRY_DATA_PREFIX = "message:retry:data:";
    private static final long RETRY_EXPIRE_HOURS = 24;

    private final ImRedisUtil redisUtil;
    private final ImMessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(2,
            r -> {
                Thread t = new Thread(r, "message-retry-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            });

    public ImMessageRetryServiceImpl(ImRedisUtil redisUtil,
                                     @Lazy ImMessageService messageService) {
        this.redisUtil = redisUtil;
        this.messageService = messageService;
    }

    /**
     * 应用关闭时释放线程池资源
     */
    @javax.annotation.PreDestroy
    public void shutdown() {
        retryExecutor.shutdown();
        try {
            if (!retryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                retryExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            retryExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void recordSendFailure(String clientMsgId, ImMessageSendRequest request, Long userId, String errorMessage) {
        if (clientMsgId == null || clientMsgId.isEmpty()) {
            return;
        }

        try {
            // 增加重试计数
            String countKey = RETRY_COUNT_PREFIX + clientMsgId;
            Object countObj = redisUtil.get(countKey);
            int currentCount = countObj != null ? Integer.parseInt(countObj.toString()) : 0;

            if (currentCount < MAX_RETRY_COUNT) {
                redisUtil.set(countKey, String.valueOf(currentCount + 1), RETRY_EXPIRE_HOURS, TimeUnit.HOURS);

                // 存储失败消息数据
                String dataKey = RETRY_DATA_PREFIX + clientMsgId;
                FailureData data = new FailureData();
                data.setClientMsgId(clientMsgId);
                data.setRequest(request);
                data.setUserId(userId);
                data.setErrorMessage(errorMessage);
                data.setFirstFailTime(System.currentTimeMillis());

                String jsonData = objectMapper.writeValueAsString(data);
                redisUtil.set(dataKey, jsonData, RETRY_EXPIRE_HOURS, TimeUnit.HOURS);

                // 记录到重试队列
                String retryKey = RETRY_KEY_PREFIX + clientMsgId;
                redisUtil.set(retryKey, "1", RETRY_EXPIRE_HOURS, TimeUnit.HOURS);

                log.warn("记录消息发送失败: clientMsgId={}, retryCount={}, error={}",
                        clientMsgId, currentCount + 1, errorMessage);
            } else {
                log.error("消息已达最大重试次数: clientMsgId={}, maxRetry={}", clientMsgId, MAX_RETRY_COUNT);
            }
        } catch (Exception e) {
            ExceptionHandlerUtil.logError(log, "记录消息发送失败异常: clientMsgId={}", e, clientMsgId);
        }
    }

    @Override
    public boolean retrySend(String clientMsgId) {
        if (clientMsgId == null || clientMsgId.isEmpty()) {
            return false;
        }

        try {
            // 检查重试次数
            int retryCount = getRetryCount(clientMsgId);
            if (retryCount >= MAX_RETRY_COUNT) {
                log.warn("消息已达最大重试次数，不再重试: clientMsgId={}", clientMsgId);
                clearFailureRecord(clientMsgId);
                return true;
            }

            // 获取失败消息数据
            String dataKey = RETRY_DATA_PREFIX + clientMsgId;
            Object jsonDataObj = redisUtil.get(dataKey);
            if (jsonDataObj == null) {
                log.warn("未找到失败消息数据: clientMsgId={}", clientMsgId);
                return true;
            }

            String jsonData = jsonDataObj.toString();
            FailureData data = objectMapper.readValue(jsonData, FailureData.class);
            if (data == null || data.getRequest() == null) {
                log.error("解析失败消息数据异常: clientMsgId={}", clientMsgId);
                clearFailureRecord(clientMsgId);
                return true;
            }

            // 尝试重新发送
            log.info("开始重试发送消息: clientMsgId={}, retryCount={}", clientMsgId, retryCount + 1);

            com.ruoyi.im.vo.message.ImMessageVO vo = messageService.sendMessage(data.getRequest(), data.getUserId());
            Long messageId = vo != null ? vo.getId() : null;

            if (messageId != null) {
                log.info("消息重试发送成功: clientMsgId={}, messageId={}", clientMsgId, messageId);
                clearFailureRecord(clientMsgId);
                return true;
            } else {
                log.warn("消息重试发送失败，将在下次重试: clientMsgId={}", clientMsgId);
                return false;
            }

        } catch (Exception e) {
            ExceptionHandlerUtil.logError(log, "消息重试发送异常: clientMsgId={}", e, clientMsgId);
            return false;
        }
    }

    @Override
    public void retrySendWithDelay(String clientMsgId) {
        int retryCount = getRetryCount(clientMsgId);
        long delay = calculateRetryDelay(retryCount);

        retryExecutor.schedule(() -> {
            try {
                boolean success = retrySend(clientMsgId);
                if (!success) {
                    // 继续重试
                    if (canRetry(clientMsgId)) {
                        retrySendWithDelay(clientMsgId);
                    }
                }
            } catch (Exception e) {
                ExceptionHandlerUtil.logError(log, "延迟重试消息异常: clientMsgId={}", e, clientMsgId);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int getRetryCount(String clientMsgId) {
        if (clientMsgId == null || clientMsgId.isEmpty()) {
            return 0;
        }

        try {
            String countKey = RETRY_COUNT_PREFIX + clientMsgId;
            Object countObj = redisUtil.get(countKey);
            return countObj != null ? Integer.parseInt(countObj.toString()) : 0;
        } catch (Exception e) {
            ExceptionHandlerUtil.logError(log, "获取重试次数异常: clientMsgId={}", e, clientMsgId);
            return 0;
        }
    }

    @Override
    public void clearFailureRecord(String clientMsgId) {
        if (clientMsgId == null || clientMsgId.isEmpty()) {
            return;
        }

        try {
            redisUtil.delete(RETRY_KEY_PREFIX + clientMsgId);
            redisUtil.delete(RETRY_COUNT_PREFIX + clientMsgId);
            redisUtil.delete(RETRY_DATA_PREFIX + clientMsgId);

            log.debug("清除消息失败记录: clientMsgId={}", clientMsgId);
        } catch (Exception e) {
            ExceptionHandlerUtil.logError(log, "清除失败记录异常: clientMsgId={}", e, clientMsgId);
        }
    }

    @Override
    public boolean canRetry(String clientMsgId) {
        if (clientMsgId == null || clientMsgId.isEmpty()) {
            return false;
        }

        String retryKey = RETRY_KEY_PREFIX + clientMsgId;
        return redisUtil.exists(retryKey) && getRetryCount(clientMsgId) < MAX_RETRY_COUNT;
    }

    /**
     * 失败消息数据结构
     */
    static class FailureData {
        private String clientMsgId;
        private ImMessageSendRequest request;
        private Long userId;
        private String errorMessage;
        private Long firstFailTime;

        public String getClientMsgId() {
            return clientMsgId;
        }

        public void setClientMsgId(String clientMsgId) {
            this.clientMsgId = clientMsgId;
        }

        public ImMessageSendRequest getRequest() {
            return request;
        }

        public void setRequest(ImMessageSendRequest request) {
            this.request = request;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Long getFirstFailTime() {
            return firstFailTime;
        }

        public void setFirstFailTime(Long firstFailTime) {
            this.firstFailTime = firstFailTime;
        }
    }
}
