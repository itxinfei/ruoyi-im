package com.ruoyi.im.dto.reaction;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 消息反应添加请求DTO
 *
 * @author ruoyi
 */
@Data
public class ImMessageReactionAddRequest {

    /** 消息ID */
    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    /** 反应类型：EMOJI表情 LIKE点赞 CLAP拍手 HEART爱心 */
    private String reactionType;

    /** emoji表情字符（如：👍❤️😂🎉） */
    @NotBlank(message = "表情不能为空")
    private String emoji;
}
