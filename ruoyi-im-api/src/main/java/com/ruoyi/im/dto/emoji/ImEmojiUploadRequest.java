package com.ruoyi.im.dto.emoji;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 上传自定义表情请求DTO
 *
 * @author ruoyi
 */
@Data
public class ImEmojiUploadRequest {

    /** 表情编码 */
    @NotBlank(message = "表情编码不能为空")
    private String emojiCode;

    /** 表情名称 */
    private String emojiName;

    /** 表情图片URL */
    @NotBlank(message = "表情图片不能为空")
    private String emojiUrl;

    /** 图片宽度 */
    private Integer width;

    /** 图片高度 */
    private Integer height;

    /** 是否公开：0私有 1公开 */
    private Boolean isPublic;
}
