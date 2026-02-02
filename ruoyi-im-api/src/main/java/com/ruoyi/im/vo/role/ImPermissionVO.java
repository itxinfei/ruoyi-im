package com.ruoyi.im.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 权限视图对象
 *
 * @author ruoyi
 */
@Data
@Schema(description = "权限视图对象")
public class ImPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @Schema(description = "权限ID")
    private Long id;

    /**
     * 父权限ID
     */
    @Schema(description = "父权限ID")
    private Long parentId;

    /**
     * 权限名称
     */
    @Schema(description = "权限名称")
    private String permissionName;

    /**
     * 权限编码
     */
    @Schema(description = "权限编码")
    private String permissionCode;

    /**
     * 权限类型：1=菜单 2=按钮
     */
    @Schema(description = "权限类型")
    private Integer permissionType;

    /**
     * 权限类型名称
     */
    @Schema(description = "权限类型名称")
    private String permissionTypeName;

    /**
     * 路由路径
     */
    @Schema(description = "路由路径")
    private String routePath;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径")
    private String component;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sortOrder;

    /**
     * 是否可见
     */
    @Schema(description = "是否可见")
    private Boolean visible;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 子权限列表
     */
    @Schema(description = "子权限列表")
    private List<ImPermissionVO> children;

    /**
     * 权限类型标识（用于前端树形组件）
     */
    @Schema(description = "权限类型标识")
    private String type;
}
