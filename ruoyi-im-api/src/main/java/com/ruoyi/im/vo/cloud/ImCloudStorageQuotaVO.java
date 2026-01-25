package com.ruoyi.im.vo.cloud;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 云盘存储配额VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "云盘存储配额")
public class ImCloudStorageQuotaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总容量（字节）")
    private Long totalCapacity;

    @Schema(description = "已用容量（字节）")
    private Long usedCapacity;

    @Schema(description = "可用容量（字节）")
    private Long availableCapacity;

    @Schema(description = "使用率（百分比）")
    private Double usageRate;

    @Schema(description = "文件数量")
    private Integer fileCount;

    @Schema(description = "文件夹数量")
    private Integer folderCount;

    @Schema(description = "格式化总容量")
    private String totalCapacityFormat;

    @Schema(description = "格式化已用容量")
    private String usedCapacityFormat;

    @Schema(description = "格式化可用容量")
    private String availableCapacityFormat;
}
