package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;

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
     * 完成时间，审批流程的完成时间
     */
    private LocalDateTime completedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(Long currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public LocalDateTime getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }

}
