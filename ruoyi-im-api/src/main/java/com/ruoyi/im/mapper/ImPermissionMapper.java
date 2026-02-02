package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImPermissionMapper extends BaseMapper<ImPermission> {

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM im_permission p " +
            "INNER JOIN im_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1 " +
            "ORDER BY p.sort_order ASC")
    List<ImPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.* FROM im_permission p " +
            "INNER JOIN im_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN im_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 " +
            "ORDER BY p.sort_order ASC")
    List<ImPermission> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 查询所有菜单权限
     *
     * @return 权限列表
     */
    @Select("SELECT * FROM im_permission WHERE status = 1 ORDER BY sort_order ASC")
    List<ImPermission> selectAllPermissions();
}
