package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批实例实体
 *
 * 用于存储IM系统中的审批实例信息，包括审批流程的发起、流转、审批等全生命周期管理
 * 支持基于模板的审批流程，记录申请人、审批状态、当前节点、表单数据等关键信息
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImApproval extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审批ID，主键，唯一标识审批实例
     */
    private Long id;

    /**
     * 模板ID，关联到im_approval_template表，指定该审批使用的模板
     */
    private Long templateId;

    /**
     * 审批标题，用于描述审批事项的简短标题
     */
    private String title;

    /**
     * 申请人ID，发起审批的用户ID，关联到im_user表
     */
    private Long applicantId;

    /**
     * 状态（PENDING待审批 APPROVED已通过 REJECTED已驳回 CANCELLED已取消）
     * PENDING: 审批已发起，等待审批人处理
     * APPROVED: 审批流程已全部通过
     * REJECTED: 审批流程被驳回，需要重新申请或终止
     * CANCELLED: 审批流程被申请人主动取消
     */
    private String status;

    /**
     * 当前节点ID，审批流程当前所处的节点ID，用于跟踪审批进度
     */
    private Long currentNodeId;

    /**
     * 表单数据（JSON格式），申请人填写的表单数据，以JSON字符串形式存储
     */
    private String formData;

    /**
     * 附件列表（JSON格式），审批相关的附件信息，以JSON字符串形式存储
     */
    private String attachments;

    /**
     * 申请时间，审批发起的时间
     */
    private LocalDateTime applyTime;

    /**
     * 完成时间，审批流程的完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 申请人姓名（非数据库字段，用于查询结果展示）
     */
    private String applicantName;

}
