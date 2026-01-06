package com.ruoyi.im.dto.conversation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 会话创建请求
 *
 * @author ruoyi
 */
public class ImConversationCreateRequest {

    /**
     * 会话类型：PRIVATE(单聊)、GROUP(群聊)
     */
    @NotBlank(message = "会话类型不能为空")
    private String type;

    /**
     * 目标ID：单聊为目标用户ID，群聊为群组ID
     */
    private Long targetId;

    /**
     * 群组名称（群聊时使用）
     */
    private String groupName;

    /**
     * 群组头像（群聊时使用）
     */
    private String groupAvatar;

    /**
     * 群组成员ID列表（群聊时使用）
     */
    private List<Long> memberIds;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupAvatar() {
        return groupAvatar;
    }

    public void setGroupAvatar(String groupAvatar) {
        this.groupAvatar = groupAvatar;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}