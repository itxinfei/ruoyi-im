package com.ruoyi.im.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 基础分页请求
 * 所有分页查询请求的基类
 *
 * @author ruoyi
 */
@Schema(description = "基础分页请求")
@Validated
public class BasePageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页条数
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最小页码
     */
    private static final int MIN_PAGE_NUM = 1;

    /**
     * 最大页码
     */
    private static final int MAX_PAGE_NUM = 10000;

    /**
     * 最小每页条数
     */
    private static final int MIN_PAGE_SIZE = 1;

    /**
     * 最大每页条数
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 页码，从1开始
     */
    @Schema(description = "页码，从1开始", defaultValue = "1", example = "1")
    @Min(value = MIN_PAGE_NUM, message = "页码最小为1")
    @Max(value = MAX_PAGE_NUM, message = "页码最大为10000")
    private Integer pageNum = DEFAULT_PAGE_NUM;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", defaultValue = "20", example = "20")
    @Min(value = MIN_PAGE_SIZE, message = "每页最少1条")
    @Max(value = MAX_PAGE_SIZE, message = "每页最多100条")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "createTime")
    private String orderBy;

    /**
     * 排序方向：asc/desc
     */
    @Schema(description = "排序方向：asc/desc", example = "desc")
    private String orderDirection;

    /**
     * 获取页码
     */
    public Integer getPageNum() {
        return pageNum;
    }

    /**
     * 设置页码
     */
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 获取每页条数
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页条数
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取排序字段
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * 设置排序字段
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * 获取排序方向
     */
    public String getOrderDirection() {
        return orderDirection;
    }

    /**
     * 设置排序方向
     */
    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }

    /**
     * 计算偏移量
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 验证分页参数
     */
    public void validate() {
        if (pageNum == null || pageNum < MIN_PAGE_NUM) {
            this.pageNum = DEFAULT_PAGE_NUM;
        }
        if (pageSize == null || pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        }
    }
}
