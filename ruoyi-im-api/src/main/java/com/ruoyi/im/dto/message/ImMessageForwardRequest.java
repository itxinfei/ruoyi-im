package com.ruoyi.im.dto.message;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 消息转发请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImMessageForwardRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    
    private Long toConversationId;

    
    private Long toUserId;

    
    private String content;
}

