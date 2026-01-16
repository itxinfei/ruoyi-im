package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImGroupLog;
import com.ruoyi.web.mapper.ImGroupLogMapper;
import com.ruoyi.web.service.ImGroupLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组管理日志Service实现
 *
 * @author ruoyi
 */
@Service
public class ImGroupLogServiceImpl implements ImGroupLogService {

    @Autowired
    private ImGroupLogMapper imGroupLogMapper;

    @Override
    public ImGroupLog selectImGroupLogById(Long id) {
        return imGroupLogMapper.selectImGroupLogById(id);
    }

    @Override
    public List<ImGroupLog> selectImGroupLogList(ImGroupLog imGroupLog) {
        return imGroupLogMapper.selectImGroupLogList(imGroupLog);
    }

    @Override
    public int insertImGroupLog(ImGroupLog imGroupLog) {
        if (imGroupLog.getCreateTime() == null) {
            imGroupLog.setCreateTime(LocalDateTime.now());
        }
        return imGroupLogMapper.insertImGroupLog(imGroupLog);
    }

    @Override
    public int updateImGroupLog(ImGroupLog imGroupLog) {
        return imGroupLogMapper.updateImGroupLog(imGroupLog);
    }

    @Override
    public int deleteImGroupLogByIds(Long[] ids) {
        return imGroupLogMapper.deleteImGroupLogByIds(ids);
    }

    @Override
    public int deleteImGroupLogById(Long id) {
        return imGroupLogMapper.deleteImGroupLogById(id);
    }

    @Override
    public List<ImGroupLog> selectLogsByGroupId(Long groupId) {
        return imGroupLogMapper.selectLogsByGroupId(groupId);
    }

    @Override
    public List<ImGroupLog> selectLogsByOperatorId(Long operatorId) {
        return imGroupLogMapper.selectLogsByOperatorId(operatorId);
    }

    @Override
    public int countLogsByGroupId(Long groupId) {
        return imGroupLogMapper.countLogsByGroupId(groupId);
    }

    @Override
    public int logGroupOperation(Long groupId, Long operatorId, String operatorName, String operationType, String operationDesc) {
        ImGroupLog log = new ImGroupLog();
        log.setGroupId(groupId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setCreateTime(LocalDateTime.now());
        return imGroupLogMapper.insertImGroupLog(log);
    }

    @Override
    public int logMemberOperation(Long groupId, Long operatorId, String operatorName, String operationType, String operationDesc, Long targetUserId, String targetUserName) {
        ImGroupLog log = new ImGroupLog();
        log.setGroupId(groupId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setTargetUserId(targetUserId);
        log.setTargetUserName(targetUserName);
        log.setCreateTime(LocalDateTime.now());
        return imGroupLogMapper.insertImGroupLog(log);
    }
}
