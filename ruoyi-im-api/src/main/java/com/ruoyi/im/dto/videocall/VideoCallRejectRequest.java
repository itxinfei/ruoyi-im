package com.ruoyi.im.dto.videocall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拒绝通话请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "拒绝通话请求")
public class VideoCallRejectRequest {

    @Schema(description = "拒绝原因", example = "现在不方便接听")
    private String reason;
}