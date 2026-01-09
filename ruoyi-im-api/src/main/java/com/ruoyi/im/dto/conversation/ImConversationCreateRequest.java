package com.ruoyi.im.dto.conversation;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 会话创建请求
 *
 * @author ruoyi
 */
@Data
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
}