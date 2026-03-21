package com.ruoyi.im.dto.message;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 消息批量转发请求DTO
 *
 * @author ruoyi
 */
@Data
public class ImMessageBatchForwardRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待转发的消息ID列表
     */
    @NotEmpty(message = "待转发消息列表不能为空")
    private List<Long> messageIds;

    /**
     * 目标会话ID列表
     */
    @NotEmpty(message = "目标会话列表不能为空")
    private List<Long> toConversationIds;

    /**
     * 是否合并转发（true：合并为一条；false：逐条转发）
     */
    private Boolean isCombine;

    /**
     * 合并转发时的标题（仅在 isCombine 为 true 时有效）
     */
    private String title;
}
