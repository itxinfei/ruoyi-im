package com.ruoyi.im.dto.mention;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @提及信息DTO
 *
 * @author ruoyi
 */
@Data
public class ImMentionInfo {

    /** 会话ID（用于权限验证和获取群成员） */
    private Long conversationId;

    /** 被@的用户ID列表 */
    private List<Long> userIds;

    /** 是否@所有人 */
    private Boolean mentionAll;

    /** @所有人类型：ALL所有人（群主/管理员权限） */
    private String mentionAllType;
}
