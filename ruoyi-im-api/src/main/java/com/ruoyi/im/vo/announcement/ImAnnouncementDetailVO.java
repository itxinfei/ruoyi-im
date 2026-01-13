package com.ruoyi.im.vo.announcement;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告详情视图对象
 *
 * @author ruoyi
 */
@Data
@Schema(description = "公告详情视图对象")
public class ImAnnouncementDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "公告类型")
    private String announcementType;

    @Schema(description = "公告类型显示名称")
    private String announcementTypeDisplay;

    @Schema(description = "优先级")
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

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "目标范围")
    private String targetType;

    @Schema(description = "目标范围显示名称")
    private String targetTypeDisplay;

    @Schema(description = "目标名称列表")
    private List<String> targetNames;

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

    @Schema(description = "附件列表")
    private List<AnnouncementAttachment> attachments;

    @Schema(description = "评论列表")
    private List<AnnouncementComment> comments;

    @Schema(description = "已读用户列表")
    private List<ReadUser> readUsers;

    @Schema(description = "点赞用户列表")
    private List<LikedUser> likedUsers;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "是否已过期")
    private Boolean isExpired;

    /**
     * 公告附件
     */
    @Data
    @Schema(description = "公告附件")
    public static class AnnouncementAttachment implements Serializable {
        @Schema(description = "附件ID")
        private Long id;

        @Schema(description = "文件名")
        private String name;

        @Schema(description = "文件URL")
        private String url;

        @Schema(description = "文件大小")
        private Long size;

        @Schema(description = "文件大小显示")
        private String sizeDisplay;

        @Schema(description = "文件类型")
        private String fileType;

        @Schema(description = "是否为图片")
        private Boolean isImage;
    }

    /**
     * 公告评论
     */
    @Data
    @Schema(description = "公告评论")
    public static class AnnouncementComment implements Serializable {
        @Schema(description = "评论ID")
        private Long id;

        @Schema(description = "评论内容")
        private String content;

        @Schema(description = "评论人ID")
        private Long commentatorId;

        @Schema(description = "评论人姓名")
        private String commentatorName;

        @Schema(description = "评论人头像")
        private String commentatorAvatar;

        @Schema(description = "评论时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime commentTime;
    }

    /**
     * 已读用户
     */
    @Data
    @Schema(description = "已读用户")
    public static class ReadUser implements Serializable {
        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户姓名")
        private String userName;

        @Schema(description = "用户头像")
        private String userAvatar;

        @Schema(description = "部门名称")
        private String deptName;

        @Schema(description = "阅读时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime readTime;
    }

    /**
     * 点赞用户
     */
    @Data
    @Schema(description = "点赞用户")
    public static class LikedUser implements Serializable {
        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户姓名")
        private String userName;

        @Schema(description = "用户头像")
        private String userAvatar;

        @Schema(description = "点赞时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime likeTime;
    }
}
