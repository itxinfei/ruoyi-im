package com.ruoyi.im.dto.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 文件查询请求DTO
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
@ApiModel(description = "文件查询请求")
public class ImFileQueryRequest {

    @ApiModelProperty(value = "文件名称关键词", example = "avatar")
    private String fileName;

    @ApiModelProperty(value = "文件类型", example = "image")
    private String fileType;

    @ApiModelProperty(value = "文件分类", example = "avatar")
    private String category;

    @ApiModelProperty(value = "上传者用户ID", example = "1")
    private Long uploaderId;

    @ApiModelProperty(value = "开始时间", example = "2024-01-01 00:00:00")
    private String startTime;

    @ApiModelProperty(value = "结束时间", example = "2024-12-31 23:59:59")
    private String endTime;

    @ApiModelProperty(value = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能大于100")
    private Integer pageSize = 10;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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