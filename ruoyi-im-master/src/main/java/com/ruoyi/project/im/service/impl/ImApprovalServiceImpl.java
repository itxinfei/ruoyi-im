package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImApproval;
import com.ruoyi.web.mapper.ImApprovalMapper;
import com.ruoyi.web.service.ImApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * IM审批Service实现（Admin模块专用）
 */
@Service
public class ImApprovalServiceImpl implements ImApprovalService {

    @Autowired
    private ImApprovalMapper approvalMapper;

    @Override
    public List<ImApproval> selectImApprovalList(ImApproval imApproval) {
        return approvalMapper.selectImApprovalList(imApproval);
    }

    @Override
    public Map<String, Object> getApprovalDetail(Long id) {
        return approvalMapper.getApprovalDetail(id);
    }

    @Override
    public ImApproval selectImApprovalById(Long id) {
        return approvalMapper.selectImApprovalById(id);
    }

    @Override
    public int insertImApproval(ImApproval imApproval) {
        return approvalMapper.insertImApproval(imApproval);
    }

    @Override
    public int updateImApproval(ImApproval imApproval) {
        return approvalMapper.updateImApproval(imApproval);
    }

    @Override
    public int deleteImApprovalByIds(Long[] ids) {
        return approvalMapper.deleteImApprovalByIds(ids);
    }

    @Override
    public List<ImApproval> selectApprovalListForAdmin(String status, Long userId, String title) {
        return approvalMapper.selectApprovalListForAdmin(status, userId, title);
    }

    @Override
    public List<ImApproval> getMyApprovals(Long userId) {
        return approvalMapper.getMyApprovals(userId);
    }

    @Override
    public List<?> getTemplates() {
        return approvalMapper.getTemplates();
    }

    @Override
    public List<?> getActiveTemplates() {
        return approvalMapper.getActiveTemplates();
    }

    @Override
    public void updateTemplateStatus(Long id, Boolean isActive) {
        approvalMapper.updateTemplateStatus(id, isActive);
    }

    @Override
    public void processApproval(Long id, String action, String comment, Long userId) {
        approvalMapper.processApproval(id, action, comment, userId);
    }

    @Override
    public void cancelApproval(Long id, Long userId) {
        approvalMapper.cancelApproval(id, userId);
    }

    @Override
    public Map<String, Object> getApprovalStatistics(String startTime, String endTime) {
        return approvalMapper.getApprovalStatistics(startTime, endTime);
    }
}
