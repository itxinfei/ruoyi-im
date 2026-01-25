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
 * 企业云盘文件分享实体
 *
 * @author ruoyi
 */
@TableName("im_cloud_file_share")
@Data
@Schema(description = "文件分享")
public class ImCloudFileShare implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分享ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "分享ID")
    private Long id;

    /**
     * 分享类型：FILE文件, FOLDER文件夹
     */
    @Schema(description = "分享类型")
    @TableField("share_type")
    private String shareType;

    /**
     * 文件或文件夹ID
     */
    @Schema(description = "资源ID")
    @TableField("resource_id")
    private Long resourceId;

    /**
     * 分享者ID
     */
    @Schema(description = "分享者ID")
    @TableField("sharer_id")
    private Long sharerId;

    /**
     * 分享码（唯一）
     */
    @Schema(description = "分享码")
    @TableField("share_code")
    private String shareCode;

    /**
     * 分享链接
     */
    @Schema(description = "分享链接")
    @TableField("share_link")
    private String shareLink;

    /**
     * 访问密码
     */
    @Schema(description = "访问密码")
    @TableField("access_password")
    private String accessPassword;

    /**
     * 有效期天数，0表示永久
     */
    @Schema(description = "有效期天数")
    @TableField("expire_days")
    private Integer expireDays;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 允许下载：true允许, false禁止
     */
    @Schema(description = "允许下载")
    @TableField("allow_download")
    private Boolean allowDownload;

    /**
     * 允许预览：true允许, false禁止
     */
    @Schema(description = "允许预览")
    @TableField("allow_preview")
    private Boolean allowPreview;

    /**
     * 访问次数
     */
    @Schema(description = "访问次数")
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 下载次数
     */
    @Schema(description = "下载次数")
    @TableField("download_count")
    private Integer downloadCount;

    /**
     * 状态：ACTIVE有效, EXPIRED已过期, CANCELLED已取消
     */
    @Schema(description = "状态")
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
