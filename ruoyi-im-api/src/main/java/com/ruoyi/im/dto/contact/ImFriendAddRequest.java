package com.ruoyi.im.dto.contact;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 添加好友请求
 *
 * @author ruoyi
 */
public class ImFriendAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标用户ID
     */
    private Long targetUserId;

    /**
     * 申请消息
     */
    @Size(max = 200, message = "申请消息长度不能超过200个字符")
    private String message;

    /**
     * 好友分组
     */
    @Size(max = 20, message = "分组名称长度不能超过20个字符")
    private String groupName;

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
