package com.ruoyi.im.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 群组权限检查VO
 *
 * @author ruoyi
 */
@Data
@Builder
@Schema(description = "群组权限检查结果")
public class GroupPermissionCheckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否有权限")
    private Boolean hasPermission;

    @Schema(description = "检查的权限名称")
    private String permission;
}
