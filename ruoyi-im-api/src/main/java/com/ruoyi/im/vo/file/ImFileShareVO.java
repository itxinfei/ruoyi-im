package com.ruoyi.im.vo.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件分享VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文件分享信息")
public class ImFileShareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分享ID")
    private Long id;

    @Schema(description = "分享链接")
    private String shareUrl;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "分享者ID")
    private Long sharerId;

    @Schema(description = "分享者昵称")
    private String sharerName;

    @Schema(description = "分享者头像")
    private String sharerAvatar;

    @Schema(description = "分享权限: 1=公开, 2=指定人, 3=密码保护")
    private Integer permission;

    @Schema(description = "访问次数")
    private Integer accessCount;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "是否已过期")
    private Boolean isExpired;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}
