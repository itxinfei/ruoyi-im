package com.ruoyi.im.dto.cloud;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 文件分享请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImCloudFileShareRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分享类型：FILE文件, FOLDER文件夹
     */
    
    @NotNull(message = "分享类型不能为空")
    private String shareType;

    /**
     * 文件或文件夹ID
     */
    
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    /**
     * 访问密码（可选）
     */
    
    private String accessPassword;

    /**
     * 有效期天数，0表示永久
     */
    
    private Integer expireDays;

    /**
     * 是否允许下载
     */
    
    private Boolean allowDownload;

    /**
     * 是否允许预览
     */
    
    private Boolean allowPreview;
}

