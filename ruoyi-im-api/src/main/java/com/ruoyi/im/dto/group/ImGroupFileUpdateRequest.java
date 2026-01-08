package com.ruoyi.im.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 群组文件更新请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "群组文件更新请求")
public class ImGroupFileUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件名称")
    @NotBlank(message = "文件名称不能为空")
    private String fileName;

    @Schema(description = "文件分类：default/document/image/video/audio")
    private String category;

    @Schema(description = "下载权限：ALL=所有人, ADMIN=仅管理员")
    private String permission;
}
