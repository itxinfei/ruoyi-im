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
 * 公告点赞记录实体类
 *
 * @author ruoyi
 */
@TableName("im_announcement_like")
@Data
@Schema(description = "公告点赞记录")
public class ImAnnouncementLike implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("announcement_id")
    @Schema(description = "公告ID")
    private Long announcementId;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("like_time")
    @Schema(description = "点赞时间")
    private LocalDateTime likeTime;

    @TableField(exist = false)
    @Schema(description = "用户昵称")
    private String userNickname;

    @TableField(exist = false)
    @Schema(description = "用户头像")
    private String userAvatar;
}
