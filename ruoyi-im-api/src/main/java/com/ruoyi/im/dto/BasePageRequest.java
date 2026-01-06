package com.ruoyi.im.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 分页查询基础请求对象
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "分页查询基础请求")
public class BasePageRequest {
    
    @ApiModelProperty(value = "页码，从1开始", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;
    
    @ApiModelProperty(value = "每页数量，最大100", example = "20")
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 20;

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
}
