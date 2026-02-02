package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

/**
 * 角色权限关联Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImRolePermissionMapper extends BaseMapper<ImRolePermission> {

    /**
     * 删除角色的所有权限
     *
     * @param roleId 角色ID
     * @return 删除行数
     */
    @Delete("DELETE FROM im_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除权限的所有角色关联
     *
     * @param permissionId 权限ID
     * @return 删除行数
     */
    @Delete("DELETE FROM im_role_permission WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}
