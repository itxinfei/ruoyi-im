package com.ruoyi.im.dto.announcement;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告创建请求
 *
 * @author ruoyi
 */
@Data

public class ImAnnouncementCreateRequest {

    
    @NotBlank(message = "公告标题不能为空")
    private String title;

    
    private String content;

    
    private String announcementType = "SYSTEM";

    
    private Integer priority = 1;

    
    private LocalDateTime expiryTime;

    
    private Long departmentId;

    
    private String targetType = "ALL";

    
    private List<Long> targetIds;

    
    private List<AttachmentInfo> attachments;

    
    private Boolean isPinned = false;

    
    private Boolean allowComment = true;

    
    private String remark;

    /**
     * 附件信息
     */
    @Data
    
    public static class AttachmentInfo {
        
        private String name;

        
        private String url;

        
        private Long size;

        
        private String fileType;
    }
}

