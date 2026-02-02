package com.ruoyi.im.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 角色更新请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "角色更新请求")
public class ImRoleUpdateRequest {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long id;

    /**
     * 角色名称
     */
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色描述
     */
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    @Schema(description = "角色描述")
    private String description;

    /**
     * 数据范围：1=全部 2=本部门 3=本部门及子部门 4=仅本人
     */
    @Schema(description = "数据范围")
    private Integer dataScope;

    /**
     * 状态：0=禁用 1=启用
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sortOrder;

    /**
     * 角色颜色
     */
    @Size(max = 20, message = "角色颜色长度不能超过20个字符")
    @Schema(description = "角色颜色")
    private String color;
}
