package com.ruoyi.im.vo.transcript;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 语音转文字VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "语音转文字")
public class ImVoiceTranscriptVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "语音文件URL")
    private String voiceUrl;

    @Schema(description = "语音时长（秒）")
    private Integer duration;

    @Schema(description = "转换状态")
    private String status;

    @Schema(description = "识别文本")
    private String transcriptText;

    @Schema(description = "语言类型")
    private String language;

    @Schema(description = "置信度")
    private Integer confidence;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
}
