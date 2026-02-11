package com.ruoyi.im.dto.cloud;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 云盘文件夹列表请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "云盘文件夹列表请求")
public class CloudFolderListRequest {

    @NotNull(message = "父文件夹ID不能为空")
    @Schema(description = "父文件夹ID，0表示根目录", required = true, example = "0")
    private Long parentId;

    @Schema(description = "所有者类型：USER个人, DEPARTMENT部门, COMPANY公司", example = "USER")
    private String ownerType;
}