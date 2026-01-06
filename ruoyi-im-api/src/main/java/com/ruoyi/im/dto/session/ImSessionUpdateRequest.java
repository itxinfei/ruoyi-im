package com.ruoyi.im.dto.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * 浼氳瘽鏇存柊璇锋眰DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "浼氳瘽鏇存柊璇锋眰")
public class ImSessionUpdateRequest {

    @ApiModelProperty(value = "鐢ㄦ埛ID", required = true, example = "1")
    @NotNull(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    private Long userId;

    @ApiModelProperty(value = "鐩爣ID", required = true, example = "2")
    @NotNull(message = "鐩爣ID涓嶈兘涓虹┖")
    private Long targetId;

    @ApiModelProperty(value = "浼氳瘽绫诲瀷", required = true, example = "private")
    @NotNull(message = "浼氳瘽绫诲瀷涓嶈兘涓虹┖")
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
