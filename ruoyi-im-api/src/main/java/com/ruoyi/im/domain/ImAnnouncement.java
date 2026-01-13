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
 * 公告实体类
 *
 * @author ruoyi
 */
@TableName("im_announcement")
@Data
@Schema(description = "公告")
public class ImAnnouncement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "公告ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    @Schema(description = "公告标题")
    private String title;

    @TableField("content")
    @Schema(description = "公告内容")
    private String content;

    @TableField("announcement_type")
    @Schema(description = "公告类型（SYSTEM系统 DEPARTMENT部门 PROJECT项目）")
    private String announcementType;

    @TableField("priority")
    @Schema(description = "优先级（1=普通 2=重要 3=紧急）")
    private Integer priority;

    @TableField("status")
    @Schema(description = "状态（DRAFT草稿 PUBLISHED已发布 EXPIRED已过期 WITHDRAWN已撤回）")
    private String status;

    @TableField("publisher_id")
    @Schema(description = "发布人ID")
    private Long publisherId;

    @TableField("publish_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @TableField("expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "过期时间")
    private LocalDateTime expiryTime;

    @TableField("department_id")
    @Schema(description = "部门ID（部门公告时使用）")
    private Long departmentId;

    @TableField("target_type")
    @Schema(description = "目标范围（ALL全部 DEPARTMENT部门指定 ROLE角色指定 USER用户指定）")
    private String targetType;

    @TableField("target_ids")
    @Schema(description = "目标ID列表（JSON格式，存储部门ID、角色ID或用户ID）")
    private String targetIds;

    @TableField("attachments")
    @Schema(description = "附件（JSON格式）")
    private String attachments;

    @TableField("is_pinned")
    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @TableField("allow_comment")
    @Schema(description = "是否允许评论")
    private Boolean allowComment;

    @TableField("view_count")
    @Schema(description = "浏览次数")
    private Integer viewCount;

    @TableField("like_count")
    @Schema(description = "点赞数")
    private Integer likeCount;

    @TableField("comment_count")
    @Schema(description = "评论数")
    private Integer commentCount;

    @TableField("read_count")
    @Schema(description = "已读人数")
    private Integer readCount;

    @TableField("total_target_count")
    @Schema(description = "目标总人数")
    private Integer totalTargetCount;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @Schema(description = "发布人姓名")
    private String publisherName;

    @TableField(exist = false)
    @Schema(description = "发布人头像")
    private String publisherAvatar;

    @TableField(exist = false)
    @Schema(description = "部门名称")
    private String departmentName;

    @TableField(exist = false)
    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @TableField(exist = false)
    @Schema(description = "是否已读")
    private Boolean isRead;
}
