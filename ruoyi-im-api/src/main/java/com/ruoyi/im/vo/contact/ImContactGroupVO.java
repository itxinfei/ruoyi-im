package com.ruoyi.im.vo.contact;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 联系人分组视图对象
 * 
 * @author ruoyi
 */
@ApiModel(description = "联系人分组信息")
public class ImContactGroupVO {

    @ApiModelProperty(value = "分组ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "分组名称", example = "我的好友")
    private String name;

    @ApiModelProperty(value = "分组描述", example = "我的好友分组")
    private String description;

    @ApiModelProperty(value = "分组颜色", example = "#007AFF")
    private String color;

    @ApiModelProperty(value = "排序号", example = "1")
    private Integer sortOrder;

    @ApiModelProperty(value = "成员数量", example = "10")
    private Integer memberCount;

    @ApiModelProperty(value = "是否默认分组", example = "true")
    private Boolean isDefault;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}