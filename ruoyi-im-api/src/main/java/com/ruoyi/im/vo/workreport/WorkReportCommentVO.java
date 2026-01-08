package com.ruoyi.im.vo.workreport;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作日志评论VO
 */
@Data
@Schema(description = "工作日志评论")
public class WorkReportCommentVO implements Serializable {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "日志ID")
    private Long reportId;

    @Schema(description = "评论人ID")
    private Long userId;

    @Schema(description = "评论人姓名")
    private String userName;

    @Schema(description = "评论人头像")
    private String userAvatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "父评论内容")
    private String parentContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "评论时间")
    private LocalDateTime createTime;

    @Schema(description = "子评论列表")
    private List<WorkReportCommentVO> children;
}
