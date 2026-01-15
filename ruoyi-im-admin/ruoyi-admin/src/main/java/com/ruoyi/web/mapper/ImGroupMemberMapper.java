package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImGroupMember;
import java.util.List;

/**
 * 群组成员Mapper接口
 *
 * @author ruoyi
 */
public interface ImGroupMemberMapper {

    /**
     * 查询群组成员
     *
     * @param id 群组成员主键
     * @return 群组成员
     */
    public ImGroupMember selectImGroupMemberById(Long id);

    /**
     * 查询群组成员列表
     *
     * @param imGroupMember 群组成员
     * @return 群组成员集合
     */
    public List<ImGroupMember> selectImGroupMemberList(ImGroupMember imGroupMember);

    /**
     * 根据群组ID查询成员列表
     *
     * @param groupId 群组ID
     * @return 成员列表
     */
    public List<ImGroupMember> selectMembersByGroupId(Long groupId);

    /**
     * 统计群组成员数量
     *
     * @param groupId 群组ID
     * @return 成员数量
     */
    public Integer countMembersByGroupId(Long groupId);

    /**
     * 查询用户加入的群组列表
     *
     * @param userId 用户ID
     * @return 群组成员列表
     */
    public List<ImGroupMember> selectUserGroups(Long userId);

    /**
     * 查询群主
     *
     * @param groupId 群组ID
     * @return 群主信息
     */
    public ImGroupMember selectGroupOwner(Long groupId);

    /**
     * 检查成员是否存在
     *
     * @param params 包含groupId和userId的参数
     * @return 存在数量
     */
    public Integer checkMemberExists(java.util.Map<String, Object> params);

    /**
     * 新增群组成员
     *
     * @param imGroupMember 群组成员
     * @return 结果
     */
    public int insertImGroupMember(ImGroupMember imGroupMember);

    /**
     * 修改群组成员
     *
     * @param imGroupMember 群组成员
     * @return 结果
     */
    public int updateImGroupMember(ImGroupMember imGroupMember);

    /**
     * 修改成员角色
     *
     * @param params 包含groupId、userId、role的参数
     * @return 结果
     */
    public int updateMemberRole(java.util.Map<String, Object> params);

    /**
     * 删除群组成员
     *
     * @param id 群组成员主键
     * @return 结果
     */
    public int deleteImGroupMemberById(Long id);

    /**
     * 批量删除群组成员
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteImGroupMemberByIds(Long[] ids);

    /**
     * 移除成员（软删除）
     *
     * @param params 包含groupId和userId的参数
     * @return 结果
     */
    public int removeMember(java.util.Map<String, Object> params);

    /**
     * 根据群组ID删除所有成员
     *
     * @param groupId 群组ID
     * @return 结果
     */
    public int deleteMembersByGroupId(Long groupId);
}
