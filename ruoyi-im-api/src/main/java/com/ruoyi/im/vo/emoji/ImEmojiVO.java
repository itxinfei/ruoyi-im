package com.ruoyi.im.vo.emoji;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自定义表情包VO
 *
 * @author ruoyi
 */
@Data
public class ImEmojiVO {

    /** 表情ID */
    private Long id;

    /** 表情编码 */
    private String emojiCode;

    /** 表情图片URL */
    private String emojiUrl;

    /** 表情名称 */
    private String emojiName;

    /** 分类 */
    private String category;

    /** 上传者ID */
    private Long uploaderId;

    /** 上传者名称 */
    private String uploaderName;

    /** 图片宽度 */
    private Integer width;

    /** 图片高度 */
    private Integer height;

    /** 是否公开 */
    private Boolean isPublic;

    /** 是否当前用户上传 */
    private Boolean isOwn;

    /** 使用次数 */
    private Integer useCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
