package com.ruoyi.im.vo.ding;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DING详情VO
 */
@Data
@Schema(description = "DING详情")
public class DingDetailVO implements Serializable {

    @Schema(description = "DING ID")
    private Long id;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者名称")
    private String senderName;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "DING内容")
    private String content;

    @Schema(description = "DING类型")
    private String dingType;

    @Schema(description = "是否紧急")
    private Integer isUrgent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "总接收人数")
    private Integer totalCount;

    @Schema(description = "已读人数")
    private Integer readCount;

    @Schema(description = "已确认人数")
    private Integer confirmedCount;

    @Schema(description = "附件URL")
    private String attachment;

    @Schema(description = "回执列表")
    private List<DingReceiptVO> receipts;
}
