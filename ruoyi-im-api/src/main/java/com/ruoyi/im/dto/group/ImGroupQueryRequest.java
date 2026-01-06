package com.ruoyi.im.dto.group;

import com.ruoyi.im.dto.BasePageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 群组查询请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "群组查询请求")
public class ImGroupQueryRequest extends BasePageRequest {

    @ApiModelProperty(value = "群组名称", example = "技术")
    private String groupName;

    @ApiModelProperty(value = "群主名称", example = "张三")
    private String ownerName;

    @ApiModelProperty(value = "群组状态", example = "ACTIVE")
    private String status;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}