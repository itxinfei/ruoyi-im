package com.ruoyi.im.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发送通知请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "发送通知请求")
public class NotificationSendRequest {

    @NotNull(message = "接收者ID不能为空")
    @Schema(description = "接收者ID", required = true)
    private Long receiverId;

    @NotBlank(message = "通知类型不能为空")
    @Schema(description = "通知类型", required = true, example = "SYSTEM")
    private String type;

    @NotBlank(message = "通知标题不能为空")
    @Schema(description = "通知标题", required = true)
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @Schema(description = "通知内容", required = true)
    private String content;

    @Schema(description = "关联ID", example = "1001")
    private Long relatedId;

    @Schema(description = "关联类型", example = "TASK")
    private String relatedType;
}