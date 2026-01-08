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
 * 工作日志点赞实体类
 */
@TableName("im_work_report_like")
@Data
@Schema(description = "工作日志点赞")
public class ImWorkReportLike implements Serializable {

    @Schema(description = "点赞ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("report_id")
    @Schema(description = "日志ID")
    private Long reportId;

    @TableField("user_id")
    @Schema(description = "点赞人ID")
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "点赞时间")
    private LocalDateTime createTime;
}
