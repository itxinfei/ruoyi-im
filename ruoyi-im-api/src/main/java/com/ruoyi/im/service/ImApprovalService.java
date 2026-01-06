package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImApproval;
import com.ruoyi.im.domain.ImApprovalTemplate;

import java.util.List;
import java.util.Map;

/**
 * 审批服务接口
 *
 * @author ruoyi
 */
public interface ImApprovalService {

    /**
     * 发起审批
     *
     * @param templateId 模板ID
     * @param title 审批标题
     * @param formData 表单数据
     * @param applicantId 申请人ID
     * @return 审批ID
     */
    Long createApproval(Long templateId, String title, Map<String, Object> formData, Long applicantId);

    /**
     * 处理审批（通过/驳回）
     *
     * @param approvalId 审批ID
     * @param action 操作：APPROVE通过/REJECT驳回/TRANSFER转审
     * @param comment 审批意见
     * @param approverId 审批人ID
     */
    void processApproval(Long approvalId, String action, String comment, Long approverId);

    /**
     * 获取审批详情
     *
     * @param approvalId 审批ID
     * @return 审批详情
     */
    Map<String, Object> getApprovalDetail(Long approvalId);

    /**
     * 查询待我审批列表
     *
     * @param approverId 审批人ID
     * @return 审批列表
     */
    List<ImApproval> getPendingApprovals(Long approverId);

    /**
     * 查询我发起的审批列表
     *
     * @param applicantId 申请人ID
     * @return 审批列表
     */
    List<ImApproval> getMyApprovals(Long applicantId);

    /**
     * 查询我已审批列表
     *
     * @param approverId 审批人ID
     * @return 审批列表
     */
    List<ImApproval> getProcessedApprovals(Long approverId);

    /**
     * 撤回审批
     *
     * @param approvalId 审批ID
     * @param applicantId 申请人ID
     */
    void cancelApproval(Long approvalId, Long applicantId);

    /**
     * 获取审批模板列表
     *
     * @return 模板列表
     */
    List<ImApprovalTemplate> getTemplates();

    /**
     * 获取启用的审批模板列表
     *
     * @return 模板列表
     */
    List<ImApprovalTemplate> getActiveTemplates();
}
