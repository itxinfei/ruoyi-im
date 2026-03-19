package com.ruoyi.im.dto.message;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 消息撤回请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImMessageRecallRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @NotNull(message = "消息ID不能为空")
    private Long messageId;
}

