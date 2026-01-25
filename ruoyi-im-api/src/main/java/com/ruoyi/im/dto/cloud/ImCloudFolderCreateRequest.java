package com.ruoyi.im.dto.cloud;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "创建文件夹请求")
public class ImCloudFolderCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件夹名称
     */
    @Schema(description = "文件夹名称", required = true)
    @NotBlank(message = "文件夹名称不能为空")
    private String folderName;

    /**
     * 父文件夹ID，0表示根目录
     */
    @Schema(description = "父文件夹ID", required = true)
    @NotNull(message = "父文件夹ID不能为空")
    private Long parentId;

    /**
     * 所有者类型：USER个人, DEPARTMENT部门, COMPANY公司
     */
    @Schema(description = "所有者类型", required = true)
    @NotBlank(message = "所有者类型不能为空")
    private String ownerType;

    /**
     * 部门ID（当owner_type为DEPARTMENT时必填）
     */
    @Schema(description = "部门ID")
    private Long departmentId;

    /**
     * 访问权限：PRIVATE私有, DEPARTMENT部门可见, PUBLIC公开
     */
    @Schema(description = "访问权限")
    private String accessPermission;
}
