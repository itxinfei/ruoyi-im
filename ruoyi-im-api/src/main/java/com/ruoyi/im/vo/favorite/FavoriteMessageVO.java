package com.ruoyi.im.vo.favorite;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏消息VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "收藏消息")
public class FavoriteMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "会话名称")
    private String conversationName;

    @Schema(description = "消息内容")
    private String messageContent;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "发送者名称")
    private String senderName;

    @Schema(description = "发送者头像")
    private String senderAvatar;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "消息时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime messageTime;

    @Schema(description = "收藏备注")
    private String remark;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "收藏时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
