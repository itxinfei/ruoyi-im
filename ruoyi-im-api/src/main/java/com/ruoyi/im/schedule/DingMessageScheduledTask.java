package com.ruoyi.im.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImDingMessage;
import com.ruoyi.im.mapper.ImDingMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DING消息定时任务
 * 处理过期DING消息的清理
 *
 * @author ruoyi
 */
@Component
public class DingMessageScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(DingMessageScheduledTask.class);

    @Autowired
    private ImDingMessageMapper dingMessageMapper;

    /**
     * 处理过期DING消息
     * 每小时执行一次，检查并处理过期的DING消息
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processExpiredDingMessages() {
        try {
            // 查询已过期的DING消息
            LambdaQueryWrapper<ImDingMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(ImDingMessage::getId,
                        ImDingMessage::getSenderId,
                        ImDingMessage::getContent,
                        ImDingMessage::getDingType,
                        ImDingMessage::getPriority,
                        ImDingMessage::getStatus,
                        ImDingMessage::getExpireTime,
                        ImDingMessage::getCreateTime)
                    .eq(ImDingMessage::getStatus, "SENT")
                    .isNotNull(ImDingMessage::getExpireTime)
                    .le(ImDingMessage::getExpireTime, LocalDateTime.now());

            List<ImDingMessage> expiredDings = dingMessageMapper.selectList(wrapper);

            if (!expiredDings.isEmpty()) {
                log.info("发现 {} 条过期DING消息", expiredDings.size());
                // 可以在这里将过期消息状态更新为EXPIRED
                for (ImDingMessage ding : expiredDings) {
                    ding.setStatus("EXPIRED");
                    dingMessageMapper.updateById(ding);
                }
                log.info("过期DING消息处理完成，处理数量: {}", expiredDings.size());
            }

        } catch (Exception e) {
            log.error("处理过期DING消息失败", e);
        }
    }

    /**
     * DING消息统计任务
     * 每天凌晨执行，统计DING消息数据
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDingStatistics() {
        try {
            // 统计已发送的DING消息数量
            LambdaQueryWrapper<ImDingMessage> sentWrapper = new LambdaQueryWrapper<>();
            sentWrapper.eq(ImDingMessage::getStatus, "SENT");
            Long sentCount = dingMessageMapper.selectCount(sentWrapper);

            // 统计已过期的DING消息数量
            LambdaQueryWrapper<ImDingMessage> expiredWrapper = new LambdaQueryWrapper<>();
            expiredWrapper.eq(ImDingMessage::getStatus, "EXPIRED");
            Long expiredCount = dingMessageMapper.selectCount(expiredWrapper);

            log.info("DING消息统计 - 已发送: {}, 已过期: {}", sentCount, expiredCount);

        } catch (Exception e) {
            log.error("生成DING消息统计失败", e);
        }
    }
}
