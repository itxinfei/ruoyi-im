package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 审计导出请求对象 im_audit_export_request
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_audit_export_request")
public class ImAuditExportRequest extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 申请人用户ID */
    @Excel(name = "申请人用户ID")
    @TableField("requester_id")
    private Long requesterId;

    /** 导出类型（MESSAGE消息 FILE文件 ALL全部） */
    @Excel(name = "导出类型", readConverterExp = "MESSAGE=消息,FILE=文件,ALL=全部")
    @TableField("export_type")
    private String exportType;

    /** 查询条件（JSON格式） */
    @Excel(name = "查询条件")
    @TableField("query_conditions")
    private String queryConditions;

    /** 申请理由 */
    @Excel(name = "申请理由")
    @TableField("reason")
    private String reason;

    /** 审批状态（PENDING待审批 APPROVED已通过 REJECTED已拒绝） */
    @Excel(name = "审批状态", readConverterExp = "PENDING=待审批,APPROVED=已通过,REJECTED=已拒绝")
    @TableField("status")
    private String status;

    /** 审批人用户ID */
    @Excel(name = "审批人用户ID")
    @TableField("approver_id")
    private Long approverId;

    /** 审批时间 */
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @TableField("approved_time")
    private Date approvedTime;

    /** 审批意见 */
    @Excel(name = "审批意见")
    @TableField("approval_comment")
    private String approvalComment;

    /** 导出文件路径 */
    @Excel(name = "导出文件路径")
    @TableField("export_file_path")
    private String exportFilePath;

    /** 导出完成时间 */
    @Excel(name = "导出完成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @TableField("export_completed_time")
    private Date exportCompletedTime;

    /** 申请人用户名（非数据库字段） */
    @TableField(exist = false)
    private String requesterUserName;

    /** 申请人昵称（非数据库字段） */
    @TableField(exist = false)
    private String requesterNickName;

    /** 审批人用户名（非数据库字段） */
    @TableField(exist = false)
    private String approverUserName;

    /** 审批人昵称（非数据库字段） */
    @TableField(exist = false)
    private String approverNickName;

    /** 导出文件大小（非数据库字段） */
    @TableField(exist = false)
    private Long exportFileSize;

    /** 导出文件大小（格式化，非数据库字段） */
    @TableField(exist = false)
    private String exportFileSizeFormatted;

    /** 是否可下载（非数据库字段） */
    @TableField(exist = false)
    private Boolean downloadable;

    /**
     * 检查是否可下载
     * @return 是否可下载
     */
    public Boolean getDownloadable() {
        return "APPROVED".equals(status) && exportFilePath != null && exportCompletedTime != null;
    }
}