package com.ruoyi.im.vo.workreport;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作日志评论VO
 */
@Data

public class WorkReportCommentVO implements Serializable {

    
    private Long id;

    
    private Long reportId;

    
    private Long userId;

    
    private String userName;

    
    private String userAvatar;

    
    private String content;

    
    private Long parentId;

    
    private String parentContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime createTime;

    
    private List<WorkReportCommentVO> children;
}

