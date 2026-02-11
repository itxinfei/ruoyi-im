package com.ruoyi.im.schedule;

import com.ruoyi.im.mapper.ImConversationMemberMapper;
import com.ruoyi.im.util.ImRedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 未读数同步定时任务
 * 定期将 Redis 中的未读数同步到数据库
 *
 * @author ruoyi
 */
@Component
public class UnreadCountSyncTask {

    private static final Logger log = LoggerFactory.getLogger(UnreadCountSyncTask.class);

    @Autowired(required = false)
    private ImRedisUtil redisUtil;

    @Autowired(required = false)
    private ImConversationMemberMapper conversationMemberMapper;

    /**
     * 同步未读数到数据库
     * 每 5 分钟执行一次，将 Redis 中的未读数同步到数据库
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncUnreadCountToDatabase() {
        if (redisUtil == null || conversationMemberMapper == null) {
            return;
        }

        try {
            // 获取所有有未读数据的用户（通过在线用户列表）
            Set<Long> onlineUserIds = redisUtil.getOnlineUserIds();
            if (onlineUserIds == null || onlineUserIds.isEmpty()) {
                return;
            }

            int totalSynced = 0;
            int totalFailed = 0;

            for (Long userId : onlineUserIds) {
                try {
                    // 获取用户在 Redis 中的所有未读数
                    Map<Long, Integer> unreadCounts = redisUtil.getAllUnreadCounts(userId);
                    if (unreadCounts == null || unreadCounts.isEmpty()) {
                        continue;
                    }

                    // 同步到数据库
                    for (Map.Entry<Long, Integer> entry : unreadCounts.entrySet()) {
                        Long conversationId = entry.getKey();
                        Integer count = entry.getValue();
                        try {
                            conversationMemberMapper.syncUnreadCount(userId, conversationId, count);
                            totalSynced++;
                        } catch (Exception e) {
                            log.warn("同步未读数失败: userId={}, conversationId={}, count={}", 
                                    userId, conversationId, count, e);
                            totalFailed++;
                        }
                    }
                } catch (Exception e) {
                    log.error("同步用户未读数失败: userId={}", userId, e);
                }
            }

            if (totalSynced > 0 || totalFailed > 0) {
                log.info("未读数同步完成: 成功={}, 失败={}", totalSynced, totalFailed);
            }

        } catch (Exception e) {
            log.error("同步未读数定时任务执行异常", e);
        }
    }
}