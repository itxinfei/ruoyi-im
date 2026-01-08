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
 * 工作日志评论实体类
 */
@TableName("im_work_report_comment")
@Data
@Schema(description = "工作日志评论")
public class ImWorkReportComment implements Serializable {

    @Schema(description = "评论ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("report_id")
    @Schema(description = "日志ID")
    private Long reportId;

    @TableField("user_id")
    @Schema(description = "评论人ID")
    private Long userId;

    @Schema(description = "评论内容")
    private String content;

    @TableField("parent_id")
    @Schema(description = "父评论ID")
    private Long parentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "评论时间")
    private LocalDateTime createTime;

    @TableField(exist = false)
    @Schema(description = "评论人姓名")
    private String userName;

    @TableField(exist = false)
    @Schema(description = "评论人头像")
    private String userAvatar;
}
