package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 批量转发请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "批量转发请求")
public class ImMessageBatchForwardRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原消息ID列表", required = true)
    @NotEmpty(message = "消息ID列表不能为空")
    private List<Long> messageIds;

    @Schema(description = "目标会话ID", required = true)
    @NotNull(message = "目标会话ID不能为空")
    private Long toConversationId;

    @Schema(description = "转发方式：batch=逐条转发, combine=合并转发", required = true)
    @NotEmpty(message = "转发方式不能为空")
    private String forwardType; // batch 或 combine

    @Schema(description = "转发时附加的说明内容")
    private String content;
}
