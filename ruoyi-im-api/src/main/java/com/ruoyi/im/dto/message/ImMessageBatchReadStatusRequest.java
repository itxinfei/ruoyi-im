package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量获取消息已读状态请求 DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "批量获取消息已读状态请求")
public class ImMessageBatchReadStatusRequest {

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "1")
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * 消息ID列表
     */
    @Schema(description = "消息ID列表", example = "[1, 2, 3]")
    @NotEmpty(message = "消息ID列表不能为空")
    private List<Long> messageIds;
}
