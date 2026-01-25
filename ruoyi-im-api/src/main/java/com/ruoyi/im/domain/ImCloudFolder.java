package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业云盘文件夹实体
 *
 * @author ruoyi
 */
@TableName("im_cloud_folder")
@Data
@Schema(description = "企业云盘文件夹")
public class ImCloudFolder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件夹ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "文件夹ID")
    private Long id;

    /**
     * 文件夹名称
     */
    @Schema(description = "文件夹名称")
    @TableField("folder_name")
    private String folderName;

    /**
     * 父文件夹ID，0表示根目录
     */
    @Schema(description = "父文件夹ID")
    @TableField("parent_id")
    private Long parentId;

    /**
     * 所有者ID
     */
    @Schema(description = "所有者ID")
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 所有者类型：USER个人, DEPARTMENT部门, COMPANY公司
     */
    @Schema(description = "所有者类型")
    @TableField("owner_type")
    private String ownerType;

    /**
     * 部门ID（当owner_type为DEPARTMENT时）
     */
    @Schema(description = "部门ID")
    @TableField("department_id")
    private Long departmentId;

    /**
     * 文件夹路径
     */
    @Schema(description = "文件夹路径")
    @TableField("folder_path")
    private String folderPath;

    /**
     * 层级深度
     */
    @Schema(description = "层级深度")
    @TableField("level")
    private Integer level;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 访问权限：PRIVATE私有, DEPARTMENT部门可见, PUBLIC公开
     */
    @Schema(description = "访问权限")
    @TableField("access_permission")
    private String accessPermission;

    /**
     * 是否已删除
     */
    @Schema(description = "是否已删除")
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    @Schema(description = "删除时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
