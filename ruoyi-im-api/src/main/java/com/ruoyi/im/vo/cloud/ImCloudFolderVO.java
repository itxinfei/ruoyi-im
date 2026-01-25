package com.ruoyi.im.vo.cloud;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云盘文件夹VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "云盘文件夹")
public class ImCloudFolderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件夹ID")
    private Long id;

    @Schema(description = "文件夹名称")
    private String folderName;

    @Schema(description = "父文件夹ID")
    private Long parentId;

    @Schema(description = "所有者ID")
    private Long ownerId;

    @Schema(description = "所有者名称")
    private String ownerName;

    @Schema(description = "所有者类型")
    private String ownerType;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "文件夹路径")
    private String folderPath;

    @Schema(description = "层级深度")
    private Integer level;

    @Schema(description = "子文件夹数量")
    private Integer subFolderCount;

    @Schema(description = "文件数量")
    private Integer fileCount;

    @Schema(description = "访问权限")
    private String accessPermission;

    @Schema(description = "是否可编辑")
    private Boolean canEdit;

    @Schema(description = "是否可删除")
    private Boolean canDelete;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
