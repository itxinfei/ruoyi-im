package com.ruoyi.im.dto.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 文件上传请求DTO
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "文件上传请求")
public class ImFileUploadRequest {

    @ApiModelProperty(value = "上传者用户ID", required = true, example = "1")
    @NotNull(message = "上传者用户ID不能为空")
    @Positive(message = "上传者用户ID必须为正数")
    private Long uploaderId;

    @ApiModelProperty(value = "文件描述", example = "用户头像图片")
    private String description;

    @ApiModelProperty(value = "文件分类", example = "avatar")
    private String category;

    @ApiModelProperty(value = "标签", example = "头像,个人资料")
    private String tags;

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}