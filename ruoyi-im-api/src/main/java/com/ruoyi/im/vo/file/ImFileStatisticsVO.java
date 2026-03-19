package com.ruoyi.im.vo.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件统计VO
 *
 * @author ruoyi
 */
@Data

public class ImFileStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Integer totalFiles;

    
    private Integer todayUploads;

    
    private Long totalStorageSize;

    
    private Integer totalDownloads;

    
    private Integer imageCount;

    
    private Integer videoCount;

    
    private Integer documentCount;

    
    private Integer otherCount;
}

