package com.ruoyi.im.schedule;

import com.ruoyi.im.mapper.ImBotMessageLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 机器人消息日志清理定时任务
 * 定期清理超过30天的Bot消息日志，防止数据库无限增长
 *
 * @author ruoyi
 */
@Component
public class BotMessageLogCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(BotMessageLogCleanupTask.class);

    /** 日志保留天数 */
    private static final int RETAIN_DAYS = 30;

    @Resource
    private ImBotMessageLogMapper botMessageLogMapper;

    /**
     * 每天凌晨3点清理过期日志
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredLogs() {
        try {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(RETAIN_DAYS);
            int deletedCount = botMessageLogMapper.deleteBeforeTime(beforeTime);
            log.info("Bot消息日志清理完成: 删除{}条超过{}天的日志", deletedCount, RETAIN_DAYS);
        } catch (Exception e) {
            log.error("Bot消息日志清理失败", e);
        }
    }
}
