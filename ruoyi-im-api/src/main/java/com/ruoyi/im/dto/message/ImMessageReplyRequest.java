package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 消息回复请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "消息回复请求")
public class ImMessageReplyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原消息ID", required = true)
    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    @Schema(description = "回复内容", required = true)
    @NotBlank(message = "回复内容不能为空")
    private String content;
}
