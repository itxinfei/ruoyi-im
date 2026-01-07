package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 消息转发请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "消息转发请求")
public class ImMessageForwardRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原消息ID", required = true)
    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    @Schema(description = "目标会话ID，为空则转发到原会话")
    private Long toConversationId;

    @Schema(description = "目标用户ID，为空则转发给原接收者")
    private Long toUserId;

    @Schema(description = "转发时附加的说明内容")
    private String content;
}
