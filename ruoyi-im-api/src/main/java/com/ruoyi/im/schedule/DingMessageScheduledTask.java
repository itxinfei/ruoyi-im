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
     * 处理长时间未完成的DING消息
     * 每小时执行一次，检查并处理超过24小时仍处于SENDING状态的DING消息
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processExpiredDingMessages() {
        try {
            // 查询超过24小时仍处于SENDING状态的DING消息
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            LambdaQueryWrapper<ImDingMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(ImDingMessage::getId,
                    ImDingMessage::getSenderId,
                    ImDingMessage::getContent,
                    ImDingMessage::getDingType,
                    ImDingMessage::getIsUrgent,
                    ImDingMessage::getStatus,
                    ImDingMessage::getSendTime,
                    ImDingMessage::getCreateTime)
                    .eq(ImDingMessage::getStatus, "SENDING")
                    .le(ImDingMessage::getCreateTime, oneDayAgo);

            List<ImDingMessage> expiredDings = dingMessageMapper.selectList(wrapper);

            if (!expiredDings.isEmpty()) {
                log.info("发现 {} 条长时间未完成的DING消息", expiredDings.size());
                // 将长时间未完成的消息状态更新为FAILED
                for (ImDingMessage ding : expiredDings) {
                    ding.setStatus("FAILED");
                    dingMessageMapper.updateById(ding);
                }
                log.info("长时间未完成DING消息处理完成，处理数量: {}", expiredDings.size());
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

            // 统计发送中的DING消息数量
            LambdaQueryWrapper<ImDingMessage> sendingWrapper = new LambdaQueryWrapper<>();
            sendingWrapper.eq(ImDingMessage::getStatus, "SENDING");
            Long sendingCount = dingMessageMapper.selectCount(sendingWrapper);

            // 统计失败的DING消息数量
            LambdaQueryWrapper<ImDingMessage> failedWrapper = new LambdaQueryWrapper<>();
            failedWrapper.eq(ImDingMessage::getStatus, "FAILED");
            Long failedCount = dingMessageMapper.selectCount(failedWrapper);

            log.info("DING消息统计 - 已发送: {}, 发送中: {}, 失败: {}", sentCount, sendingCount, failedCount);

        } catch (Exception e) {
            log.error("生成DING消息统计失败", e);
        }
    }
}
