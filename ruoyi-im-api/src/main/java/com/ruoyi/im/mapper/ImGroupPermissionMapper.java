package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImGroupPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 群组权限 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImGroupPermissionMapper extends BaseMapper<ImGroupPermission> {

    /**
     * 查询群组的所有角色权限配置
     *
     * @param groupId 群组ID
     * @return 权限配置列表
     */
    @Select("SELECT * FROM im_group_permission WHERE group_id = #{groupId} ORDER BY FIELD(role, 'OWNER', 'ADMIN', 'MEMBER')")
    List<ImGroupPermission> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询群组指定角色的权限配置
     *
     * @param groupId 群组ID
     * @param role 角色
     * @return 权限配置
     */
    @Select("SELECT * FROM im_group_permission WHERE group_id = #{groupId} AND role = #{role}")
    ImGroupPermission selectByGroupIdAndRole(@Param("groupId") Long groupId, @Param("role") String role);

    /**
     * 删除群组的所有权限配置
     *
     * @param groupId 群组ID
     * @return 删除数量
     */
    int deleteByGroupId(@Param("groupId") Long groupId);
}
