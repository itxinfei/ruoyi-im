package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDepartmentMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门成员Mapper接口
 *
 * 用于操作部门成员数据
 *
 * @author ruoyi
 */
@Mapper
public interface ImDepartmentMemberMapper extends BaseMapper<ImDepartmentMember> {

    /**
     * 根据部门ID查询成员列表
     *
     * @param departmentId 部门ID
     * @return 成员列表
     */
    List<com.ruoyi.im.vo.organization.ImDepartmentMemberVO> selectMembersByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据用户ID查询用户所属部门列表
     *
     * @param userId 用户ID
     * @return 部门成员关系列表
     */
    List<ImDepartmentMember> selectDepartmentsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的主部门
     *
     * @param userId 用户ID
     * @return 主部门成员关系
     */
    ImDepartmentMember selectPrimaryDepartmentByUserId(@Param("userId") Long userId);

    /**
     * 根据部门ID和用户ID查询成员关系
     *
     * @param departmentId 部门ID
     * @param userId 用户ID
     * @return 成员关系
     */
    ImDepartmentMember selectByDepartmentIdAndUserId(@Param("departmentId") Long departmentId,
                                                       @Param("userId") Long userId);

    /**
     * 更新用户的主部门
     *
     * @param userId 用户ID
     * @param departmentId 部门ID
     * @return 更新行数
     */
    int updatePrimaryDepartment(@Param("userId") Long userId, @Param("departmentId") Long departmentId);

    /**
     * 根据部门ID删除所有成员
     *
     * @param departmentId 部门ID
     * @return 删除行数
     */
    int deleteMembersByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据用户ID删除所有部门关系
     *
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteDepartmentsByUserId(@Param("userId") Long userId);

    /**
     * 统计部门成员数量
     *
     * @param departmentId 部门ID
     * @return 成员数量
     */
    int countMembersByDepartmentId(@Param("departmentId") Long departmentId);
}
