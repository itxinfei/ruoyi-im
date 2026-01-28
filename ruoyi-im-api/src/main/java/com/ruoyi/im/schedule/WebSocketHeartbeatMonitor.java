package com.ruoyi.im.schedule;

import com.ruoyi.im.util.ImRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WebSocket心跳监控定时任务
 * 定期检测并清理超时的WebSocket连接
 *
 * @author ruoyi
 */
@Component
public class WebSocketHeartbeatMonitor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHeartbeatMonitor.class);

    @Autowired
    private ImRedisUtil imRedisUtil;

    /**
     * 心跳超时时间（秒）
     * 默认90秒无心跳则判定为超时
     */
    private static final long HEARTBEAT_TIMEOUT_SECONDS = 90;

    /**
     * 检测频率（秒）
     * 每30秒检测一次
     */
    private static final long CHECK_INTERVAL_SECONDS = 30;

    /**
     * 定时检测心跳超时连接
     * 每30秒执行一次，清理超时未发送心跳的连接
     */
    @Scheduled(fixedRate = CHECK_INTERVAL_SECONDS * 1000)
    public void monitorHeartbeat() {
        try {
            // 获取所有在线用户
            Set<String> onlineUsers = imRedisUtil.getOnlineUsers();

            if (onlineUsers.isEmpty()) {
                log.debug("当前没有在线用户，跳过心跳检测");
                return;
            }

            log.debug("开始心跳检测，在线用户数: {}", onlineUsers.size());

            List<Long> timeoutUsers = new ArrayList<>();

            // 检查每个用户的最后心跳时间
            for (String userIdStr : onlineUsers) {
                try {
                    Long userId = Long.parseLong(userIdStr);

                    // 检查心跳是否超时
                    if (imRedisUtil.isHeartbeatTimeout(userId, HEARTBEAT_TIMEOUT_SECONDS)) {
                        timeoutUsers.add(userId);
                        log.warn("用户心跳超时: userId={}", userId);
                    }
                } catch (NumberFormatException e) {
                    log.error("解析用户ID失败: userIdStr={}", userIdStr, e);
                }
            }

            // 清理超时用户
            if (!timeoutUsers.isEmpty()) {
                for (Long userId : timeoutUsers) {
                    try {
                        // 从Redis中移除在线状态
                        imRedisUtil.removeOnlineUser(userId);

                        // 这里无法直接关闭Session，因为Session存储在内存中
                        // 需要通过其他机制通知关闭，或者等待客户端重连时检测到过期Token
                        log.info("已清理心跳超时用户: userId={}", userId);
                    } catch (Exception e) {
                        log.error("清理超时用户失败: userId={}", userId, e);
                    }
                }

                log.info("心跳检测完成，清理超时用户数: {}", timeoutUsers.size());
            } else {
                log.debug("心跳检测完成，所有用户心跳正常");
            }

        } catch (Exception e) {
            log.error("心跳监控任务执行异常", e);
        }
    }

    /**
     * 获取心跳超时时间配置
     *
     * @return 超时时间（秒）
     */
    public long getHeartbeatTimeoutSeconds() {
        return HEARTBEAT_TIMEOUT_SECONDS;
    }

    /**
     * 获取检测频率配置
     *
     * @return 检测频率（秒）
     */
    public long getCheckIntervalSeconds() {
        return CHECK_INTERVAL_SECONDS;
    }
}