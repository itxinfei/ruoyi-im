package com.ruoyi.im.dto.mention;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @提及信息DTO
 *
 * @author ruoyi
 */
public class ImMentionInfo {

    /** 会话ID（用于权限验证和获取群成员） */
    private Long conversationId;

    /** 被@的用户ID列表 */
    private List<Long> userIds;

    /** 是否@所有人 */
    private Boolean mentionAll;

    /** @所有人类型：ALL所有人（群主/管理员权限） */
    private String mentionAllType;

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public Boolean getMentionAll() {
        return mentionAll;
    }

    public void setMentionAll(Boolean mentionAll) {
        this.mentionAll = mentionAll;
    }

    public String getMentionAllType() {
        return mentionAllType;
    }

    public void setMentionAllType(String mentionAllType) {
        this.mentionAllType = mentionAllType;
    }
}
