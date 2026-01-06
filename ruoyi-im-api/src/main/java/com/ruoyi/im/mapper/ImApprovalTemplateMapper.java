package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImApprovalTemplate;

import java.util.List;

/**
 * 审批模板Mapper接口
 *
 * @author ruoyi
 */
public interface ImApprovalTemplateMapper {

    /**
     * 查询审批模板
     *
     * @param id 模板ID
     * @return 审批模板
     */
    ImApprovalTemplate selectImApprovalTemplateById(Long id);

    /**
     * 根据编码查询审批模板
     *
     * @param code 模板编码
     * @return 审批模板
     */
    ImApprovalTemplate selectImApprovalTemplateByCode(String code);

    /**
     * 查询审批模板列表
     *
     * @param imApprovalTemplate 审批模板
     * @return 审批模板集合
     */
    List<ImApprovalTemplate> selectImApprovalTemplateList(ImApprovalTemplate imApprovalTemplate);

    /**
     * 查询启用的审批模板列表
     *
     * @return 审批模板集合
     */
    List<ImApprovalTemplate> selectActiveTemplates();

    /**
     * 新增审批模板
     *
     * @param imApprovalTemplate 审批模板
     * @return 结果
     */
    int insertImApprovalTemplate(ImApprovalTemplate imApprovalTemplate);

    /**
     * 修改审批模板
     *
     * @param imApprovalTemplate 审批模板
     * @return 结果
     */
    int updateImApprovalTemplate(ImApprovalTemplate imApprovalTemplate);

    /**
     * 删除审批模板
     *
     * @param id 模板ID
     * @return 结果
     */
    int deleteImApprovalTemplateById(Long id);

    /**
     * 批量删除审批模板
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImApprovalTemplateByIds(Long[] ids);
}
