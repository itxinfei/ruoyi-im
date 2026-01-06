package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件分享记录实体
 * 记录用户分享的文件信息和访问权限
 *
 * @author ruoyi
 */
@TableName("im_file_share")
@Data
@Schema(description = "文件分享记录")
public class ImFileShare implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "分享ID")
    private Long id;

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "分享者ID")
    private Long sharerId;

    @Schema(description = "接收者ID，多个接收者用逗号分隔")
    private String receiverIds;

    @Schema(description = "分享权限: 1=公开, 2=指定人可见, 3=密码保护")
    private Integer permission;

    @Schema(description = "访问密码")
    private String accessPassword;

    @Schema(description = "允许下载: 0=否, 1=是")
    private Integer allowDownload;

    @Schema(description = "允许预览: 0=否, 1=是")
    private Integer allowPreview;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "访问次数")
    private Integer accessCount;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
