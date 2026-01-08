package com.ruoyi.im.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 群组文件上传请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "群组文件上传请求")
public class ImGroupFileUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "群组ID", required = true)
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    @Schema(description = "文件资产ID（已上传到im_file_asset的文件ID）", required = true)
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件分类：default/document/image/video/audio")
    private String category = "default";

    @Schema(description = "下载权限：ALL=所有人, ADMIN=仅管理员")
    private String permission = "ALL";
}
