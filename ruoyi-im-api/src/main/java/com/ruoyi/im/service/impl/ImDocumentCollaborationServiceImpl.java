package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImDocument;
import com.ruoyi.im.domain.ImDocumentCollaborator;
import com.ruoyi.im.domain.ImDocumentOperationLog;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.document.ImDocumentCollaboratorRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImDocumentCollaboratorMapper;
import com.ruoyi.im.mapper.ImDocumentMapper;
import com.ruoyi.im.mapper.ImDocumentOperationLogMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImDocumentCollaborationService;
import com.ruoyi.im.vo.document.ImDocumentCollaboratorVO;
import com.ruoyi.im.vo.document.ImDocumentOperationLogVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档协作服务实现
 *
 * @author ruoyi
 */
@Service
public class ImDocumentCollaborationServiceImpl implements ImDocumentCollaborationService {

    private static final Logger logger = LoggerFactory.getLogger(ImDocumentCollaborationServiceImpl.class);

    /**
     * 在线超时时间（分钟）
     */
    private static final int ONLINE_TIMEOUT_MINUTES = 5;

    @Autowired
    private ImDocumentCollaboratorMapper documentCollaboratorMapper;

    @Autowired
    private ImDocumentOperationLogMapper documentOperationLogMapper;

    @Autowired
    private ImDocumentMapper documentMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCollaborators(ImDocumentCollaboratorRequest request, Long userId) {
        // 检查文档是否存在
        ImDocument document = documentMapper.selectById(request.getDocumentId());
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查操作权限
        if (!document.getOwnerId().equals(userId)) {
            ImDocumentCollaborator collaborator = documentCollaboratorMapper.selectByDocumentAndUser(
                    request.getDocumentId(), userId);
            if (collaborator == null || !"EDIT".equals(collaborator.getPermission())) {
                throw new BusinessException("无权限添加协作者");
            }
        }

        List<ImDocumentCollaborator> collaborators = new ArrayList<>();
        for (Long targetUserId : request.getUserIds()) {
            // 检查是否已是协作者
            ImDocumentCollaborator existCollaborator = documentCollaboratorMapper.selectByDocumentAndUser(
                    request.getDocumentId(), targetUserId);

            if (existCollaborator != null) {
                // 更新权限
                existCollaborator.setPermission(request.getPermission());
                existCollaborator.setUpdateTime(LocalDateTime.now());
                documentCollaboratorMapper.updateById(existCollaborator);
                continue;
            }

            // 获取用户信息
            ImUser user = userMapper.selectById(targetUserId);
            if (user == null) {
                continue;
            }

            ImDocumentCollaborator collaborator = new ImDocumentCollaborator();
            collaborator.setDocumentId(request.getDocumentId());
            collaborator.setUserId(targetUserId);
            collaborator.setUserName(user.getNickname());
            collaborator.setUserAvatar(user.getAvatar());
            collaborator.setPermission(request.getPermission());
            collaborator.setInviterId(userId);
            collaborator.setJoinTime(LocalDateTime.now());
            collaborator.setLastActiveTime(LocalDateTime.now());
            collaborator.setOnlineStatus("OFFLINE");
            collaborator.setCreateTime(LocalDateTime.now());
            collaborator.setUpdateTime(LocalDateTime.now());
            collaborators.add(collaborator);
        }

        if (!collaborators.isEmpty()) {
            for (ImDocumentCollaborator collaborator : collaborators) {
                documentCollaboratorMapper.insert(collaborator);
            }
        }

        logger.info("添加协作者成功: documentId={}, count={}", request.getDocumentId(), collaborators.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCollaborator(Long documentId, Long targetUserId, Long userId) {
        // 检查文档是否存在
        ImDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查操作权限
        boolean canRemove = document.getOwnerId().equals(userId);
        if (!canRemove) {
            ImDocumentCollaborator collaborator = documentCollaboratorMapper.selectByDocumentAndUser(
                    documentId, userId);
            if (collaborator != null && "EDIT".equals(collaborator.getPermission())) {
                canRemove = true;
            }
        }

        if (!canRemove) {
            throw new BusinessException("无权限移除协作者");
        }

        documentCollaboratorMapper.delete(
                new LambdaQueryWrapper<ImDocumentCollaborator>()
                        .eq(ImDocumentCollaborator::getDocumentId, documentId)
                        .eq(ImDocumentCollaborator::getUserId, targetUserId)
        );

        logger.info("移除协作者成功: documentId={}, targetUserId={}", documentId, targetUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(Long documentId, Long targetUserId, String permission, Long userId) {
        // 检查文档是否存在
        ImDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 只有文档所有者可以修改权限
        if (!document.getOwnerId().equals(userId)) {
            throw new BusinessException("只有文档所有者可以修改权限");
        }

        ImDocumentCollaborator collaborator = documentCollaboratorMapper.selectByDocumentAndUser(
                documentId, targetUserId);
        if (collaborator == null) {
            throw new BusinessException("协作者不存在");
        }

        collaborator.setPermission(permission);
        collaborator.setUpdateTime(LocalDateTime.now());
        documentCollaboratorMapper.updateById(collaborator);

        logger.info("更新协作者权限成功: documentId={}, targetUserId={}, permission={}",
                documentId, targetUserId, permission);
    }

    @Override
    public List<ImDocumentCollaboratorVO> getCollaborators(Long documentId) {
        List<ImDocumentCollaborator> collaborators = documentCollaboratorMapper.selectByDocumentId(documentId);

        return collaborators.stream().map(collaborator -> {
            ImDocumentCollaboratorVO vo = new ImDocumentCollaboratorVO();
            BeanUtils.copyProperties(collaborator, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Long> getCollaborativeDocuments(Long userId) {
        return documentCollaboratorMapper.selectCollaborativeDocumentIds(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinDocument(Long documentId, Long userId) {
        ImDocumentCollaborator collaborator = documentCollaboratorMapper.selectByDocumentAndUser(
                documentId, userId);

        if (collaborator != null) {
            documentCollaboratorMapper.updateOnlineStatus(documentId, userId,
                    "ONLINE", LocalDateTime.now());
        } else {
            // 检查是否有访问权限
            ImDocument document = documentMapper.selectById(documentId);
            if (document == null || (!document.getOwnerId().equals(userId))) {
                throw new BusinessException("无权限访问此文档");
            }

            // 创建临时协作者记录
            ImUser user = userMapper.selectById(userId);
            collaborator = new ImDocumentCollaborator();
            collaborator.setDocumentId(documentId);
            collaborator.setUserId(userId);
            collaborator.setUserName(user != null ? user.getNickname() : "");
            collaborator.setUserAvatar(user != null ? user.getAvatar() : "");
            collaborator.setPermission(document.getOwnerId().equals(userId) ? "EDIT" : "VIEW");
            collaborator.setOnlineStatus("ONLINE");
            collaborator.setJoinTime(LocalDateTime.now());
            collaborator.setLastActiveTime(LocalDateTime.now());
            documentCollaboratorMapper.insert(collaborator);
        }

        logger.info("用户加入文档编辑: documentId={}, userId={}", documentId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveDocument(Long documentId, Long userId) {
        documentCollaboratorMapper.updateOnlineStatus(documentId, userId,
                "OFFLINE", LocalDateTime.now());

        logger.info("用户离开文档编辑: documentId={}, userId={}", documentId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCursor(Long documentId, Long userId, String position, String selection) {
        documentCollaboratorMapper.updateCursor(documentId, userId, position, selection);
    }

    @Override
    public List<ImDocumentCollaboratorVO> getOnlineEditors(Long documentId) {
        List<ImDocumentCollaborator> collaborators = documentCollaboratorMapper.selectOnlineEditors(
                documentId, ONLINE_TIMEOUT_MINUTES);

        return collaborators.stream().map(collaborator -> {
            ImDocumentCollaboratorVO vo = new ImDocumentCollaboratorVO();
            BeanUtils.copyProperties(collaborator, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void heartbeat(Long documentId, Long userId) {
        documentCollaboratorMapper.updateOnlineStatus(documentId, userId,
                "ONLINE", LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logOperation(Long documentId, Long userId, String operationType,
                            Integer position, String content, Integer beforeVersion, Integer afterVersion) {
        ImUser user = userMapper.selectById(userId);
        ImDocumentOperationLog log = new ImDocumentOperationLog();
        log.setDocumentId(documentId);
        log.setUserId(userId);
        log.setUserName(user != null ? user.getNickname() : "");
        log.setOperationType(operationType);
        log.setPosition(position);
        log.setContent(content != null && content.length() > 500 ? content.substring(0, 500) : content);
        log.setContentLength(content != null ? content.length() : 0);
        log.setBeforeVersion(beforeVersion);
        log.setAfterVersion(afterVersion);
        log.setOperationTime(LocalDateTime.now());

        // 生成变更摘要
        String summary = generateChangeSummary(operationType, content);
        log.setChangeSummary(summary);

        documentOperationLogMapper.insert(log);
    }

    @Override
    public List<ImDocumentOperationLogVO> getOperationLogs(Long documentId, Integer limit) {
        int queryLimit = limit != null && limit > 0 ? limit : 100;
        List<ImDocumentOperationLog> logs = documentOperationLogMapper.selectByDocumentId(documentId, queryLimit);

        return logs.stream().map(log -> {
            ImDocumentOperationLogVO vo = new ImDocumentOperationLogVO();
            BeanUtils.copyProperties(log, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanOldLogs(Long documentId, Integer keepDays) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(keepDays);
        int deleted = documentOperationLogMapper.deleteOldLogs(beforeTime);
        logger.info("清理过期操作日志: documentId={}, deleted={}", documentId, deleted);
    }

    /**
     * 生成变更摘要
     */
    private String generateChangeSummary(String operationType, String content) {
        switch (operationType) {
            case "INSERT":
                if (content != null && content.length() > 50) {
                    return "插入: " + content.substring(0, 50) + "...";
                }
                return "插入: " + (content != null ? content : "");
            case "DELETE":
                if (content != null && content.length() > 50) {
                    return "删除: " + content.substring(0, 50) + "...";
                }
                return "删除: " + (content != null ? content : "");
            case "UPDATE":
                return "修改内容";
            case "FORMAT":
                return "格式调整";
            default:
                return "其他操作";
        }
    }
}
