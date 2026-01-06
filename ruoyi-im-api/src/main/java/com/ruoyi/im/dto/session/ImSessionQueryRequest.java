package com.ruoyi.im.dto.session;

import com.ruoyi.im.common.BasePageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 浼氳瘽鏌ヨ璇锋眰DTO
 * 
 * @author ruoyi
 */
@ApiModel(description = "浼氳瘽鏌ヨ璇锋眰")
public class ImSessionQueryRequest extends BasePageRequest {

    @ApiModelProperty(value = "鐢ㄦ埛ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "鐩爣ID", example = "2")
    private Long targetId;

    @ApiModelProperty(value = "浼氳瘽绫诲瀷", example = "private")
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
