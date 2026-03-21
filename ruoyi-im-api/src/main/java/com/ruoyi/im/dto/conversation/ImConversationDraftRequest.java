package com.ruoyi.im.dto.conversation;

import lombok.Data;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 会话草稿更新请求
 *
 * @author ruoyi
 */
@Data
public class ImConversationDraftRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 草稿内容
     * 为空表示清除草稿
     */
    @Size(max = 1000, message = "草稿内容不能超过1000个字符")
    private String draft;
}
