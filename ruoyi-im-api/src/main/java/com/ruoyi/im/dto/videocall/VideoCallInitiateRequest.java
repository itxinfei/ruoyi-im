package com.ruoyi.im.dto.videocall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发起通话请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "发起通话请求")
public class VideoCallInitiateRequest {

    @NotNull(message = "被叫用户ID不能为空")
    @Schema(description = "被叫用户ID", required = true)
    private Long calleeId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @NotBlank(message = "通话类型不能为空")
    @Schema(description = "通话类型", required = true, allowableValues = {"VIDEO", "AUDIO"}, example = "VIDEO")
    private String callType;
}