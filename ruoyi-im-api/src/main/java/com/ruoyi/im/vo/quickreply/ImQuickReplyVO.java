package com.ruoyi.im.vo.quickreply;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快捷回复VO
 *
 * @author ruoyi
 */
@Data
public class ImQuickReplyVO {

    /** 快捷回复ID */
    private Long id;

    /** 回复内容 */
    private String content;

    /** 分类 */
    private String category;

    /** 标题 */
    private String title;

    /** 排序 */
    private Integer sortOrder;

    /** 使用次数 */
    private Integer useCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
