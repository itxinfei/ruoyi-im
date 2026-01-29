package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constants.StatusConstants;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImVoiceTranscript;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImVoiceTranscriptMapper;
import com.ruoyi.im.service.ImVoiceTranscriptService;
import com.ruoyi.im.vo.transcript.ImVoiceTranscriptVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 语音转文字服务实现
 *
 * @author ruoyi
 */
@Service
public class ImVoiceTranscriptServiceImpl implements ImVoiceTranscriptService {

    private static final Logger logger = LoggerFactory.getLogger(ImVoiceTranscriptServiceImpl.class);

    /**
     * 默认语音识别服务商
     */
    private static final String DEFAULT_PROVIDER = "ALIYUN";

    /**
     * 默认语言类型
     */
    private static final String DEFAULT_LANGUAGE = "zh-CN";

    @Autowired
    private ImVoiceTranscriptMapper voiceTranscriptMapper;

    @Autowired
    private ImMessageMapper messageMapper;

    @Value("${voice.transcript.enabled:false}")
    private Boolean transcriptEnabled;

    @Value("${voice.transcript.provider:ALIYUN}")
    private String transcriptProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTranscriptTask(Long messageId, String voiceUrl, Integer duration, String language, Long userId) {
        // 检查是否启用语音转文字
        if (!Boolean.TRUE.equals(transcriptEnabled)) {
            throw new BusinessException("语音转文字功能未启用");
        }

        // 检查消息是否存在
        ImMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 检查是否已有转写记录
        ImVoiceTranscript existTranscript = voiceTranscriptMapper.selectByMessageId(messageId);
        if (existTranscript != null && "SUCCESS".equals(existTranscript.getStatus())) {
            return existTranscript.getId();
        }

        // 创建转写任务
        ImVoiceTranscript transcript = new ImVoiceTranscript();
        transcript.setMessageId(messageId);
        transcript.setVoiceUrl(voiceUrl);
        transcript.setDuration(duration);
        transcript.setLanguage(language != null ? language : DEFAULT_LANGUAGE);
        transcript.setStatus(StatusConstants.Task.PENDING);
        transcript.setProvider(transcriptProvider != null ? transcriptProvider : DEFAULT_PROVIDER);
        transcript.setCreateTime(LocalDateTime.now());

        voiceTranscriptMapper.insert(transcript);

        logger.info("创建语音转文字任务: transcriptId={}, messageId={}", transcript.getId(), messageId);
        return transcript.getId();
    }

    @Override
    public ImVoiceTranscriptVO getTranscript(Long messageId, Long userId) {
        ImVoiceTranscript transcript = voiceTranscriptMapper.selectByMessageId(messageId);

        if (transcript == null) {
            return null;
        }

        ImVoiceTranscriptVO vo = new ImVoiceTranscriptVO();
        BeanUtils.copyProperties(transcript, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reTranscribe(Long messageId, Long userId) {
        // 删除旧的转写记录
        voiceTranscriptMapper.delete(
                new LambdaQueryWrapper<ImVoiceTranscript>()
                        .eq(ImVoiceTranscript::getMessageId, messageId)
        );

        // 获取消息信息
        ImMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 创建新的转写任务
        return createTranscriptTask(messageId, message.getFileUrl(),
                extractDuration(message), null, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processPendingTasks() {
        List<ImVoiceTranscript> pendingTasks = voiceTranscriptMapper.selectPending(10);

        int processed = 0;
        for (ImVoiceTranscript task : pendingTasks) {
            try {
                // 更新状态为处理中
                task.setStatus("PROCESSING");
                voiceTranscriptMapper.updateById(task);

                // 调用语音识别服务
                String result = performTranscription(task);

                // 更新结果
                if (result != null) {
                    task.setTranscriptText(result);
                    task.setStatus("SUCCESS");
                    task.setConfidence(95);
                    task.setCompleteTime(LocalDateTime.now());
                } else {
                    task.setStatus("FAILED");
                    task.setErrorCode("SERVICE_ERROR");
                    task.setErrorMsg("语音识别服务返回为空");
                }

                task.setProcessDuration(calculateProcessDuration(task.getCreateTime()));
                voiceTranscriptMapper.updateById(task);
                processed++;

            } catch (Exception e) {
                logger.error("处理语音转写任务失败: transcriptId={}", task.getId(), e);
                task.setStatus("FAILED");
                task.setErrorCode("PROCESS_ERROR");
                task.setErrorMsg(e.getMessage());
                voiceTranscriptMapper.updateById(task);
            }
        }

        if (processed > 0) {
            logger.info("处理语音转写任务完成: count={}", processed);
        }

        return processed;
    }

    @Override
    public List<ImVoiceTranscriptVO> getUserTranscripts(Long userId) {
        List<ImVoiceTranscript> transcripts = voiceTranscriptMapper.selectByUserId(userId);

        return transcripts.stream().map(transcript -> {
            ImVoiceTranscriptVO vo = new ImVoiceTranscriptVO();
            BeanUtils.copyProperties(transcript, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTranscriptStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        List<ImVoiceTranscript> transcripts = voiceTranscriptMapper.selectByUserId(userId);

        int totalCount = transcripts.size();
        int successCount = (int) transcripts.stream().filter(t -> "SUCCESS".equals(t.getStatus())).count();
        int failedCount = (int) transcripts.stream().filter(t -> "FAILED".equals(t.getStatus())).count();
        int pendingCount = (int) transcripts.stream().filter(t -> StatusConstants.Task.PENDING.equals(t.getStatus())
                || "PROCESSING".equals(t.getStatus())).count();

        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("pendingCount", pendingCount);

        return stats;
    }

    /**
     * 执行语音识别
     * 这里是模拟实现，实际需要调用第三方语音识别服务
     */
    private String performTranscription(ImVoiceTranscript task) {
        // TODO: 集成阿里云、讯飞或腾讯云语音识别服务
        logger.info("执行语音识别: transcriptId={}, provider={}", task.getId(), task.getProvider());

        // 模拟识别结果
        return "这是一段测试语音转文字的内容";
    }

    /**
     * 从消息中提取时长
     */
    private Integer extractDuration(ImMessage message) {
        // 实际应该从语音文件中获取时长
        return 10;
    }

    /**
     * 计算处理时长
     */
    private Long calculateProcessDuration(LocalDateTime startTime) {
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
    }
}
