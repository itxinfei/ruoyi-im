package com.ruoyi.im.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量删除消息请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "批量删除消息请求")
public class MessagesBatchDeleteRequest {

    @NotEmpty(message = "消息ID列表不能为空")
    @Schema(description = "消息ID列表", required = true)
    private List<Long> messageIds;
}