package com.ruoyi.im.dto.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 任务查询请求
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)

public class ImTaskQueryRequest extends PageRequest {

    
    private String keyword;

    
    private Long projectId;

    
    private Long creatorId;

    
    private Long assigneeId;

    
    private Long departmentId;

    
    private Integer priority;

    
    private String status;

    
    private String taskType;

    
    private Long parentId;

    
    private String tag;

    
    private LocalDate startDateBegin;

    
    private LocalDate startDateEnd;

    
    private LocalDate dueDateBegin;

    
    private LocalDate dueDateEnd;

    
    private Boolean overdueOnly = false;

    
    private Boolean myCreatedOnly = false;

    
    private Boolean myAssignedOnly = false;

    
    private Boolean myFollowedOnly = false;

    
    private String orderBy = "createTime";

    
    private String orderDirection = "DESC";
}

