package com.ruoyi.im.dto.videocall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 切换设备状态请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "切换设备状态请求")
public class VideoCallToggleRequest {

    @NotNull(message = "状态不能为空")
    @Schema(description = "是否开启/关闭", required = true, example = "true")
    private Boolean enabled;
}