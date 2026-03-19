package com.ruoyi.im.dto.group;

import lombok.Data;

import java.io.Serializable;

/**
 * 群组文件查询请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImGroupFileQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long groupId;

    
    private String category;

    
    private String fileType;

    
    private Long uploaderId;

    
    private String keyword;

    
    private Integer pageNum = 1;

    
    private Integer pageSize = 20;
}

