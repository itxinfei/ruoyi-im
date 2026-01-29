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
 * 自定义表情包实体
 *
 * @author ruoyi
 */
@TableName("im_emoji")
@Data
public class ImEmoji implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 表情ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 表情编码（用于快捷输入） */
    @TableField("emoji_code")
    private String emojiCode;

    /** 表情图片URL */
    private String emojiUrl;

    /** 表情名称 */
    @TableField("emoji_name")
    private String emojiName;

    /** 分类：system系统/custom自定义 */
    private String category;

    /** 上传者ID */
    @TableField("uploader_id")
    private Long uploaderId;

    /** 图片宽度 */
    private Integer width;

    /** 图片高度 */
    private Integer height;

    /** 是否公开：0私有 1公开 */
    @TableField("is_public")
    private Boolean isPublic;

    /** 分享次数 */
    @TableField("share_count")
    private Integer shareCount;

    /** 使用次数 */
    @TableField("use_count")
    private Integer useCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
