package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImDepartment;
import com.ruoyi.web.mapper.ImDepartmentMapper;
import com.ruoyi.web.service.ImDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 部门Service实现
 *
 * @author ruoyi
 */
@Service
public class ImDepartmentServiceImpl implements ImDepartmentService {

    @Autowired
    private ImDepartmentMapper imDepartmentMapper;

    @Override
    public ImDepartment selectImDepartmentById(Long id) {
        return imDepartmentMapper.selectImDepartmentById(id);
    }

    @Override
    public List<ImDepartment> selectImDepartmentList(ImDepartment imDepartment) {
        return imDepartmentMapper.selectImDepartmentList(imDepartment);
    }

    @Override
    public List<ImDepartment> selectImDepartmentTree(ImDepartment imDepartment) {
        List<ImDepartment> departments = imDepartmentMapper.selectImDepartmentTree(imDepartment);
        return buildTree(departments);
    }

    /**
     * 构建树形结构
     */
    private List<ImDepartment> buildTree(List<ImDepartment> departments) {
        List<ImDepartment> tree = new ArrayList<>();
        for (ImDepartment dept : departments) {
            if (dept.getParentId() == null || dept.getParentId() == 0) {
                tree.add(findChildren(dept, departments));
            }
        }
        return tree;
    }

    /**
     * 递归查找子节点
     */
    private ImDepartment findChildren(ImDepartment parent, List<ImDepartment> departments) {
        for (ImDepartment dept : departments) {
            if (parent.getId().equals(dept.getParentId())) {
                parent.getChildren().add(findChildren(dept, departments));
            }
        }
        return parent;
    }

    @Override
    public int insertImDepartment(ImDepartment imDepartment) {
        return imDepartmentMapper.insertImDepartment(imDepartment);
    }

    @Override
    public int updateImDepartment(ImDepartment imDepartment) {
        return imDepartmentMapper.updateImDepartment(imDepartment);
    }

    @Override
    public int deleteImDepartmentByIds(Long[] ids) {
        return imDepartmentMapper.deleteImDepartmentByIds(ids);
    }

    @Override
    public int deleteImDepartmentById(Long id) {
        return imDepartmentMapper.deleteImDepartmentById(id);
    }

    @Override
    public int updateDepartmentStatus(Long id, Integer status) {
        return imDepartmentMapper.updateDepartmentStatus(id, status);
    }

    @Override
    public int checkHasChildren(Long id) {
        return imDepartmentMapper.checkHasChildren(id);
    }

    @Override
    public Map<String, Object> getDepartmentStatistics() {
        return imDepartmentMapper.getDepartmentStatistics();
    }
}
