package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件附件实体
 *
 * @author ruoyi
 */
@TableName("im_email_attachment")
@Data
public class ImEmailAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 附件ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 邮件ID */
    @TableField("email_id")
    private Long emailId;

    /** 文件名 */
    @TableField("file_name")
    private String fileName;

    /** 文件大小(字节) */
    @TableField("file_size")
    private Long fileSize;

    /** 文件类型(MIME) */
    @TableField("file_type")
    private String fileType;

    /** 文件存储路径 */
    @TableField("file_path")
    private String filePath;

    /** 文件访问URL */
    @TableField("file_url")
    private String fileUrl;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /** 上传者ID */
    @TableField("uploader_id")
    private Long uploaderId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
