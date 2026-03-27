package com.ruoyi.im.dto.message;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 标记消息已读请求
 */
@Data
public class MarkAsReadRequest implements Serializable {

    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    @NotEmpty(message = "消息ID列表不能为空")
    private List<Long> messageIds;
}
