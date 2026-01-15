package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM审批Mapper接口（Admin模块专用）
 */
@Mapper
public interface ImApprovalMapper {

    /**
     * 查询审批列表
     */
    List<ImApproval> selectImApprovalList(ImApproval imApproval);

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
     * 获取审批详情（包含流程节点）
     */
    Map<String, Object> getApprovalDetail(Long id);

    /**
     * 获取审批列表（管理员视角）
     */
    List<ImApproval> selectApprovalListForAdmin(@Param("status") String status, @Param("userId") Long userId, @Param("title") String title);

    /**
     * 获取我发起的审批列表
     */
    List<ImApproval> getMyApprovals(@Param("userId") Long userId);

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
    void updateTemplateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    /**
     * 处理审批（通过/驳回）
     */
    void processApproval(@Param("id") Long id, @Param("action") String action, @Param("comment") String comment, @Param("userId") Long userId);

    /**
     * 撤销审批
     */
    void cancelApproval(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 获取审批统计
     */
    Map<String, Object> getApprovalStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
