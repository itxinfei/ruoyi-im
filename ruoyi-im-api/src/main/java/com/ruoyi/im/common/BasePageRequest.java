package com.ruoyi.im.common;

import io.swagger.annotations.ApiModelProperty;

/**
 * 分页请求基类
 * 
 * @author ruoyi
 */
public class BasePageRequest {
    
    @ApiModelProperty("页码")
    private Integer pageNum = 1;
    
    @ApiModelProperty("每页大小")
    private Integer pageSize = 10;
    
    @ApiModelProperty("排序字段")
    private String orderByColumn;
    
    @ApiModelProperty("排序方向")
    private String isAsc = "desc";
    
    public Integer getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public String getOrderByColumn() {
        return orderByColumn;
    }
    
    public void setOrderByColumn(String orderByColumn) {
        this.orderByColumn = orderByColumn;
    }
    
    public String getIsAsc() {
        return isAsc;
    }
    
    public void setIsAsc(String isAsc) {
        this.isAsc = isAsc;
    }
}