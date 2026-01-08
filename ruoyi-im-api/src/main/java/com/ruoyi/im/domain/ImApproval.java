package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批实例实体
 *
 * @author ruoyi
 */
@TableName("im_approval")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImApproval extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 审批ID，主键，唯一标识审批实例 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板ID，关联到im_approval_template表 */
    @TableField("template_id")
    private Long templateId;

    /** 审批标题，用于描述审批事项的简短标题 */
    private String title;

    /** 申请人ID，发起审批的用户ID */
    @TableField("applicant_id")
    private Long applicantId;

    /** 状态（PENDING待审批 APPROVED已通过 REJECTED已驳回 CANCELLED已取消） */
    private String status;

    /** 当前节点ID，审批流程当前所处的节点ID */
    @TableField("current_node_id")
    private Long currentNodeId;

    /** 表单数据（JSON格式），申请人填写的表单数据 */
    @TableField("form_data")
    private String formData;

    /** 附件列表（JSON格式），审批相关的附件信息 */
    private String attachments;

    /** 申请时间，审批发起的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /** 完成时间，审批流程的完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("finish_time")
    private LocalDateTime finishTime;

    /** 创建者 */
    @TableField("create_by")
    private String createBy;

    /** 更新者 */
    @TableField("update_by")
    private String updateBy;

    /** 备注 */
    private String remark;
}
