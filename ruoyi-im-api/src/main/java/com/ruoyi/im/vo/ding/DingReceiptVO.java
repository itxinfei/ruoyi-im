package com.ruoyi.im.vo.ding;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DING回执VO
 */
@Data
@Schema(description = "DING回执")
public class DingReceiptVO implements Serializable {

    @Schema(description = "回执ID")
    private Long id;

    @Schema(description = "接收者ID")
    private Long receiverId;

    @Schema(description = "接收者名称")
    private String receiverName;

    @Schema(description = "接收者头像")
    private String receiverAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "是否确认")
    private Integer confirmed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "备注")
    private String remark;
}
