package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群公告已读状态实体
 *
 * 用于记录群组成员对公告的已读状态
 *
 * @author ruoyi
 */
@TableName("im_group_announcement_read")
@Data
public class ImGroupAnnouncementRead implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群组ID
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 公告ID
     */
    @TableField("announcement_id")
    private Long announcementId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 已读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
