package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件分享记录实体
 * 记录用户分享的文件信息和访问权限
 *
 * @author ruoyi
 */
@TableName("im_file_share")
@Data

public class ImFileShare implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    
    private Long id;

    
    private Long fileId;

    
    private Long sharerId;

    
    private String receiverIds;

    
    private Integer permission;

    
    private String accessPassword;

    
    private Integer allowDownload;

    
    private Integer allowPreview;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    
    private Integer accessCount;

    
    private Integer downloadCount;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

