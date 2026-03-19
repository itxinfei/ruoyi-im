package com.ruoyi.im.vo.announcement;

import com.fasterxml.jackson.annotation.JsonFormat;
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

public class ImAnnouncementDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String title;

    
    private String content;

    
    private String announcementType;

    
    private String announcementTypeDisplay;

    
    private Integer priority;

    
    private String priorityDisplay;

    
    private String status;

    
    private String statusDisplay;

    
    private Long publisherId;

    
    private String publisherName;

    
    private String publisherAvatar;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryTime;

    
    private Long departmentId;

    
    private String departmentName;

    
    private String targetType;

    
    private String targetTypeDisplay;

    
    private List<String> targetNames;

    
    private Boolean isPinned;

    
    private Boolean allowComment;

    
    private Integer viewCount;

    
    private Integer likeCount;

    
    private Integer commentCount;

    
    private Integer readCount;

    
    private Integer totalTargetCount;

    
    private Integer readPercent;

    
    private List<AnnouncementAttachment> attachments;

    
    private List<AnnouncementComment> comments;

    
    private List<ReadUser> readUsers;

    
    private List<LikedUser> likedUsers;

    
    private String remark;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    
    private Boolean isExpired;

    /**
     * 公告附件
     */
    @Data
    
    public static class AnnouncementAttachment implements Serializable {
        
        private Long id;

        
        private String name;

        
        private String url;

        
        private Long size;

        
        private String sizeDisplay;

        
        private String fileType;

        
        private Boolean isImage;
    }

    /**
     * 公告评论
     */
    @Data
    
    public static class AnnouncementComment implements Serializable {
        
        private Long id;

        
        private String content;

        
        private Long commentatorId;

        
        private String commentatorName;

        
        private String commentatorAvatar;

        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime commentTime;
    }

    /**
     * 已读用户
     */
    @Data
    
    public static class ReadUser implements Serializable {
        
        private Long userId;

        
        private String userName;

        
        private String userAvatar;

        
        private String deptName;

        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime readTime;
    }

    /**
     * 点赞用户
     */
    @Data
    
    public static class LikedUser implements Serializable {
        
        private Long userId;

        
        private String userName;

        
        private String userAvatar;

        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime likeTime;
    }
}

