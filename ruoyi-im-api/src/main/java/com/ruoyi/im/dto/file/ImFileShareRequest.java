package com.ruoyi.im.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 文件分享请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文件分享请求")
public class ImFileShareRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件ID", required = true)
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "接收者ID列表，多个用逗号分隔")
    @NotBlank(message = "接收者不能为空")
    private String receiverIds;

    @Schema(description = "分享权限: 1=公开, 2=指定人")
    private Integer permission = 1;

    @Schema(description = "访问密码，permission=2时需要")
    private String accessPassword;

    @Schema(description = "是否允许下载")
    private Integer allowDownload = 1;

    @Schema(description = "是否允许预览")
    private Integer allowPreview = 1;

    @Schema(description = "有效期（天），0表示永久")
    private Integer expireDays = 0;
}
