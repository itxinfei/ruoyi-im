package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImApproval;

import java.util.List;

/**
 * 审批实例Mapper接口
 *
 * @author ruoyi
 */
public interface ImApprovalMapper {

    /**
     * 查询审批实例
     *
     * @param id 审批ID
     * @return 审批实例
     */
    ImApproval selectImApprovalById(Long id);

    /**
     * 查询审批实例列表
     *
     * @param imApproval 审批实例
     * @return 审批实例集合
     */
    List<ImApproval> selectImApprovalList(ImApproval imApproval);

    /**
     * 查询用户的待审批列表
     *
     * @param approverId 审批人ID
     * @return 审批实例集合
     */
    List<ImApproval> selectPendingApprovalByApproverId(Long approverId);

    /**
     * 查询用户发起的审批列表
     *
     * @param applicantId 申请人ID
     * @return 审批实例集合
     */
    List<ImApproval> selectApprovalByApplicantId(Long applicantId);

    /**
     * 新增审批实例
     *
     * @param imApproval 审批实例
     * @return 结果
     */
    int insertImApproval(ImApproval imApproval);

    /**
     * 修改审批实例
     *
     * @param imApproval 审批实例
     * @return 结果
     */
    int updateImApproval(ImApproval imApproval);

    /**
     * 删除审批实例
     *
     * @param id 审批ID
     * @return 结果
     */
    int deleteImApprovalById(Long id);

    /**
     * 批量删除审批实例
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImApprovalByIds(Long[] ids);
}
