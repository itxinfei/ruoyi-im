package com.ruoyi.im.dto.search;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 全局搜索请求DTO
 *
 * @author ruoyi
 */
@Data

public class GlobalSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    
    private String keyword;

    /**
     * 搜索类型（可选）：message, contact, group, file, workbench, all
     * 默认为 all
     */
    
    private String searchType;

    /**
     * 是否精确匹配
     */
    
    private Boolean exactMatch;

    /**
     * 页码
     */
    
    private Integer pageNum;

    /**
     * 每页数量
     */
    
    private Integer pageSize;
}

