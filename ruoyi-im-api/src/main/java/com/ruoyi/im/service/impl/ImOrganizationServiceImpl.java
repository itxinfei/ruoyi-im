package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.domain.ImDepartment;
import com.ruoyi.im.domain.ImDepartmentMember;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.organization.ImDepartmentCreateRequest;
import com.ruoyi.im.dto.organization.ImDepartmentMemberAddRequest;
import com.ruoyi.im.dto.organization.ImDepartmentUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImDepartmentMapper;
import com.ruoyi.im.mapper.ImDepartmentMemberMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.service.ImOrganizationService;
import com.ruoyi.im.vo.organization.ImDepartmentMemberVO;
import com.ruoyi.im.vo.organization.ImDepartmentTreeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织架构服务实现
 *
 * @author ruoyi
 */
@Service
public class ImOrganizationServiceImpl implements ImOrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(ImOrganizationServiceImpl.class);

    private final ImDepartmentMapper imDepartmentMapper;
    private final ImDepartmentMemberMapper imDepartmentMemberMapper;
    private final ImUserMapper imUserMapper;
    private final ImRedisUtil imRedisUtil;

    /**
     * 构造器注入依赖
     *
     * @param imDepartmentMapper        部门Mapper
     * @param imDepartmentMemberMapper  部门成员Mapper
     * @param imUserMapper               用户Mapper
     * @param imRedisUtil                Redis工具类
     */
    public ImOrganizationServiceImpl(ImDepartmentMapper imDepartmentMapper,
                                      ImDepartmentMemberMapper imDepartmentMemberMapper,
                                      ImUserMapper imUserMapper,
                                      ImRedisUtil imRedisUtil) {
        this.imDepartmentMapper = imDepartmentMapper;
        this.imDepartmentMemberMapper = imDepartmentMemberMapper;
        this.imUserMapper = imUserMapper;
        this.imRedisUtil = imRedisUtil;
    }

    @Override
    public List<ImDepartmentTreeVO> getDepartmentTree() {
        List<ImDepartment> allDepartments = imDepartmentMapper.selectAllDepartments();
        List<ImDepartmentTreeVO> treeList = new ArrayList<>();

        for (ImDepartment department : allDepartments) {
            ImDepartmentTreeVO treeVO = new ImDepartmentTreeVO();
            BeanUtils.copyProperties(department, treeVO);

            ImUser leader = imUserMapper.selectImUserById(department.getLeaderId());
            if (leader != null) {
                treeVO.setLeaderName(leader.getNickname());
            }

            int memberCount = imDepartmentMemberMapper.countMembersByDepartmentId(department.getId());
            treeVO.setMemberCount(memberCount);

            if (department.getParentId().equals(0L)) {
                treeVO.setChildren(buildChildrenTree(department.getId(), allDepartments));
                treeList.add(treeVO);
            }
        }

        return treeList;
    }

    private List<ImDepartmentTreeVO> buildChildrenTree(Long parentId, List<ImDepartment> allDepartments) {
        List<ImDepartmentTreeVO> children = new ArrayList<>();

        for (ImDepartment department : allDepartments) {
            if (department.getParentId().equals(parentId)) {
                ImDepartmentTreeVO treeVO = new ImDepartmentTreeVO();
                BeanUtils.copyProperties(department, treeVO);

                ImUser leader = imUserMapper.selectImUserById(department.getLeaderId());
                if (leader != null) {
                    treeVO.setLeaderName(leader.getNickname());
                }

                int memberCount = imDepartmentMemberMapper.countMembersByDepartmentId(department.getId());
                treeVO.setMemberCount(memberCount);

                treeVO.setChildren(buildChildrenTree(department.getId(), allDepartments));
                children.add(treeVO);
            }
        }

        return children;
    }

    @Override
    public ImDepartment getDepartmentById(Long departmentId) {
        ImDepartment department = imDepartmentMapper.selectById(departmentId);
        if (department == null) {
            throw new BusinessException("DEPARTMENT_NOT_EXIST", "Department does not exist");
        }
        return department;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDepartment(ImDepartmentCreateRequest request) {
        if (checkDepartmentNameExists(request.getName(), request.getParentId(), null)) {
            throw new BusinessException("DEPARTMENT_NAME_EXISTS", "Department name already exists");
        }

                    ImDepartment parentDepartment = null;
                if (!request.getParentId().equals(0L)) {
                    parentDepartment = imDepartmentMapper.selectById(request.getParentId());
                    if (parentDepartment == null) {
                        throw new BusinessException("PARENT_DEPARTMENT_NOT_EXIST", "Parent department does not exist");
                    }
                }
        ImDepartment department = new ImDepartment();
        BeanUtils.copyProperties(request, department);
        department.setStatus(0);
        department.setDelFlag(0);
        department.setCreateTime(LocalDateTime.now());
        department.setUpdateTime(LocalDateTime.now());

        if (parentDepartment != null) {
            department.setAncestors(parentDepartment.getAncestors() + "," + parentDepartment.getId());
        } else {
            department.setAncestors("0");
        }

        if (department.getOrderNum() == null) {
            department.setOrderNum(0);
        }

        int result = imDepartmentMapper.insert(department);
        if (result <= 0) {
            throw new BusinessException("CREATE_DEPARTMENT_FAILED", "Create department failed");
        }

        return department.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDepartment(ImDepartmentUpdateRequest request) {
        ImDepartment department = imDepartmentMapper.selectById(request.getId());
        if (department == null) {
            throw new BusinessException("DEPARTMENT_NOT_EXIST", "部门不存在");
        }

        if (checkDepartmentNameExists(request.getName(), request.getParentId(), request.getId())) {
            throw new BusinessException("DEPARTMENT_NAME_EXISTS", "部门名称已存在");
        }

        if (!request.getParentId().equals(department.getParentId())) {
            if (request.getId().equals(request.getParentId())) {
                throw new BusinessException("CANNOT_SET_SELF_AS_PARENT", "Cannot set self as parent department");
            }

            ImDepartment newParent = imDepartmentMapper.selectById(request.getParentId());
            if (newParent == null) {
                throw new BusinessException("PARENT_DEPARTMENT_NOT_EXIST", "Parent department does not exist");
            }

            List<Long> childrenIds = imDepartmentMapper.selectDepartmentAndChildrenIds(request.getId());
            if (childrenIds.contains(request.getParentId())) {
                throw new BusinessException("CANNOT_MOVE_TO_CHILD", "Cannot move department to its child department");
            }

            String newAncestors;
            if (request.getParentId().equals(0L)) {
                newAncestors = "0";
            } else {
                newAncestors = newParent.getAncestors() + "," + newParent.getId();
            }

            updateChildrenAncestors(request.getId(), newAncestors);
        }

        BeanUtils.copyProperties(request, department);
        department.setUpdateTime(LocalDateTime.now());

        int result = imDepartmentMapper.updateById(department);
        if (result <= 0) {
            throw new BusinessException("UPDATE_DEPARTMENT_FAILED", "Update department failed");
        }
    }

    private void updateChildrenAncestors(Long departmentId, String newAncestors) {
        List<ImDepartment> children = imDepartmentMapper.selectChildrenByParentId(departmentId);
        for (ImDepartment child : children) {
            String childAncestors = newAncestors + "," + child.getId();
            child.setAncestors(childAncestors);
            imDepartmentMapper.updateById(child);
            updateChildrenAncestors(child.getId(), childAncestors);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long departmentId) {
        ImDepartment department = imDepartmentMapper.selectById(departmentId);
        if (department == null) {
            throw new BusinessException("DEPARTMENT_NOT_EXIST", "部门不存在");
        }

        int childrenCount = imDepartmentMapper.countChildrenByDepartmentId(departmentId);
        if (childrenCount > 0) {
            throw new BusinessException("DEPARTMENT_HAS_CHILDREN", "Department has sub-departments, cannot delete");
        }

        int memberCount = imDepartmentMemberMapper.countMembersByDepartmentId(departmentId);
        if (memberCount > 0) {
            throw new BusinessException("DEPARTMENT_HAS_MEMBERS", "Department has members, cannot delete");
        }

        department.setDelFlag(1);
        department.setUpdateTime(LocalDateTime.now());

        int result = imDepartmentMapper.updateById(department);
        if (result <= 0) {
            throw new BusinessException("DELETE_DEPARTMENT_FAILED", "Delete department failed");
        }
    }

    @Override
    public List<ImDepartmentMemberVO> getDepartmentMembers(Long departmentId) {
        List<ImDepartmentMemberVO> members = imDepartmentMemberMapper.selectMembersByDepartmentId(departmentId);

        // 为每个成员添加在线状态
        if (members != null && !members.isEmpty()) {
            for (ImDepartmentMemberVO member : members) {
                try {
                    boolean isOnline = imRedisUtil.isOnlineUser(member.getUserId());
                    member.setOnline(isOnline);
                } catch (Exception e) {
                    logger.warn("获取用户在线状态失败: userId={}", member.getUserId(), e);
                    member.setOnline(false); // 降级为离线
                }
            }
        }

        return members;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDepartmentMember(ImDepartmentMemberAddRequest request) {
        ImDepartment department = imDepartmentMapper.selectById(request.getDepartmentId());
        if (department == null) {
            throw new BusinessException("DEPARTMENT_NOT_EXIST", "部门不存在");
        }

        ImUser user = imUserMapper.selectImUserById(request.getUserId());
        if (user == null) {
            throw new BusinessException("USER_NOT_EXIST", "User does not exist");
        }

        ImDepartmentMember existingMember = imDepartmentMemberMapper.selectByDepartmentIdAndUserId(
                request.getDepartmentId(), request.getUserId());
        if (existingMember != null) {
            throw new BusinessException("MEMBER_ALREADY_EXISTS", "Member already exists in this department");
        }

        if (request.getIsPrimary() != null && request.getIsPrimary().equals(1)) {
            imDepartmentMemberMapper.updatePrimaryDepartment(request.getUserId(), request.getDepartmentId());
        }

        ImDepartmentMember member = new ImDepartmentMember();
        BeanUtils.copyProperties(request, member);
        member.setJoinTime(LocalDateTime.now());
        member.setStatus(0);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());

        if (member.getIsPrimary() == null) {
            member.setIsPrimary(0);
        }

        int result = imDepartmentMemberMapper.insert(member);
        if (result <= 0) {
            throw new BusinessException("ADD_MEMBER_FAILED", "Add member failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDepartmentMember(Long departmentId, Long userId) {
        ImDepartmentMember member = imDepartmentMemberMapper.selectByDepartmentIdAndUserId(departmentId, userId);
        if (member == null) {
            throw new BusinessException("MEMBER_NOT_EXIST", "Member does not exist");
        }

        int result = imDepartmentMemberMapper.deleteById(member.getId());
        if (result <= 0) {
            throw new BusinessException("REMOVE_MEMBER_FAILED", "Remove member failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPrimaryDepartment(Long userId, Long departmentId) {
        ImDepartment department = imDepartmentMapper.selectById(departmentId);
        if (department == null) {
            throw new BusinessException("DEPARTMENT_NOT_EXIST", "部门不存在");
        }

        ImDepartmentMember member = imDepartmentMemberMapper.selectByDepartmentIdAndUserId(departmentId, userId);
        if (member == null) {
            throw new BusinessException("MEMBER_NOT_EXIST", "Member does not exist");
        }

        imDepartmentMemberMapper.updatePrimaryDepartment(userId, departmentId);

        member.setIsPrimary(1);
        member.setUpdateTime(LocalDateTime.now());

        int result = imDepartmentMemberMapper.updateById(member);
        if (result <= 0) {
            throw new BusinessException("SET_PRIMARY_DEPARTMENT_FAILED", "Set primary department failed");
        }
    }

    @Override
    public List<ImDepartment> getUserDepartments(Long userId) {
        List<ImDepartmentMember> memberList = imDepartmentMemberMapper.selectDepartmentsByUserId(userId);
        List<Long> departmentIds = memberList.stream()
                .map(ImDepartmentMember::getDepartmentId)
                .collect(Collectors.toList());

        if (departmentIds.isEmpty()) {
            return new ArrayList<>();
        }

        return imDepartmentMapper.selectBatchIds(departmentIds);
    }

    @Override
    public ImDepartment getUserPrimaryDepartment(Long userId) {
        ImDepartmentMember member = imDepartmentMemberMapper.selectPrimaryDepartmentByUserId(userId);
        if (member == null) {
            return null;
        }

        return imDepartmentMapper.selectById(member.getDepartmentId());
    }

    @Override
    public boolean checkDepartmentNameExists(String name, Long parentId, Long excludeId) {
        QueryWrapper<ImDepartment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ImDepartment::getName, name);
        queryWrapper.eq(ImDepartment::getParentId, parentId);
        queryWrapper.eq(ImDepartment::getDelFlag, 0);

        if (excludeId != null) {
            queryWrapper.ne(ImDepartment::getId, excludeId);
        }

        Long count = imDepartmentMapper.selectCount(queryWrapper);
        return count != null && count > 0;
    }

    @Override
    public List<Long> getDepartmentAndChildrenIds(Long departmentId) {
        return imDepartmentMapper.selectDepartmentAndChildrenIds(departmentId);
    }
}
