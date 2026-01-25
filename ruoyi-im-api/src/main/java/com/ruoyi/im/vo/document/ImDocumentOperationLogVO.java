package com.ruoyi.im.vo.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档操作日志VO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文档操作日志")
public class ImDocumentOperationLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作位置")
    private Integer position;

    @Schema(description = "操作内容")
    private String content;

    @Schema(description = "内容长度")
    private Integer contentLength;

    @Schema(description = "变更摘要")
    private String changeSummary;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;
}
