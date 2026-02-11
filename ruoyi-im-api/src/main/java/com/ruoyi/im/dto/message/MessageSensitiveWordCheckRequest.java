package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 敏感词检测请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "敏感词检测请求")
public class MessageSensitiveWordCheckRequest {

    @NotBlank(message = "检测文本不能为空")
    @Schema(description = "待检测文本", required = true)
    private String text;
}