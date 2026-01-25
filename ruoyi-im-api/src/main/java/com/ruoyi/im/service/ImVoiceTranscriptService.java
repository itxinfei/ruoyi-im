package com.ruoyi.im.service;

import com.ruoyi.im.vo.transcript.ImVoiceTranscriptVO;

/**
 * 语音转文字服务接口
 *
 * @author ruoyi
 */
public interface ImVoiceTranscriptService {

    /**
     * 创建语音转文字任务
     *
     * @param messageId 消息ID
     * @param voiceUrl  语音文件URL
     * @param duration  语音时长（秒）
     * @param language  语言类型
     * @param userId    用户ID
     * @return 任务ID
     */
    Long createTranscriptTask(Long messageId, String voiceUrl, Integer duration, String language, Long userId);

    /**
     * 获取转写结果
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 转写结果VO
     */
    ImVoiceTranscriptVO getTranscript(Long messageId, Long userId);

    /**
     * 重新转写
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 新任务ID
     */
    Long reTranscribe(Long messageId, Long userId);

    /**
     * 处理待转写任务
     * 由定时任务调用，批量处理待转写的语音消息
     *
     * @return 处理的任务数量
     */
    int processPendingTasks();

    /**
     * 获取用户转写列表
     *
     * @param userId 用户ID
     * @return 转写列表
     */
    java.util.List<ImVoiceTranscriptVO> getUserTranscripts(Long userId);

    /**
     * 获取转写统计信息
     *
     * @param userId 用户ID
     * @return 统计信息（总次数、成功次数、失败次数）
     */
    java.util.Map<String, Object> getTranscriptStats(Long userId);
}
