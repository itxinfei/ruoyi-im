package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 敏感词过滤请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "敏感词过滤请求")
public class MessageSensitiveWordFilterRequest {

    @NotBlank(message = "过滤文本不能为空")
    @Schema(description = "待过滤文本", required = true)
    private String text;

    @Schema(description = "替换字符，默认为***")
    private String replacement;
}