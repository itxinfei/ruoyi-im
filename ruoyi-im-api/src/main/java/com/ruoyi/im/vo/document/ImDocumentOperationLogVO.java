package com.ruoyi.im.vo.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档操作日志VO
 *
 * @author ruoyi
 */
@Data

public class ImDocumentOperationLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private Long id;

    
    private Long documentId;

    
    private Long userId;

    
    private String userName;

    
    private String operationType;

    
    private Integer position;

    
    private String content;

    
    private Integer contentLength;

    
    private String changeSummary;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;
}

