package com.ruoyi.im.dto.conversation;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 群聊会话创建请求
 *
 * @author ruoyi
 */
@Data
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
}