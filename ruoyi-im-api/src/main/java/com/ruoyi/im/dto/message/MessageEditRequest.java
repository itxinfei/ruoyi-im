package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 编辑消息请求
 */
@Data
@Schema(description = "编辑消息请求")
public class MessageEditRequest implements Serializable {

    @NotNull(message = "消息ID不能为空")
    @Schema(description = "消息ID")
    private Long messageId;

    @NotBlank(message = "新内容不能为空")
    @Schema(description = "新的消息内容")
    private String newContent;
}
