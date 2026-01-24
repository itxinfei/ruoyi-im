package com.ruoyi.web.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.domain.ImDepartmentMember;
import com.ruoyi.web.mapper.ImDepartmentMemberMapper;
import com.ruoyi.web.service.ImDepartmentMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 部门成员Service实现
 *
 * @author ruoyi
 */
@Service
public class ImDepartmentMemberServiceImpl implements ImDepartmentMemberService {

    @Autowired
    private ImDepartmentMemberMapper imDepartmentMemberMapper;

    @Override
    public ImDepartmentMember selectImDepartmentMemberById(Long id) {
        return imDepartmentMemberMapper.selectImDepartmentMemberById(id);
    }

    @Override
    public List<ImDepartmentMember> selectImDepartmentMemberList(ImDepartmentMember imDepartmentMember) {
        return imDepartmentMemberMapper.selectImDepartmentMemberList(imDepartmentMember);
    }

    @Override
    public List<ImDepartmentMember> selectMembersByDepartmentId(Long departmentId) {
        return imDepartmentMemberMapper.selectMembersByDepartmentId(departmentId);
    }

    @Override
    public List<ImDepartmentMember> selectDepartmentsByUserId(Long userId) {
        return imDepartmentMemberMapper.selectDepartmentsByUserId(userId);
    }

    @Override
    public ImDepartmentMember selectPrimaryDepartmentByUserId(Long userId) {
        return imDepartmentMemberMapper.selectPrimaryDepartmentByUserId(userId);
    }

    @Override
    public ImDepartmentMember selectByDepartmentIdAndUserId(Long departmentId, Long userId) {
        return imDepartmentMemberMapper.selectByDepartmentIdAndUserId(departmentId, userId);
    }

    @Override
    @Transactional
    public int insertImDepartmentMember(ImDepartmentMember imDepartmentMember) {
        // 检查用户是否已在部门中
        int count = imDepartmentMemberMapper.checkUserExistsInDepartment(
                imDepartmentMember.getDepartmentId(),
                imDepartmentMember.getUserId()
        );
        if (count > 0) {
            throw new ServiceException("该用户已在此部门中");
        }

        // 设置创建时间
        imDepartmentMember.setCreateTime(new Date());
        if (imDepartmentMember.getStatus() == null) {
            imDepartmentMember.setStatus(0); // 默认在职
        }
        if (imDepartmentMember.getIsPrimary() == null) {
            imDepartmentMember.setIsPrimary(0); // 默认非主部门
        }

        // 如果设置为主部门，需要先清除该用户的其他主部门标记
        if (imDepartmentMember.getIsPrimary() == 1) {
            imDepartmentMemberMapper.updatePrimaryDepartment(
                    imDepartmentMember.getUserId(),
                    imDepartmentMember.getDepartmentId()
            );
        }

        return imDepartmentMemberMapper.insertImDepartmentMember(imDepartmentMember);
    }

    @Override
    @Transactional
    public int updateImDepartmentMember(ImDepartmentMember imDepartmentMember) {
        // 查询原记录
        ImDepartmentMember oldMember = imDepartmentMemberMapper.selectImDepartmentMemberById(
                imDepartmentMember.getId()
        );
        if (oldMember == null) {
            throw new ServiceException("部门成员关系不存在");
        }

        // 如果修改了主部门标记
        if (imDepartmentMember.getIsPrimary() != null
                && imDepartmentMember.getIsPrimary() == 1
                && !oldMember.getIsPrimary().equals(1)) {
            // 清除该用户的其他主部门标记
            imDepartmentMemberMapper.updatePrimaryDepartment(
                    oldMember.getUserId(),
                    oldMember.getDepartmentId()
            );
        }

        return imDepartmentMemberMapper.updateImDepartmentMember(imDepartmentMember);
    }

    @Override
    public int deleteImDepartmentMemberByIds(Long[] ids) {
        return imDepartmentMemberMapper.deleteImDepartmentMemberByIds(ids);
    }

    @Override
    public int deleteImDepartmentMemberById(Long id) {
        return imDepartmentMemberMapper.deleteImDepartmentMemberById(id);
    }

    @Override
    public int deleteMembersByDepartmentId(Long departmentId) {
        return imDepartmentMemberMapper.deleteMembersByDepartmentId(departmentId);
    }

    @Override
    public int deleteDepartmentsByUserId(Long userId) {
        return imDepartmentMemberMapper.deleteDepartmentsByUserId(userId);
    }

    @Override
    @Transactional
    public int setPrimaryDepartment(Long userId, Long departmentId) {
        // 检查部门成员关系是否存在
        ImDepartmentMember member = imDepartmentMemberMapper.selectByDepartmentIdAndUserId(
                departmentId, userId
        );
        if (member == null) {
            throw new ServiceException("用户不在此部门中");
        }

        // 更新主部门
        return imDepartmentMemberMapper.updatePrimaryDepartment(userId, departmentId);
    }

    @Override
    public int countMembersByDepartmentId(Long departmentId) {
        return imDepartmentMemberMapper.countMembersByDepartmentId(departmentId);
    }

    @Override
    public Map<String, Object> getDepartmentMemberStatistics() {
        return imDepartmentMemberMapper.getDepartmentMemberStatistics();
    }

    @Override
    public int checkUserExistsInDepartment(Long departmentId, Long userId) {
        return imDepartmentMemberMapper.checkUserExistsInDepartment(departmentId, userId);
    }
}
