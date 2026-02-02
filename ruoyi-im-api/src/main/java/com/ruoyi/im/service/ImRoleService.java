package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImRole;
import com.ruoyi.im.dto.role.ImRoleCreateRequest;
import com.ruoyi.im.dto.role.ImRoleUpdateRequest;
import com.ruoyi.im.vo.role.ImPermissionVO;
import com.ruoyi.im.vo.role.ImRoleVO;

import java.util.List;
import java.util.Map;

/**
 * 角色服务接口
 *
 * @author ruoyi
 */
public interface ImRoleService extends IService<ImRole> {

    /**
     * 分页查询角色列表
     *
     * @param params 查询参数
     * @return 分页结果
     */
    Map<String, Object> getRoleList(Map<String, Object> params);

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色视图对象
     */
    ImRoleVO getRoleDetail(Long roleId);

    /**
     * 创建角色
     *
     * @param request 创建请求
     * @return 角色ID
     */
    Long createRole(ImRoleCreateRequest request);

    /**
     * 更新角色
     *
     * @param request 更新请求
     */
    void updateRole(ImRoleUpdateRequest request);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);

    /**
     * 获取所有权限（树形结构）
     *
     * @return 权限树
     */
    List<ImPermissionVO> getPermissionTree();

    /**
     * 获取角色的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 分配角色权限
     *
     * @param roleId       角色ID
     * @param permissionIds 权限ID列表
     */
    void assignRolePermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色成员列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> getRoleMemberIds(Long roleId);

    /**
     * 添加角色成员
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     */
    void addRoleMembers(Long roleId, List<Long> userIds);

    /**
     * 移除角色成员
     *
     * @param roleId 角色ID
     * @param userId 用户ID
     */
    void removeRoleMember(Long roleId, Long userId);
}
