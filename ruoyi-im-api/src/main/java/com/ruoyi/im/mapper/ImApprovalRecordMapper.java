package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImApprovalRecord;

import java.util.List;

/**
 * 审批记录Mapper接口
 *
 * @author ruoyi
 */
public interface ImApprovalRecordMapper {

    /**
     * 查询审批记录
     *
     * @param id 记录ID
     * @return 审批记录
     */
    ImApprovalRecord selectImApprovalRecordById(Long id);

    /**
     * 根据审批ID查询记录列表
     *
     * @param approvalId 审批ID
     * @return 审批记录集合
     */
    List<ImApprovalRecord> selectRecordsByApprovalId(Long approvalId);

    /**
     * 查询审批记录列表
     *
     * @param imApprovalRecord 审批记录
     * @return 审批记录集合
     */
    List<ImApprovalRecord> selectImApprovalRecordList(ImApprovalRecord imApprovalRecord);

    /**
     * 新增审批记录
     *
     * @param imApprovalRecord 审批记录
     * @return 结果
     */
    int insertImApprovalRecord(ImApprovalRecord imApprovalRecord);

    /**
     * 批量新增审批记录
     *
     * @param records 审批记录集合
     * @return 结果
     */
    int batchInsertRecords(List<ImApprovalRecord> records);

    /**
     * 修改审批记录
     *
     * @param imApprovalRecord 审批记录
     * @return 结果
     */
    int updateImApprovalRecord(ImApprovalRecord imApprovalRecord);

    /**
     * 删除审批记录
     *
     * @param id 记录ID
     * @return 结果
     */
    int deleteImApprovalRecordById(Long id);

    /**
     * 根据审批ID删除记录
     *
     * @param approvalId 审批ID
     * @return 结果
     */
    int deleteRecordsByApprovalId(Long approvalId);
}
