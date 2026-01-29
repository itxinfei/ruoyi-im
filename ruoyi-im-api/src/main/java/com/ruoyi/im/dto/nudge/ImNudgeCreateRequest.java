package com.ruoyi.im.dto.nudge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 拍一拍创建请求
 *
 * @author ruoyi
 */
@Schema(description = "拍一拍创建请求")
@Data
public class ImNudgeCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID", required = true)
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    @Schema(description = "被拍用户ID", required = true)
    @NotNull(message = "被拍用户ID不能为空")
    private Long nudgedUserId;
}
