package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.domain.ImGroupPermission;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.mapper.ImGroupPermissionMapper;
import com.ruoyi.im.service.ImGroupMemberService;
import com.ruoyi.im.service.ImGroupPermissionService;
import com.ruoyi.im.vo.group.GroupPermissionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组权限服务实现类
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class ImGroupPermissionServiceImpl implements ImGroupPermissionService {

    private final ImGroupPermissionMapper permissionMapper;
    private final ImGroupMemberMapper groupMemberMapper;
    private final ImGroupMapper groupMapper;

    public ImGroupPermissionServiceImpl(ImGroupPermissionMapper permissionMapper,
                                        ImGroupMemberMapper groupMemberMapper,
                                        ImGroupMapper groupMapper) {
        this.permissionMapper = permissionMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.groupMapper = groupMapper;
    }

    /**
     * 默认权限配置
     */
    private static final Map<String, Map<String, Integer>> DEFAULT_PERMISSIONS = new HashMap<String, Map<String, Integer>>() {
        {
            // 群主默认拥有所有权限
            Map<String, Integer> owner = new HashMap<>();
            owner.put("canInvite", 1);
            owner.put("canRemove", 1);
            owner.put("canMute", 1);
            owner.put("canAnnounce", 1);
            owner.put("canUpload", 1);
            owner.put("canEditGroup", 1);
            owner.put("canKick", 1);
            owner.put("canSetAdmin", 1);
            owner.put("canDisband", 1);
            put("OWNER", owner);

            // 管理员默认权限（不能设置管理员和解散群组）
            Map<String, Integer> admin = new HashMap<>();
            admin.put("canInvite", 1);
            admin.put("canRemove", 0);
            admin.put("canMute", 1);
            admin.put("canAnnounce", 1);
            admin.put("canUpload", 1);
            admin.put("canEditGroup", 1);
            admin.put("canKick", 1);
            admin.put("canSetAdmin", 0);
            admin.put("canDisband", 0);
            put("ADMIN", admin);

            // 普通成员默认权限（只能邀请和上传）
            Map<String, Integer> member = new HashMap<>();
            member.put("canInvite", 1);
            member.put("canRemove", 0);
            member.put("canMute", 0);
            member.put("canAnnounce", 0);
            member.put("canUpload", 1);
            member.put("canEditGroup", 0);
            member.put("canKick", 0);
            member.put("canSetAdmin", 0);
            member.put("canDisband", 0);
            put("MEMBER", member);
        }
    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initGroupPermissions(Long groupId) {
        if (groupId == null) {
            return;
        }

        // 为每个角色创建默认权限配置
        for (Map.Entry<String, Map<String, Integer>> entry : DEFAULT_PERMISSIONS.entrySet()) {
            String role = entry.getKey();
            Map<String, Integer> permissions = entry.getValue();

            ImGroupPermission groupPermission = new ImGroupPermission();
            groupPermission.setGroupId(groupId);
            groupPermission.setRole(role);
            groupPermission.setCanInvite(permissions.get("canInvite"));
            groupPermission.setCanRemove(permissions.get("canRemove"));
            groupPermission.setCanMute(permissions.get("canMute"));
            groupPermission.setCanAnnounce(permissions.get("canAnnounce"));
            groupPermission.setCanUpload(permissions.get("canUpload"));
            groupPermission.setCanEditGroup(permissions.get("canEditGroup"));
            groupPermission.setCanKick(permissions.get("canKick"));
            groupPermission.setCanSetAdmin(permissions.get("canSetAdmin"));
            groupPermission.setCanDisband(permissions.get("canDisband"));
            groupPermission.setCreateTime(LocalDateTime.now());
            groupPermission.setUpdateTime(LocalDateTime.now());

            permissionMapper.insert(groupPermission);
        }

        log.info("初始化群组权限配置: groupId={}", groupId);
    }

    @Override
    public List<GroupPermissionVO> getGroupPermissions(Long groupId) {
        if (groupId == null) {
            return new ArrayList<>();
        }

        List<ImGroupPermission> permissions = permissionMapper.selectByGroupId(groupId);
        List<GroupPermissionVO> result = new ArrayList<>();

        for (ImGroupPermission p : permissions) {
            GroupPermissionVO vo = new GroupPermissionVO();
            vo.setRole(p.getRole());
            vo.setRoleName(GroupPermissionVO.getRoleName(p.getRole()));
            vo.setCanInvite(p.getCanInvite());
            vo.setCanRemove(p.getCanRemove());
            vo.setCanMute(p.getCanMute());
            vo.setCanAnnounce(p.getCanAnnounce());
            vo.setCanUpload(p.getCanUpload());
            vo.setCanEditGroup(p.getCanEditGroup());
            vo.setCanKick(p.getCanKick());
            vo.setCanSetAdmin(p.getCanSetAdmin());
            vo.setCanDisband(p.getCanDisband());
            result.add(vo);
        }

        return result;
    }

    @Override
    public ImGroupPermission getPermission(Long groupId, String role) {
        if (groupId == null || role == null) {
            return null;
        }
        return permissionMapper.selectByGroupIdAndRole(groupId, role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(Long groupId, String role, Map<String, Integer> permissions, Long operatorId) {
        // 验证操作者是群主
        if (!isOwner(groupId, operatorId)) {
            throw new BusinessException("只有群主可以修改权限配置");
        }

        // 不能修改群主的权限
        if (ImGroupPermission.ROLE_OWNER.equals(role)) {
            throw new BusinessException("不能修改群主的权限");
        }

        ImGroupPermission permission = permissionMapper.selectByGroupIdAndRole(groupId, role);
        if (permission == null) {
            throw new BusinessException("权限配置不存在");
        }

        // 更新权限
        if (permissions.containsKey("canInvite")) {
            permission.setCanInvite(permissions.get("canInvite"));
        }
        if (permissions.containsKey("canRemove")) {
            permission.setCanRemove(permissions.get("canRemove"));
        }
        if (permissions.containsKey("canMute")) {
            permission.setCanMute(permissions.get("canMute"));
        }
        if (permissions.containsKey("canAnnounce")) {
            permission.setCanAnnounce(permissions.get("canAnnounce"));
        }
        if (permissions.containsKey("canUpload")) {
            permission.setCanUpload(permissions.get("canUpload"));
        }
        if (permissions.containsKey("canEditGroup")) {
            permission.setCanEditGroup(permissions.get("canEditGroup"));
        }
        if (permissions.containsKey("canKick")) {
            permission.setCanKick(permissions.get("canKick"));
        }
        if (permissions.containsKey("canSetAdmin")) {
            permission.setCanSetAdmin(permissions.get("canSetAdmin"));
        }
        if (permissions.containsKey("canDisband")) {
            permission.setCanDisband(permissions.get("canDisband"));
        }

        permission.setUpdateTime(LocalDateTime.now());
        permissionMapper.updateById(permission);

        log.info("更新群组权限配置: groupId={}, role={}, operatorId={}", groupId, role, operatorId);
    }

    @Override
    public boolean hasPermission(Long groupId, Long userId, String permission) {
        if (groupId == null || userId == null || permission == null) {
            return false;
        }

        // 获取用户在群组中的角色
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            return false;
        }

        String role = member.getRole();
        ImGroupPermission groupPermission = permissionMapper.selectByGroupIdAndRole(groupId, role);
        if (groupPermission == null) {
            // 如果没有配置，使用默认权限
            Map<String, Integer> defaultPerm = DEFAULT_PERMISSIONS.get(role);
            if (defaultPerm == null) {
                return false;
            }
            Integer value = defaultPerm.get(permission);
            return value != null && value == 1;
        }

        // 检查配置的权限
        switch (permission) {
            case "canInvite":
                return groupPermission.getCanInvite() != null && groupPermission.getCanInvite() == 1;
            case "canRemove":
                return groupPermission.getCanRemove() != null && groupPermission.getCanRemove() == 1;
            case "canMute":
                return groupPermission.getCanMute() != null && groupPermission.getCanMute() == 1;
            case "canAnnounce":
                return groupPermission.getCanAnnounce() != null && groupPermission.getCanAnnounce() == 1;
            case "canUpload":
                return groupPermission.getCanUpload() != null && groupPermission.getCanUpload() == 1;
            case "canEditGroup":
                return groupPermission.getCanEditGroup() != null && groupPermission.getCanEditGroup() == 1;
            case "canKick":
                return groupPermission.getCanKick() != null && groupPermission.getCanKick() == 1;
            case "canSetAdmin":
                return groupPermission.getCanSetAdmin() != null && groupPermission.getCanSetAdmin() == 1;
            case "canDisband":
                return groupPermission.getCanDisband() != null && groupPermission.getCanDisband() == 1;
            default:
                return false;
        }
    }

    @Override
    public boolean isOwner(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return false;
        }

        // 先检查群主字段
        ImGroup group = groupMapper.selectById(groupId);
        if (group != null && userId.equals(group.getOwnerId())) {
            return true;
        }

        // 再检查成员表中的角色
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        return member != null && ImGroupPermission.ROLE_OWNER.equals(member.getRole());
    }

    @Override
    public boolean isAdminOrOwner(Long groupId, Long userId) {
        if (isOwner(groupId, userId)) {
            return true;
        }

        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        return member != null && ImGroupPermission.ROLE_ADMIN.equals(member.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetToDefault(Long groupId) {
        if (groupId == null) {
            return;
        }

        // 删除现有权限配置
        permissionMapper.deleteByGroupId(groupId);

        // 重新初始化默认权限
        initGroupPermissions(groupId);

        log.info("重置群组权限为默认配置: groupId={}", groupId);
    }
}
