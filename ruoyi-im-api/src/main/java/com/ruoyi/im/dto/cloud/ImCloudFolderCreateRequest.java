package com.ruoyi.im.dto.cloud;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 创建文件夹请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImCloudFolderCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件夹名称
     */
    
    @NotBlank(message = "文件夹名称不能为空")
    private String folderName;

    /**
     * 父文件夹ID，0表示根目录
     */
    
    @NotNull(message = "父文件夹ID不能为空")
    private Long parentId;

    /**
     * 所有者类型：USER个人, DEPARTMENT部门, COMPANY公司
     */
    
    @NotBlank(message = "所有者类型不能为空")
    private String ownerType;

    /**
     * 部门ID（当owner_type为DEPARTMENT时必填）
     */
    
    private Long departmentId;

    /**
     * 访问权限：PRIVATE私有, DEPARTMENT部门可见, PUBLIC公开
     */
    
    private String accessPermission;
}

