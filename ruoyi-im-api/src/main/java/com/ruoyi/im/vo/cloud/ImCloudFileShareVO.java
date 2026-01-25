package com.ruoyi.im.vo.cloud;

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
@Schema(description = "文件分享")
public class ImCloudFileShareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分享ID")
    private Long id;

    @Schema(description = "分享类型")
    private String shareType;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "分享者ID")
    private Long sharerId;

    @Schema(description = "分享者名称")
    private String sharerName;

    @Schema(description = "分享链接")
    private String shareLink;

    @Schema(description = "是否有密码")
    private Boolean hasPassword;

    @Schema(description = "有效期天数")
    private Integer expireDays;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "是否已过期")
    private Boolean isExpired;

    @Schema(description = "是否允许下载")
    private Boolean allowDownload;

    @Schema(description = "是否允许预览")
    private Boolean allowPreview;

    @Schema(description = "访问次数")
    private Integer viewCount;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
