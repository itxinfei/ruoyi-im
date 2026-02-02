package com.ruoyi.im.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色视图对象
 *
 * @author ruoyi
 */
@Data
@Schema(description = "角色视图对象")
public class ImRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private Long id;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    private String roleCode;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述")
    private String description;

    /**
     * 数据范围：1=全部 2=本部门 3=本部门及子部门 4=仅本人
     */
    @Schema(description = "数据范围")
    private Integer dataScope;

    /**
     * 数据范围描述
     */
    @Schema(description = "数据范围描述")
    private String dataScopeDesc;

    /**
     * 是否系统内置
     */
    @Schema(description = "是否系统内置")
    private Boolean builtin;

    /**
     * 状态
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
    @Schema(description = "角色颜色")
    private String color;

    /**
     * 成员数量
     */
    @Schema(description = "成员数量")
    private Integer memberCount;

    /**
     * 权限ID列表
     */
    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
