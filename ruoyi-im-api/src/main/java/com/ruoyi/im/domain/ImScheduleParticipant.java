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
 * 日程参与人实体类
 */
@TableName("im_schedule_participant")
@Data
@Schema(description = "日程参与人")
public class ImScheduleParticipant implements Serializable {

    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("event_id")
    @Schema(description = "日程ID")
    private Long eventId;

    @TableField("user_id")
    @Schema(description = "参与人ID")
    private Long userId;

    @Schema(description = "参与状态")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "回复时间")
    private LocalDateTime responseTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(exist = false)
    @Schema(description = "参与人姓名")
    private String userName;

    @TableField(exist = false)
    @Schema(description = "参与人头像")
    private String userAvatar;
}
