package com.ruoyi.im.vo.transcript;

import lombok.Data;

import java.io.Serializable;

/**
 * 语音转文字统计结果VO
 *
 * @author ruoyi
 */
@Data
public class VoiceTranscriptStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总转写次数
     */
    private Integer totalCount;

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 失败次数
     */
    private Integer failedCount;

    /**
     * 待处理次数
     */
    private Integer pendingCount;
}
