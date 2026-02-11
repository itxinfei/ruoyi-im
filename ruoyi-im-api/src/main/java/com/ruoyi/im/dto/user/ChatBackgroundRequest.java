package com.ruoyi.im.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 聊天背景设置请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "聊天背景设置请求")
public class ChatBackgroundRequest {

    @NotBlank(message = "背景类型不能为空")
    @Schema(description = "背景类型：default/color/image", required = true)
    private String type;

    @Schema(description = "背景值（颜色值或图片URL）")
    private String value;

    @Schema(description = "会话ID（可选），不传则设置全局背景")
    private Long conversationId;
}