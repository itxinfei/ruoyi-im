package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImWorkReport;
import com.ruoyi.web.mapper.ImWorkReportMapper;
import com.ruoyi.web.service.ImWorkReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 工作报告Service实现
 *
 * @author ruoyi
 */
@Service
public class ImWorkReportServiceImpl implements ImWorkReportService {

    @Autowired
    private ImWorkReportMapper imWorkReportMapper;

    @Override
    public ImWorkReport selectImWorkReportById(Long id) {
        return imWorkReportMapper.selectImWorkReportById(id);
    }

    @Override
    public List<ImWorkReport> selectImWorkReportList(ImWorkReport imWorkReport) {
        return imWorkReportMapper.selectImWorkReportList(imWorkReport);
    }

    @Override
    public List<ImWorkReport> selectReportsByUserId(Long userId) {
        return imWorkReportMapper.selectReportsByUserId(userId);
    }

    @Override
    public int insertImWorkReport(ImWorkReport imWorkReport) {
        return imWorkReportMapper.insertImWorkReport(imWorkReport);
    }

    @Override
    public int updateImWorkReport(ImWorkReport imWorkReport) {
        return imWorkReportMapper.updateImWorkReport(imWorkReport);
    }

    @Override
    public int deleteImWorkReportByIds(Long[] ids) {
        return imWorkReportMapper.deleteImWorkReportByIds(ids);
    }

    @Override
    public int deleteImWorkReportById(Long id) {
        return imWorkReportMapper.deleteImWorkReportById(id);
    }

    @Override
    public int updateReportStatus(Long id, String status) {
        return imWorkReportMapper.updateReportStatus(id, status);
    }

    @Override
    public int approveReport(Long id, Long approverId, String status, String remark) {
        return imWorkReportMapper.approveReport(id, approverId, status, remark);
    }

    @Override
    public Map<String, Object> getReportStatistics() {
        return imWorkReportMapper.getReportStatistics();
    }

    @Override
    public List<Map<String, Object>> countByReportType() {
        return imWorkReportMapper.countByReportType();
    }

    @Override
    public List<ImWorkReport> selectReportsByDateRange(String startDate, String endDate) {
        return imWorkReportMapper.selectReportsByDateRange(startDate, endDate);
    }

    @Override
    public List<ImWorkReport> selectPendingApprovalReports() {
        return imWorkReportMapper.selectPendingApprovalReports();
    }
}
