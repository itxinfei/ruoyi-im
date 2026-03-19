package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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

public class ImCloudFolder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件夹ID
     */
    @TableId(type = IdType.AUTO)
    
    private Long id;

    /**
     * 文件夹名称
     */
    
    @TableField("folder_name")
    private String folderName;

    /**
     * 父文件夹ID，0表示根目录
     */
    
    @TableField("parent_id")
    private Long parentId;

    /**
     * 所有者ID
     */
    
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 所有者类型：USER个人, DEPARTMENT部门, COMPANY公司
     */
    
    @TableField("owner_type")
    private String ownerType;

    /**
     * 部门ID（当owner_type为DEPARTMENT时）
     */
    
    @TableField("department_id")
    private Long departmentId;

    /**
     * 文件夹路径
     */
    
    @TableField("folder_path")
    private String folderPath;

    /**
     * 层级深度
     */
    
    @TableField("level")
    private Integer level;

    /**
     * 排序号
     */
    
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 访问权限：PRIVATE私有, DEPARTMENT部门可见, PUBLIC公开
     */
    
    @TableField("access_permission")
    private String accessPermission;

    /**
     * 是否已删除
     */
    
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 创建时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private LocalDateTime updateTime;
}

