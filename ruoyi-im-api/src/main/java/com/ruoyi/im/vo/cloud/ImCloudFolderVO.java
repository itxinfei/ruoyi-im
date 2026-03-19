package com.ruoyi.im.vo.cloud;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云盘文件夹VO
 *
 * @author ruoyi
 */
@Data

public class ImCloudFolderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private String folderName;

    
    private Long parentId;

    
    private Long ownerId;

    
    private String ownerName;

    
    private String ownerType;

    
    private Long departmentId;

    
    private String folderPath;

    
    private Integer level;

    
    private Integer subFolderCount;

    
    private Integer fileCount;

    
    private String accessPermission;

    
    private Boolean canEdit;

    
    private Boolean canDelete;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

