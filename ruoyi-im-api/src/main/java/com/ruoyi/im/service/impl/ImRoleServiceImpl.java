package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.domain.ImPermission;
import com.ruoyi.im.domain.ImRole;
import com.ruoyi.im.domain.ImRolePermission;
import com.ruoyi.im.domain.ImUserRole;
import com.ruoyi.im.dto.role.ImRoleCreateRequest;
import com.ruoyi.im.dto.role.ImRoleUpdateRequest;
import com.ruoyi.im.mapper.ImPermissionMapper;
import com.ruoyi.im.mapper.ImRoleMapper;
import com.ruoyi.im.mapper.ImRolePermissionMapper;
import com.ruoyi.im.mapper.ImUserRoleMapper;
import com.ruoyi.im.service.ImRoleService;
import com.ruoyi.im.vo.role.ImPermissionVO;
import com.ruoyi.im.vo.role.ImRoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author ruoyi
 */
@Service
public class ImRoleServiceImpl extends ServiceImpl<ImRoleMapper, ImRole> implements ImRoleService {

    private final ImRolePermissionMapper rolePermissionMapper;
    private final ImUserRoleMapper userRoleMapper;
    private final ImPermissionMapper permissionMapper;

    public ImRoleServiceImpl(
            ImRolePermissionMapper rolePermissionMapper,
            ImUserRoleMapper userRoleMapper,
            ImPermissionMapper permissionMapper) {
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Map<String, Object> getRoleList(Map<String, Object> params) {
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 20);
        String keyword = (String) params.get("keyword");
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;

        // 构建查询条件
        LambdaQueryWrapper<ImRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImRole::getDelFlag, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ImRole::getRoleName, keyword)
                    .or().like(ImRole::getRoleCode, keyword));
        }
        if (status != null) {
            wrapper.eq(ImRole::getStatus, status);
        }
        wrapper.orderByAsc(ImRole::getSortOrder).orderByDesc(ImRole::getCreateTime);

        // 分页查询
        long total = this.count(wrapper);
        int offset = (pageNum - 1) * pageSize;
        wrapper.last("LIMIT " + offset + "," + pageSize);
        List<ImRole> roles = this.list(wrapper);

        // 转换为 VO 并填充成员数量
        List<ImRoleVO> voList = roles.stream().map(role -> {
            ImRoleVO vo = toVO(role);
            // 查询成员数量
            int memberCount = Math.toIntExact(userRoleMapper.selectCount(
                    new LambdaQueryWrapper<ImUserRole>().eq(ImUserRole::getRoleId, role.getId())
            ));
            vo.setMemberCount(memberCount);
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", voList);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public ImRoleVO getRoleDetail(Long roleId) {
        ImRole role = this.getById(roleId);
        if (role == null || role.getDelFlag() == 1) {
            throw new BusinessException("角色不存在");
        }
        ImRoleVO vo = toVO(role);

        // 获取权限ID列表
        List<Long> permissionIds = getRolePermissionIds(roleId);
        vo.setPermissionIds(permissionIds);

        // 获取成员数量
        int memberCount = Math.toIntExact(userRoleMapper.selectCount(
                new LambdaQueryWrapper<ImUserRole>().eq(ImUserRole::getRoleId, roleId)
        ));
        vo.setMemberCount(memberCount);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(ImRoleCreateRequest request) {
        // 检查角色编码是否存在
        ImRole existingRole = baseMapper.selectByRoleCode(request.getRoleCode());
        if (existingRole != null) {
            throw new BusinessException("角色编码已存在");
        }

        ImRole role = new ImRole();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setDataScope(request.getDataScope());
        role.setBuiltin(0);
        role.setStatus(1);
        role.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        role.setColor(request.getColor());
        role.setDelFlag(0);

        this.save(role);

        // 分配权限
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Long> permissionIds = Arrays.stream(request.getPermissionIds().split(","))
                    .map(Long::parseLong).collect(Collectors.toList());
            assignRolePermissions(role.getId(), permissionIds);
        }

        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(ImRoleUpdateRequest request) {
        ImRole role = this.getById(request.getId());
        if (role == null || role.getDelFlag() == 1) {
            throw new BusinessException("角色不存在");
        }
        if (role.getBuiltin() == 1) {
            throw new BusinessException("系统内置角色不允许修改");
        }

        if (request.getRoleName() != null) {
            role.setRoleName(request.getRoleName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getDataScope() != null) {
            role.setDataScope(request.getDataScope());
        }
        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }
        if (request.getSortOrder() != null) {
            role.setSortOrder(request.getSortOrder());
        }
        if (request.getColor() != null) {
            role.setColor(request.getColor());
        }

        this.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        ImRole role = this.getById(roleId);
        if (role == null || role.getDelFlag() == 1) {
            throw new BusinessException("角色不存在");
        }
        if (role.getBuiltin() == 1) {
            throw new BusinessException("系统内置角色不允许删除");
        }

        // 逻辑删除
        role.setDelFlag(1);
        this.updateById(role);

        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(roleId);

        // 删除用户角色关联
        userRoleMapper.deleteByRoleId(roleId);
    }

    @Override
    public List<ImPermissionVO> getPermissionTree() {
        List<ImPermission> allPermissions = permissionMapper.selectAllPermissions();

        // 转换为 VO
        List<ImPermissionVO> voList = allPermissions.stream().map(p -> {
            ImPermissionVO vo = new ImPermissionVO();
            vo.setId(p.getId());
            vo.setParentId(p.getParentId());
            vo.setPermissionName(p.getPermissionName());
            vo.setPermissionCode(p.getPermissionCode());
            vo.setPermissionType(p.getPermissionType());
            vo.setPermissionTypeName(p.getPermissionType() == 1 ? "菜单" : "按钮");
            vo.setRoutePath(p.getRoutePath());
            vo.setComponent(p.getComponent());
            vo.setIcon(p.getIcon());
            vo.setSortOrder(p.getSortOrder());
            vo.setVisible(p.getVisible() == 1);
            vo.setStatus(p.getStatus());
            vo.setType(p.getPermissionType() == 2 ? "button" : "");
            return vo;
        }).collect(Collectors.toList());

        // 构建树形结构
        return buildPermissionTree(voList, 0L);
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        List<ImPermission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
        return permissions.stream().map(ImPermission::getId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        ImRole role = this.getById(roleId);
        if (role == null || role.getDelFlag() == 1) {
            throw new BusinessException("角色不存在");
        }
        if (role.getBuiltin() == 1) {
            throw new BusinessException("系统内置角色不允许修改权限");
        }

        // 删除原有权限
        rolePermissionMapper.deleteByRoleId(roleId);

        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<ImRolePermission> rolePermissions = permissionIds.stream().map(permissionId -> {
                ImRolePermission rp = new ImRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                return rp;
            }).collect(Collectors.toList());
            rolePermissions.forEach(rolePermissionMapper::insert);
        }
    }

    @Override
    public List<Long> getRoleMemberIds(Long roleId) {
        List<ImUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<ImUserRole>().eq(ImUserRole::getRoleId, roleId)
        );
        return userRoles.stream().map(ImUserRole::getUserId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoleMembers(Long roleId, List<Long> userIds) {
        ImRole role = this.getById(roleId);
        if (role == null || role.getDelFlag() == 1) {
            throw new BusinessException("角色不存在");
        }

        if (userIds != null && !userIds.isEmpty()) {
            List<ImUserRole> userRoles = userIds.stream().map(userId -> {
                ImUserRole ur = new ImUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            }).collect(Collectors.toList());
            userRoles.forEach(userRoleMapper::insert);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleMember(Long roleId, Long userId) {
        userRoleMapper.delete(new LambdaQueryWrapper<ImUserRole>()
                .eq(ImUserRole::getRoleId, roleId)
                .eq(ImUserRole::getUserId, userId)
        );
    }

    /**
     * 构建权限树
     */
    private List<ImPermissionVO> buildPermissionTree(List<ImPermissionVO> permissions, Long parentId) {
        List<ImPermissionVO> result = new ArrayList<>();
        for (ImPermissionVO permission : permissions) {
            if (Objects.equals(permission.getParentId(), parentId)) {
                permission.setChildren(buildPermissionTree(permissions, permission.getId()));
                result.add(permission);
            }
        }
        return result;
    }

    /**
     * Entity 转 VO
     */
    private ImRoleVO toVO(ImRole role) {
        ImRoleVO vo = new ImRoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setDataScope(role.getDataScope());
        vo.setDataScopeDesc(getDataScopeDesc(role.getDataScope()));
        vo.setBuiltin(role.getBuiltin() == 1);
        vo.setStatus(role.getStatus());
        vo.setSortOrder(role.getSortOrder());
        vo.setColor(role.getColor());
        vo.setCreateTime(role.getCreateTime());
        vo.setUpdateTime(role.getUpdateTime());
        return vo;
    }

    /**
     * 获取数据范围描述
     */
    private String getDataScopeDesc(Integer dataScope) {
        if (dataScope == null) {
            return "未设置";
        }
        switch (dataScope) {
            case 1:
                return "全部数据";
            case 2:
                return "本部门数据";
            case 3:
                return "本部门及子部门数据";
            case 4:
                return "仅本人数据";
            default:
                return "未设置";
        }
    }
}
