package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 标记消息已读请求 DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "标记消息已读请求")
public class ImMessageMarkReadRequest {

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "1")
    private Long conversationId;

    /**
     * 消息ID列表（批量标记时使用）
     */
    @Schema(description = "消息ID列表，为空则标记整个会话为已读", example = "[1, 2, 3]")
    private List<Long> messageIds;

    /**
     * 最后已读消息ID（用于标记该消息之前的所有消息为已读）
     */
    @Schema(description = "最后已读消息ID，该消息之前的所有消息将被标记为已读", example = "100")
    private Long lastReadMessageId;
}
