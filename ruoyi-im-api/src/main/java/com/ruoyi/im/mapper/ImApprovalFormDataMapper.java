package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImApprovalFormData;

import java.util.List;

/**
 * 审批表单数据Mapper接口
 *
 * @author ruoyi
 */
public interface ImApprovalFormDataMapper {

    /**
     * 查询审批表单数据
     *
     * @param id 数据ID
     * @return 审批表单数据
     */
    ImApprovalFormData selectImApprovalFormDataById(Long id);

    /**
     * 根据审批ID查询表单数据
     *
     * @param approvalId 审批ID
     * @return 审批表单数据集合
     */
    List<ImApprovalFormData> selectFormDataByApprovalId(Long approvalId);

    /**
     * 查询审批表单数据列表
     *
     * @param imApprovalFormData 审批表单数据
     * @return 审批表单数据集合
     */
    List<ImApprovalFormData> selectImApprovalFormDataList(ImApprovalFormData imApprovalFormData);

    /**
     * 新增审批表单数据
     *
     * @param imApprovalFormData 审批表单数据
     * @return 结果
     */
    int insertImApprovalFormData(ImApprovalFormData imApprovalFormData);

    /**
     * 批量新增审批表单数据
     *
     * @param formDataList 审批表单数据集合
     * @return 结果
     */
    int batchInsertFormData(List<ImApprovalFormData> formDataList);

    /**
     * 修改审批表单数据
     *
     * @param imApprovalFormData 审批表单数据
     * @return 结果
     */
    int updateImApprovalFormData(ImApprovalFormData imApprovalFormData);

    /**
     * 删除审批表单数据
     *
     * @param id 数据ID
     * @return 结果
     */
    int deleteImApprovalFormDataById(Long id);

    /**
     * 根据审批ID删除表单数据
     *
     * @param approvalId 审批ID
     * @return 结果
     */
    int deleteFormDataByApprovalId(Long approvalId);
}
