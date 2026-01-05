package com.ruoyi.im.dto.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * 好友查询请求
 * 
 * @author ruoyi
 */
@ApiModel(description = "好友查询请求")
public class ImFriendQueryRequest {

    @ApiModelProperty(value = "搜索关键词", example = "张三")
    @Size(max = 50, message = "搜索关键词长度不能超过50个字符")
    private String keyword;

    @ApiModelProperty(value = "页码", example = "1")
    @Positive(message = "页码必须为正数")
    private Integer pageNum;

    @ApiModelProperty(value = "页面大小", example = "20")
    @Positive(message = "页面大小必须为正数")
    private Integer pageSize;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

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