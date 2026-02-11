package com.ruoyi.im.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 邮件标记已读请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "邮件标记已读请求")
public class EmailMarkReadRequest {

    @NotNull(message = "邮件ID列表不能为空")
    @Schema(description = "邮件ID列表", required = true)
    private List<Long> ids;
}