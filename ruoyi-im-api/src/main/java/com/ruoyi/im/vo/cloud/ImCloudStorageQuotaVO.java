package com.ruoyi.im.vo.cloud;

import lombok.Data;

import java.io.Serializable;

/**
 * 云盘存储配额VO
 *
 * @author ruoyi
 */
@Data

public class ImCloudStorageQuotaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long totalCapacity;

    
    private Long usedCapacity;

    
    private Long availableCapacity;

    
    private Double usageRate;

    
    private Integer fileCount;

    
    private Integer folderCount;

    
    private String totalCapacityFormat;

    
    private String usedCapacityFormat;

    
    private String availableCapacityFormat;
}

