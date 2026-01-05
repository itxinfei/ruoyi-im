package com.ruoyi.im.dto.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * 会话更新请求DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "会话更新请求")
public class ImSessionUpdateRequest {

    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "目标ID", required = true, example = "2")
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    @ApiModelProperty(value = "会话类型", required = true, example = "private")
    @NotNull(message = "会话类型不能为空")
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