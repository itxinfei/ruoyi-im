package com.ruoyi.im.vo.announcement;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告视图对象
 *
 * @author ruoyi
 */
@Data
@Schema(description = "公告视图对象")
public class ImAnnouncementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告类型")
    private String announcementType;

    @Schema(description = "公告类型显示名称")
    private String announcementTypeDisplay;

    @Schema(description = "优先级（1=普通 2=重要 3=紧急）")
    private Integer priority;

    @Schema(description = "优先级显示名称")
    private String priorityDisplay;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态显示名称")
    private String statusDisplay;

    @Schema(description = "发布人ID")
    private Long publisherId;

    @Schema(description = "发布人姓名")
    private String publisherName;

    @Schema(description = "发布人头像")
    private String publisherAvatar;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryTime;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "是否允许评论")
    private Boolean allowComment;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "已读人数")
    private Integer readCount;

    @Schema(description = "目标总人数")
    private Integer totalTargetCount;

    @Schema(description = "阅读进度百分比")
    private Integer readPercent;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "是否已过期")
    private Boolean isExpired;
}
