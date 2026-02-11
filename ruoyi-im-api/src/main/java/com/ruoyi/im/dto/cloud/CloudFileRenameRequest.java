package com.ruoyi.im.dto.cloud;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 云盘文件重命名请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "云盘文件重命名请求")
public class CloudFileRenameRequest {

    @NotBlank(message = "新名称不能为空")
    @Schema(description = "新文件/文件夹名称", required = true)
    private String newName;
}