package com.ruoyi.im.vo.workreport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.im.vo.workreport.WorkReportCommentVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作日志详情VO
 */
@Data

public class WorkReportDetailVO implements Serializable {

    
    private Long id;

    
    private Long userId;

    
    private String userName;

    
    private String userAvatar;

    
    private String deptName;

    
    private String reportType;

    
    private String reportTypeName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    
    private LocalDate reportDate;

    
    private String workContent;

    
    private String completionStatus;

    
    private String completionStatusName;

    
    private String tomorrowPlan;

    
    private String issues;

    
    private List<String> attachmentList;

    
    private BigDecimal workHours;

    
    private String visibility;

    
    private String visibilityName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime submitTime;

    
    private String status;

    
    private String statusName;

    
    private Long approverId;

    
    private String approverName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    
    private LocalDateTime approveTime;

    
    private String approveRemark;

    
    private Integer commentCount;

    
    private Integer likeCount;

    
    private Boolean isLiked;

    
    private List<WorkReportCommentVO> comments;

    
    private List<WorkReportLikeUserVO> likeUsers;
}

