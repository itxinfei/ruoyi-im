package com.ruoyi.im.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基础类
 *
 * @author ruoyi
 */
@Data
public class BasePageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向 asc/desc
     */
    private String orderByDirection = "asc";

    /**
     * 搜索关键词
     */
    private String keyword;
}
