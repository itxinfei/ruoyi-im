package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImApproval;
import java.util.List;
import java.util.Map;

/**
 * IM审批Service接口（Admin模块专用）
 */
public interface ImApprovalService {

    /**
     * 查询审批列表
     */
    List<ImApproval> selectImApprovalList(ImApproval imApproval);

    /**
     * 获取审批详情
     */
    Map<String, Object> getApprovalDetail(Long id);

    /**
     * 根据ID获取审批
     */
    ImApproval selectImApprovalById(Long id);

    /**
     * 新增审批
     */
    int insertImApproval(ImApproval imApproval);

    /**
     * 修改审批
     */
    int updateImApproval(ImApproval imApproval);

    /**
     * 删除审批
     */
    int deleteImApprovalByIds(Long[] ids);

    /**
     * 获取审批列表（管理员视角）
     */
    List<ImApproval> selectApprovalListForAdmin(String status, Long userId, String title);

    /**
     * 获取我发起的审批列表
     */
    List<ImApproval> getMyApprovals(Long userId);

    /**
     * 获取审批模板列表
     */
    List<?> getTemplates();

    /**
     * 获取启用的审批模板列表
     */
    List<?> getActiveTemplates();

    /**
     * 启用/禁用审批模板
     */
    void updateTemplateStatus(Long id, Boolean isActive);

    /**
     * 处理审批（通过/驳回）
     */
    void processApproval(Long id, String action, String comment, Long userId);

    /**
     * 撤销审批
     */
    void cancelApproval(Long id, Long userId);

    /**
     * 获取审批统计
     */
    Map<String, Object> getApprovalStatistics(String startTime, String endTime);
}
