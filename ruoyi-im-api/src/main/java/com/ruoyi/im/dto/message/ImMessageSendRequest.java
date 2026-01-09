package com.ruoyi.im.dto.message;

import com.ruoyi.im.dto.mention.ImMentionInfo;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 消息发送请求
 *
 * @author ruoyi
 */
@Data
public class ImMessageSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /** 消息类型 */
    @NotBlank(message = "消息类型不能为空")
    private String type;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 接收者ID（群组消息时必填） */
    private Long receiverId;

    /** 回复消息ID（用于回复/引用功能） */
    private Long replyToMessageId;

    /** 客户端消息ID（用于去重） */
    private String clientMsgId;

    /** 扩展数据（JSON格式，用于文件、图片等） */
    private String extra;

    /** @提及信息 */
    private ImMentionInfo mentionInfo;
}
