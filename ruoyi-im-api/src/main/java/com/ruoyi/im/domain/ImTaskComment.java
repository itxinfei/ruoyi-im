package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务评论实体类
 *
 * @author ruoyi
 */
@TableName("im_task_comment")
@Data
@Schema(description = "任务评论")
public class ImTaskComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "评论ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    @Schema(description = "任务ID")
    private Long taskId;

    @TableField("user_id")
    @Schema(description = "评论用户ID")
    private Long userId;

    @TableField("content")
    @Schema(description = "评论内容")
    private String content;

    @TableField("reply_to_user_id")
    @Schema(description = "回复的用户ID")
    private Long replyToUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    @Schema(description = "评论时间")
    private LocalDateTime createTime;

    @TableField("is_deleted")
    @Schema(description = "是否删除（0=正常 1=已删除）")
    private Integer isDeleted;

    @TableField(exist = false)
    @Schema(description = "用户昵称")
    private String userNickname;

    @TableField(exist = false)
    @Schema(description = "用户头像")
    private String userAvatar;

    @TableField(exist = false)
    @Schema(description = "回复的用户昵称")
    private String replyToUserNickname;
}
