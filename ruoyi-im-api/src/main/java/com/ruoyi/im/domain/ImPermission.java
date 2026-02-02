package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限实体
 *
 * @author ruoyi
 */
@Data
@TableName("im_permission")
@Schema(description = "权限实体")
public class ImPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
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
     * 权限编码（唯一标识）
     */
    @Schema(description = "权限编码")
    private String permissionCode;

    /**
     * 权限类型：1=菜单 2=按钮
     */
    @Schema(description = "权限类型")
    private Integer permissionType;

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
     * 是否可见：0=隐藏 1=显示
     */
    @Schema(description = "是否可见")
    private Integer visible;

    /**
     * 状态：0=禁用 1=启用
     */
    @Schema(description = "状态")
    private Integer status;

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

    // 非数据库字段

    /**
     * 子权限列表（非数据库字段）
     */
    @Schema(description = "子权限列表")
    @TableField(exist = false)
    private List<ImPermission> children;

    /**
     * 权限类型标识（用于前端树形组件）
     */
    @Schema(description = "权限类型标识")
    @TableField(exist = false)
    private String type;
}
