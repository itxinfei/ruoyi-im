package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImUserRoleMapper extends BaseMapper<ImUserRole> {

    /**
     * 删除用户的所有角色
     *
     * @param userId 用户ID
     * @return 删除行数
     */
    @Delete("DELETE FROM im_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除角色的所有用户
     *
     * @param roleId 角色ID
     * @return 删除行数
     */
    @Delete("DELETE FROM im_user_role WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
