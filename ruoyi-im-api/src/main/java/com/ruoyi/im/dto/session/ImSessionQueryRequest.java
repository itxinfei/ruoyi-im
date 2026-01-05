package com.ruoyi.im.dto.session;

import com.ruoyi.im.common.BasePageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 会话查询请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "会话查询请求")
public class ImSessionQueryRequest extends BasePageRequest {

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "目标ID", example = "2")
    private Long targetId;

    @ApiModelProperty(value = "会话类型", example = "private")
    private String sessionType;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }
}