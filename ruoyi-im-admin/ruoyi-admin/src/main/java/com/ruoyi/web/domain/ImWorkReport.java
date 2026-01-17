package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 工作报告对象 im_work_report
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImWorkReport extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long id;

    /** 提交人ID */
    @Excel(name = "提交人ID")
    private Long userId;

    /** 用户名（关联查询） */
    @Excel(name = "用户名")
    private String username;

    /** 昵称（关联查询） */
    @Excel(name = "昵称")
    private String nickname;

    /** 日志类型（DAILY日报 WEEKLY周报 MONTHLY月报） */
    @Excel(name = "报告类型")
    private String reportType;

    /** 报告日期 */
    @Excel(name = "报告日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reportDate;

    /** 工作内容 */
    @Excel(name = "工作内容")
    private String workContent;

    /** 完成状态（COMPLETED已完成 IN_PROGRESS进行中 PENDING待处理） */
    @Excel(name = "完成状态")
    private String completionStatus;

    /** 明日计划 */
    @Excel(name = "明日计划")
    private String tomorrowPlan;

    /** 遇到的问题 */
    @Excel(name = "遇到的问题")
    private String issues;

    /** 附件（多个附件用逗号分隔） */
    @Excel(name = "附件")
    private String attachments;

    /** 工作时长（小时） */
    @Excel(name = "工作时长")
    private BigDecimal workHours;

    /** 可见范围（PRIVATE私有 DEPARTMENT部门 PUBLIC公开） */
    @Excel(name = "可见范围")
    private String visibility;

    /** 提交时间 */
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 状态（DRAFT草稿 SUBMITTED已提交 APPROVED已审批 REJECTED已退回） */
    @Excel(name = "状态")
    private String status;

    /** 审批人ID */
    @Excel(name = "审批人ID")
    private Long approverId;

    /** 审批人姓名（关联查询） */
    @Excel(name = "审批人")
    private String approverName;

    /** 审批时间 */
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批备注 */
    @Excel(name = "审批备注")
    private String approveRemark;

}
