package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 消息撤回请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "消息撤回请求")
public class ImMessageRecallRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID", required = true)
    @NotNull(message = "消息ID不能为空")
    private Long messageId;
}
