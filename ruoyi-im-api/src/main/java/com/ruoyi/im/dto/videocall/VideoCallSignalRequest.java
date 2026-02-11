package com.ruoyi.im.dto.videocall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * WebRTC信令请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "WebRTC信令请求")
public class VideoCallSignalRequest {

    @NotNull(message = "通话ID不能为空")
    @Schema(description = "通话ID", required = true)
    private Long callId;

    @NotBlank(message = "信令类型不能为空")
    @Schema(description = "信令类型", required = true, example = "offer")
    private String signalType;

    @NotBlank(message = "信令数据不能为空")
    @Schema(description = "信令数据", required = true)
    private String signalData;
}