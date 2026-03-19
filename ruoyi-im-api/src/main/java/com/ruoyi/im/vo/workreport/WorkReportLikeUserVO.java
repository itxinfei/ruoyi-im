package com.ruoyi.im.vo.workreport;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作日志点赞用户VO
 */
@Data

public class WorkReportLikeUserVO implements Serializable {

    
    private Long userId;

    
    private String userName;

    
    private String userAvatar;
}

