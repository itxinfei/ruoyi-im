package com.ruoyi.im.dto.message;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 编辑消息请求
 */
@Data

public class MessageEditRequest implements Serializable {

    @NotNull(message = "消息ID不能为空")
    
    private Long messageId;

    @NotBlank(message = "新内容不能为空")
    
    private String newContent;
}

