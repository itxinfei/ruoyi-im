package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImApprovalNode;

import java.util.List;

/**
 * 审批节点Mapper接口
 *
 * @author ruoyi
 */
public interface ImApprovalNodeMapper {

    /**
     * 查询审批节点
     *
     * @param id 节点ID
     * @return 审批节点
     */
    ImApprovalNode selectImApprovalNodeById(Long id);

    /**
     * 根据审批ID查询节点列表
     *
     * @param approvalId 审批ID
     * @return 审批节点集合
     */
    List<ImApprovalNode> selectNodesByApprovalId(Long approvalId);

    /**
     * 根据审批ID查询待处理节点列表
     *
     * @param approvalId 审批ID
     * @return 待处理节点集合
     */
    List<ImApprovalNode> selectPendingNodesByApprovalId(Long approvalId);

    /**
     * 根据审批ID查询第一个节点
     *
     * @param approvalId 审批ID
     * @return 第一个节点
     */
    ImApprovalNode selectFirstNodeByApprovalId(Long approvalId);

    /**
     * 查询审批节点列表
     *
     * @param imApprovalNode 审批节点
     * @return 审批节点集合
     */
    List<ImApprovalNode> selectImApprovalNodeList(ImApprovalNode imApprovalNode);

    /**
     * 查询用户的待审批节点
     *
     * @param approverId 审批人ID
     * @return 审批节点集合
     */
    List<ImApprovalNode> selectPendingNodesByApproverId(Long approverId);

    /**
     * 批量新增审批节点
     *
     * @param nodes 审批节点集合
     * @return 结果
     */
    int batchInsertNodes(List<ImApprovalNode> nodes);

    /**
     * 新增审批节点
     *
     * @param imApprovalNode 审批节点
     * @return 结果
     */
    int insertImApprovalNode(ImApprovalNode imApprovalNode);

    /**
     * 修改审批节点
     *
     * @param imApprovalNode 审批节点
     * @return 结果
     */
    int updateImApprovalNode(ImApprovalNode imApprovalNode);

    /**
     * 删除审批节点
     *
     * @param id 节点ID
     * @return 结果
     */
    int deleteImApprovalNodeById(Long id);

    /**
     * 根据审批ID删除节点
     *
     * @param approvalId 审批ID
     * @return 结果
     */
    int deleteNodesByApprovalId(Long approvalId);
}
