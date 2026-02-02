package com.ruoyi.im.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 角色创建请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "角色创建请求")
public class ImRoleCreateRequest {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    @Schema(description = "角色编码")
    private String roleCode;

    /**
     * 角色描述
     */
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    @Schema(description = "角色描述")
    private String description;

    /**
     * 数据范围：1=全部 2=本部门 3=本部门及子部门 4=仅本人
     */
    @NotNull(message = "数据范围不能为空")
    @Schema(description = "数据范围")
    private Integer dataScope;

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

    /**
     * 权限ID列表
     */
    @Schema(description = "权限ID列表")
    private String permissionIds;
}
