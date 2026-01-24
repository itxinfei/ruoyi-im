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
 * 任务附件实体类
 *
 * @author ruoyi
 */
@TableName("im_task_attachment")
@Data
@Schema(description = "任务附件")
public class ImTaskAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "附件ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    @Schema(description = "任务ID")
    private Long taskId;

    @TableField("file_name")
    @Schema(description = "文件名")
    private String fileName;

    @TableField("file_url")
    @Schema(description = "文件URL")
    private String fileUrl;

    @TableField("file_size")
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @TableField("file_type")
    @Schema(description = "文件类型/MIME")
    private String fileType;

    @TableField("upload_user_id")
    @Schema(description = "上传用户ID")
    private Long uploadUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("upload_time")
    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    @TableField(exist = false)
    @Schema(description = "上传用户昵称")
    private String uploaderName;

    @TableField(exist = false)
    @Schema(description = "上传用户头像")
    private String uploaderAvatar;
}
