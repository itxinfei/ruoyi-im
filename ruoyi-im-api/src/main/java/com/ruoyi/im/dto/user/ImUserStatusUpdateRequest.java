package com.ruoyi.im.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 用户状态更新请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "用户状态更新请求")
public class ImUserStatusUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    /**
     * 用户状态
     */
    @Schema(description = "用户状态（ENABLED-启用 DISABLED-禁用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "ENABLED")
    @NotBlank(message = "用户状态不能为空")
    @Pattern(regexp = "^(ENABLED|DISABLED)$", message = "用户状态只能是ENABLED或DISABLED")
    private String status;
}
