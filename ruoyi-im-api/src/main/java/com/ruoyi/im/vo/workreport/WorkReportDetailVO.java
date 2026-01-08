package com.ruoyi.im.vo.workreport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.im.vo.workreport.WorkReportCommentVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作日志详情VO
 */
@Data
@Schema(description = "工作日志详情")
public class WorkReportDetailVO implements Serializable {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "提交人ID")
    private Long userId;

    @Schema(description = "提交人姓名")
    private String userName;

    @Schema(description = "提交人头像")
    private String userAvatar;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "日志类型")
    private String reportType;

    @Schema(description = "日志类型名称")
    private String reportTypeName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "报告日期")
    private LocalDate reportDate;

    @Schema(description = "工作内容")
    private String workContent;

    @Schema(description = "完成状态")
    private String completionStatus;

    @Schema(description = "完成状态名称")
    private String completionStatusName;

    @Schema(description = "明日计划")
    private String tomorrowPlan;

    @Schema(description = "遇到的问题")
    private String issues;

    @Schema(description = "附件列表")
    private List<String> attachmentList;

    @Schema(description = "工作时长（小时）")
    private BigDecimal workHours;

    @Schema(description = "可见范围")
    private String visibility;

    @Schema(description = "可见范围名称")
    private String visibilityName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "审批备注")
    private String approveRemark;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "评论列表")
    private List<WorkReportCommentVO> comments;

    @Schema(description = "点赞用户列表")
    private List<WorkReportLikeUserVO> likeUsers;
}
