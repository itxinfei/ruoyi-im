package com.ruoyi.web.domain.dto;

import lombok.Data;
import javax.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

/**
 * 消息发送请求DTO
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Data
public class MessageSendDTO {

    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    @NotBlank(message = "消息类型不能为空")
    @Pattern(regexp = "^(TEXT|IMAGE|FILE|VOICE|VIDEO|SYSTEM)$", 
             message = "消息类型必须是TEXT、IMAGE、FILE、VOICE、VIDEO、SYSTEM之一")
    private String messageType;

    @NotBlank(message = "消息内容不能为空")
    @Length(max = 10000, message = "消息内容长度不能超过10000个字符")
    private String content;

    private Long replyToMessageId;

    private String extraData;
}