package com.ruoyi.im.dto.ding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 发送DING请求
 */
@Data
@Schema(description = "发送DING请求")
public class DingSendRequest {

    @NotBlank(message = "DING内容不能为空")
    @Schema(description = "DING内容")
    private String content;

    @Schema(description = "DING类型（APP应用 SMS短信 PHONE电话）")
    private String dingType = "APP";

    @Schema(description = "是否紧急（0否 1是）")
    private Integer isUrgent = 0;

    @Schema(description = "定时发送时间（为空则立即发送）")
    private LocalDateTime scheduleTime;

    @NotNull(message = "接收者不能为空")
    @Schema(description = "接收者ID列表")
    private Long[] receiverIds;

    @Schema(description = "是否需要回执")
    private Boolean receiptRequired = true;

    @Schema(description = "强提醒间隔（分钟，0表示不重复提醒）")
    private Integer remindInterval = 0;

    @Schema(description = "最大提醒次数")
    private Integer maxRemindCount = 3;

    @Schema(description = "附件URL")
    private String attachment;
}
