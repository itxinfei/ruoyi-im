package com.ruoyi.im.vo.marker;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息标记VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "消息标记")
public class ImMessageMarkerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标记ID")
    private Long id;

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "标记类型")
    private String markerType;

    @Schema(description = "标记颜色")
    private String color;

    @Schema(description = "提醒时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime remindTime;

    @Schema(description = "待办状态")
    private String todoStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime doneTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ==================== 扩展字段 ====================

    @Schema(description = "消息内容预览")
    private String messagePreview;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者名称")
    private String senderName;

    @Schema(description = "会话名称")
    private String conversationName;

    @Schema(description = "是否过期")
    private Boolean isExpired;
}
