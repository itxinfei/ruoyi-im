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
 * URL 元数据实体
 * 用于存储链接预览信息（标题、描述、缩略图等）
 *
 * @author ruoyi
 */
@TableName("im_url_metadata")
@Data
public class ImUrlMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 元数据 ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** URL 地址 */
    @TableField("url")
    private String url;

    /** 页面标题 */
    @TableField("title")
    private String title;

    /** 页面描述 */
    @TableField("description")
    private String description;

    /** 缩略图 URL */
    @TableField("image_url")
    private String imageUrl;

    /** 网站名称 */
    @TableField("site_name")
    private String siteName;

    /** 网站图标 */
    @TableField("favicon_url")
    private String faviconUrl;

    /** 内容类型：article 文章 website 网站 video 视频 image 图片 */
    @TableField("content_type")
    private String contentType;

    /** 原始 HTML 内容（用于缓存） */
    @TableField("raw_html")
    private String rawHtml;

    /** 抓取状态：PENDING 待抓取 SUCCESS 成功 FAILED 失败 */
    @TableField("fetch_status")
    private String fetchStatus;

    /** 错误信息 */
    @TableField("error_message")
    private String errorMessage;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;
}
