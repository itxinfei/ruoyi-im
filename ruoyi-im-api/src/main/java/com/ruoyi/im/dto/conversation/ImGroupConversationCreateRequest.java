package com.ruoyi.im.dto.conversation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 群聊会话创建请求
 *
 * @author ruoyi
 */
public class ImGroupConversationCreateRequest {

    /**
     * 群组名称
     */
    @NotBlank(message = "群组名称不能为空")
    private String groupName;

    /**
     * 群组头像
     */
    private String groupAvatar;

    /**
     * 群组成员ID列表
     */
    @NotNull(message = "群组成员不能为空")
    private List<Long> memberIds;

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