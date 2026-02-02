package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体
 *
 * @author ruoyi
 */
@Data
@TableName("im_role")
@Schema(description = "角色实体")
public class ImRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "角色ID")
    private Long id;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色编码（唯一标识）
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
     * 是否系统内置角色：0=否 1=是
     */
    @Schema(description = "是否系统内置")
    private Integer builtin;

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
     * 角色颜色（前端展示）
     */
    @Schema(description = "角色颜色")
    private String color;

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

    /**
     * 删除标志：0=正常 1=删除
     */
    @Schema(description = "删除标志")
    private Integer delFlag;

    // 非数据库字段

    /**
     * 成员数量（非数据库字段）
     */
    @Schema(description = "成员数量")
    @TableField(exist = false)
    private Integer memberCount;
}
