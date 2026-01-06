package com.ruoyi.im.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息引用VO
 * 用于消息转发、回复等场景的引用信息展示
 *
 * @author ruoyi
 */
@Data
@Schema(description = "消息引用信息")
public class ImMessageReferenceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "引用ID")
    private Long id;

    @Schema(description = "原消息ID")
    private Long originalMessageId;

    @Schema(description = "引用类型: forward=转发, reply=回复")
    private String referenceType;

    @Schema(description = "原发送者ID")
    private Long originalSenderId;

    @Schema(description = "原发送者昵称")
    private String originalSenderName;

    @Schema(description = "原消息内容预览")
    private String originalContentPreview;

    @Schema(description = "原消息类型")
    private String originalMessageType;

    @Schema(description = "引用时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime referenceTime;
}
