package com.ruoyi.im.dto.cloud;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 文件移动请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文件移动请求")
public class ImCloudFileMoveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要移动的文件ID列表
     */
    @Schema(description = "要移动的文件ID列表", required = true)
    @NotEmpty(message = "文件ID列表不能为空")
    private List<Long> fileIds;

    /**
     * 目标文件夹ID
     */
    @Schema(description = "目标文件夹ID", required = true)
    @NotNull(message = "目标文件夹ID不能为空")
    private Long targetFolderId;
}
