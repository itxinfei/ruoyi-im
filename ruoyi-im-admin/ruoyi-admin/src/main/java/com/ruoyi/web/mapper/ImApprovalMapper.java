package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * IM审批数据访问层接口（Admin模块专用）
 *
 * <p>负责处理IM审批管理相关的数据库操作</p>
 * <p>主要功能包括：审批的增删改查、审批流程处理、模板管理、统计信息等</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
@Mapper
public interface ImApprovalMapper {

    /**
     * 查询审批列表
     *
     * <p>根据条件查询IM审批列表，支持按状态、用户、时间等条件筛选</p>
     *
     * @param imApproval 审批查询条件，包含status、userId、title等字段
     * @return 审批列表，如果没有符合条件的审批则返回空列表
     */
    List<ImApproval> selectImApprovalList(ImApproval imApproval);

    /**
     * 根据ID获取审批信息
     *
     * <p>通过审批ID查询审批的详细信息，包括审批状态、流程节点、参与人等</p>
     *
     * @param id 审批ID，不能为空
     * @return 审批对象，如果审批不存在则返回null
     */
    ImApproval selectImApprovalById(@Param("id") Long id);

    /**
     * 新增审批
     *
     * <p>向数据库中插入新的审批记录，审批状态默认为待审批</p>
     *
     * @param imApproval 审批对象，包含审批的基本信息和流程配置
     * @return 影响行数，1表示成功，0表示失败
     */
    int insertImApproval(ImApproval imApproval);

    /**
     * 修改审批
     *
     * <p>更新审批的信息、状态、流程节点等，审批ID不可修改</p>
     *
     * @param imApproval 审批对象，必须包含审批ID
     * @return 影响行数，1表示成功，0表示失败
     */
    int updateImApproval(ImApproval imApproval);

    /**
     * 删除审批
     *
     * <p>批量删除指定的审批记录，删除操作为物理删除</p>
     *
     * @param ids 审批ID数组，不能为空
     * @return 影响行数，表示成功删除的审批数量
     */
    int deleteImApprovalByIds(@Param("ids") Long[] ids);

    /**
     * 获取审批详情（包含流程节点）
     *
     * <p>查询审批的详细信息，包括流程节点、处理记录、附件等</p>
     * <p>返回的Map包含：审批基本信息、流程节点列表、操作日志等</p>
     *
     * @param id 审批ID，不能为空
     * @return 审批详情Map，包含审批的所有相关信息
     */
    Map<String, Object> getApprovalDetail(@Param("id") Long id);

    /**
     * 获取审批列表（管理员视角）
     *
     * <p>查询所有审批或指定条件的审批，用于管理员进行审批管理</p>
     * <p>支持按状态、用户、标题等多条件组合查询</p>
     *
     * @param status 审批状态（PENDING待审批、APPROVED已通过、REJECTED已驳回、CANCELLED已撤销），为null表示查询所有状态
     * @param userId 用户ID，为null表示查询所有用户的审批
     * @param title 审批标题，支持模糊查询
     * @return 审批列表，按创建时间倒序排列
     */
    List<ImApproval> selectApprovalListForAdmin(@Param("status") String status, @Param("userId") Long userId, @Param("title") String title);

    /**
     * 获取我发起的审批列表
     *
     * <p>查询指定用户发起的所有审批记录，用于"我的申请"功能</p>
     *
     * @param userId 用户ID，不能为空
     * @return 审批列表，按创建时间倒序排列
     */
    List<ImApproval> getMyApprovals(@Param("userId") Long userId);

    /**
     * 获取审批模板列表
     *
     * <p>查询系统中配置的所有审批模板，包括启用和禁用的模板</p>
     *
     * @return 审批模板列表，包含模板的配置和使用次数
     */
    List<?> getTemplates();

    /**
     * 获取启用的审批模板列表
     *
     * <p>查询所有启用状态的审批模板，用于发起审批时选择模板</p>
     *
     * @return 启用的审批模板列表
     */
    List<?> getActiveTemplates();

    /**
     * 启用/禁用审批模板
     *
     * <p>更新审批模板的启用状态，禁用的模板不能用于创建新审批</p>
     *
     * @param id 审批模板ID，不能为空
     * @param isActive 启用标志：true-启用，false-禁用
     */
    void updateTemplateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    /**
     * 处理审批（通过/驳回）
     *
     * <p>对指定的审批进行审核操作，记录处理结果和处理意见</p>
     * <p>支持的操作：APPROVED（通过）、REJECTED（驳回）</p>
     *
     * @param id 审批ID，不能为空
     * @param action 处理操作：APPROVED（通过）、REJECTED（驳回）
     * @param comment 处理意见，驳回时必须填写
     * @param userId 处理人ID，不能为空
     */
    void processApproval(@Param("id") Long id, @Param("action") String action, @Param("comment") String comment, @Param("userId") Long userId);

    /**
     * 撤销审批
     *
     * <p>撤销指定的审批记录，只能撤销待审批状态的审批</p>
     * <p>撤销后审批状态变为已撤销，流程终止</p>
     *
     * @param id 审批ID，不能为空
     * @param userId 撤销人ID，必须与审批发起人相同或具有管理员权限
     */
    void cancelApproval(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 获取审批统计信息
     *
     * <p>统计指定时间段内的审批数据，包括总数、通过率、驳回率等</p>
     * <p>返回的Map包含以下统计项：</p>
     * <ul>
     *   <li>total_count: 审批总数</li>
     *   *   <li>pending_count: 待审批数量</li>
     *   *   <li>approved_count: 已通过数量</li>
     *   *   <li>rejected_count: 已驳回数量</li>
     *   *   <li>cancelled_count: 已撤销数量</li>
     *   *   <li>approval_rate: 通过率（百分比）</li>
     * </ul>
     *
     * @param startTime 开始时间，格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间，格式：yyyy-MM-dd HH:mm:ss
     * @return 统计信息Map，key为统计项名称，value为统计值
     */
    Map<String, Object> getApprovalStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
