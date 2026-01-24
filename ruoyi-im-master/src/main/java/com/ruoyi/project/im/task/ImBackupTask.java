package com.ruoyi.web.task;

import com.ruoyi.web.service.ImBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * IM数据备份定时任务
 *
 * <p>提供定时自动备份功能，支持每日备份和每周备份</p>
 * <p>通过配置文件控制是否启用定时备份</p>
 *
 * @author ruoyi
 */
@Component
public class ImBackupTask {

    private static final Logger logger = LoggerFactory.getLogger(ImBackupTask.class);

    @Autowired
    private ImBackupService backupService;

    /** 是否启用每日自动备份 */
    @Value("${backup.auto.daily.enabled:false}")
    private boolean dailyBackupEnabled;

    /** 是否启用每周自动备份 */
    @Value("${backup.auto.weekly.enabled:false}")
    private boolean weeklyBackupEnabled;

    /**
     * 每日自动备份
     *
     * <p>每天凌晨 2 点执行</p>
     * <p>可通过配置 backup.auto.daily.enabled 控制是否启用</p>
     */
    @Scheduled(cron = "${backup.auto.daily.cron:0 0 2 * * ?}")
    public void dailyBackup() {
        if (!dailyBackupEnabled) {
            return;
        }

        logger.info("开始执行每日自动备份任务");
        try {
            backupService.autoBackup("AUTO_DAILY");
            logger.info("每日自动备份任务执行成功");
        } catch (Exception e) {
            logger.error("每日自动备份任务执行失败", e);
        }
    }

    /**
     * 每周自动备份
     *
     * <p>每周日凌晨 3 点执行</p>
     * <p>可通过配置 backup.auto.weekly.enabled 控制是否启用</p>
     */
    @Scheduled(cron = "${backup.auto.weekly.cron:0 0 3 ? * SUN}")
    public void weeklyBackup() {
        if (!weeklyBackupEnabled) {
            return;
        }

        logger.info("开始执行每周自动备份任务");
        try {
            backupService.autoBackup("AUTO_WEEKLY");
            logger.info("每周自动备份任务执行成功");
        } catch (Exception e) {
            logger.error("每周自动备份任务执行失败", e);
        }
    }
}
