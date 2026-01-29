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
 * 快捷回复实体
 *
 * @author ruoyi
 */
@TableName("im_quick_reply")
@Data
public class ImQuickReply implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 快捷回复ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 回复内容 */
    private String content;

    /** 分类：greeting问候语/ending结束语/common常用语/custom自定义 */
    private String category;

    /** 标题（可选） */
    private String title;

    /** 排序 */
    @TableField("sort_order")
    private Integer sortOrder;

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
