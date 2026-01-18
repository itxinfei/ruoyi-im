package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImAttendance;
import com.ruoyi.web.mapper.ImAttendanceMapper;
import com.ruoyi.web.service.ImAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 考勤记录Service实现
 *
 * @author ruoyi
 */
@Service
public class ImAttendanceServiceImpl implements ImAttendanceService {

    @Autowired
    private ImAttendanceMapper imAttendanceMapper;

    @Override
    public ImAttendance selectImAttendanceById(Long id) {
        return imAttendanceMapper.selectImAttendanceById(id);
    }

    @Override
    public List<ImAttendance> selectImAttendanceList(ImAttendance imAttendance) {
        return imAttendanceMapper.selectImAttendanceList(imAttendance);
    }

    @Override
    public List<ImAttendance> selectAttendanceByUserId(Long userId) {
        return imAttendanceMapper.selectAttendanceByUserId(userId);
    }

    @Override
    public List<ImAttendance> selectAttendanceByDateRange(Long userId, String startDate, String endDate) {
        return imAttendanceMapper.selectAttendanceByDateRange(userId, startDate, endDate);
    }

    @Override
    public int insertImAttendance(ImAttendance imAttendance) {
        return imAttendanceMapper.insertImAttendance(imAttendance);
    }

    @Override
    public int updateImAttendance(ImAttendance imAttendance) {
        return imAttendanceMapper.updateImAttendance(imAttendance);
    }

    @Override
    public int deleteImAttendanceByIds(Long[] ids) {
        return imAttendanceMapper.deleteImAttendanceByIds(ids);
    }

    @Override
    public int deleteImAttendanceById(Long id) {
        return imAttendanceMapper.deleteImAttendanceById(id);
    }

    @Override
    public int approveAttendance(Long id, String approveStatus, Long approverId, String approveComment) {
        return imAttendanceMapper.updateApproveStatus(id, approveStatus, approverId, approveComment);
    }

    @Override
    public int batchApproveAttendance(Long[] ids, String approveStatus, Long approverId, String approveComment) {
        int result = 0;
        for (Long id : ids) {
            result += imAttendanceMapper.updateApproveStatus(id, approveStatus, approverId, approveComment);
        }
        return result;
    }

    @Override
    public Map<String, Object> getAttendanceStatistics() {
        return imAttendanceMapper.getAttendanceStatistics();
    }

    @Override
    public List<ImAttendance> selectTodayAttendance(Long userId) {
        return imAttendanceMapper.selectTodayAttendance(userId);
    }

    @Override
    public List<ImAttendance> selectPendingApprovalList() {
        return imAttendanceMapper.selectPendingApprovalList();
    }
}
