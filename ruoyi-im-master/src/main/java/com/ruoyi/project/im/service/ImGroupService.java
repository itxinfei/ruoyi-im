package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImGroup;
import com.ruoyi.web.domain.ImGroupMember;

import java.util.List;
import java.util.Map;

/**
 * IM群组Service接口（Admin模块专用）
 *
 * <p>提供群组管理的核心业务功能，包括群组的增删改查、成员管理等操作</p>
 * <p>支持群组创建、解散、成员添加/移除、群组信息维护等功能</p>
 *
 * @author ruoyi
 * @date 2025-01-07
 */
public interface ImGroupService {

    /**
     * 查询IM群组列表
     *
     * <p>根据查询条件检索群组信息列表，支持多条件组合查询</p>
     * <p>可通过群组名称、类型、创建时间等条件进行筛选</p>
     *
     * @param imGroup 查询条件对象，包含群组名称、类型等筛选条件
     * @return 群组信息列表，如果没有符合条件的数据则返回空列表
     */
    List<ImGroup> selectImGroupList(ImGroup imGroup);

    /**
     * 根据群组ID查询群组信息
     *
     * <p>通过群组主键ID获取单个群组的详细信息</p>
     * <p>包含群组基本属性、成员数量、创建时间等完整信息</p>
     *
     * @param id 群组主键ID，不能为空
     * @return 群组信息对象，如果不存在则返回null
     */
    ImGroup selectImGroupById(Long id);

    /**
     * 新增IM群组
     *
     * <p>创建新的群组，包括普通群和部门群等类型</p>
     * <p>创建时自动设置创建时间、创建人等基础信息，并初始化群组状态</p>
     *
     * @param imGroup 群组信息对象，包含群组名称、类型、描述等必要信息
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int insertImGroup(ImGroup imGroup);

    /**
     * 修改IM群组信息
     *
     * <p>更新群组的基本信息，如群组名称、描述、头像等</p>
     * <p>不包含成员管理功能，成员操作请使用专门的成员管理方法</p>
     *
     * @param imGroup 群组信息对象，必须包含群组ID和要更新的字段
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int updateImGroup(ImGroup imGroup);

    /**
     * 删除单个群组
     *
     * <p>根据群组ID删除群组记录及其关联数据</p>
     * <p>注意：此操作会同时删除群组成员关系、群组消息等相关数据，请谨慎使用</p>
     *
     * @param id 群组主键ID，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int deleteImGroupById(Long id);

    /**
     * 批量删除群组
     *
     * <p>根据群组ID数组批量删除多个群组</p>
     * <p>适用于管理后台的批量删除操作，提高删除效率</p>
     *
     * @param ids 群组主键ID数组，不能为空或空数组
     * @return 影响的记录数，返回实际删除的群组数量
     */
    int deleteImGroupByIds(Long[] ids);

    /**
     * 查询群组成员列表
     *
     * <p>根据群组ID获取该群组的所有成员信息</p>
     * <p>包含成员ID、角色（群主/管理员/普通成员）、加入时间等信息</p>
     *
     * @param groupId 群组ID，不能为空
     * @return 群组成员信息列表，如果群组不存在或没有成员则返回空列表
     */
    List<ImGroupMember> selectGroupMembersByGroupId(Long groupId);

    /**
     * 添加群组成员
     *
     * <p>向指定群组添加新成员，并设置成员角色</p>
     * <p>记录邀请人信息，支持普通成员和管理员两种角色</p>
     * <p>添加前会检查用户是否已在群组中，避免重复添加</p>
     *
     * @param groupId 群组ID，不能为空
     * @param userId 要添加的用户ID，不能为空
     * @param role 成员角色，如 MEMBER（普通成员）、ADMIN（管理员）
     * @param inviterId 邀请人ID，记录是谁邀请该用户加入群组的
     * @return 影响的记录数，成功添加返回1，用户已存在返回0
     */
    int addGroupMember(Long groupId, Long userId, String role, Long inviterId);

    /**
     * 解散群组
     *
     * <p>删除群组及其所有相关数据，包括成员关系、消息记录等</p>
     * <p>只有群主才有权限解散群组，此操作不可逆</p>
     * <p>解散后所有成员将无法访问该群组的任何信息</p>
     *
     * @param groupId 要解散的群组ID，不能为空
     * @return 影响的记录数，成功返回1，失败返回0
     */
    int dismissGroup(Long groupId);

    /**
     * 获取群组统计数据
     *
     * <p>统计群组的总数、成员总数、活跃群组数等关键指标</p>
     * <p>用于管理后台的数据看板展示和报表生成</p>
     *
     * @return 统计数据Map，包含 totalGroups（群组总数）、totalMembers（成员总数）等指标
     */
    Map<String, Object> getGroupStatistics();

    /**
     * 批量导入群组
     *
     * <p>从 Excel 文件批量导入群组数据</p>
     * <p>支持导入群组名称、群主ID、描述、最大成员数等信息</p>
     * <p>会自动校验数据格式、检查重复、处理错误行</p>
     *
     * @param groups 群组列表
     * @param updateSupported 是否支持更新已存在的群组
     * @return 导入结果，包含成功数量、失败数量、失败详情等
     */
    Map<String, Object> batchImportGroups(List<ImGroup> groups, boolean updateSupported);
}
