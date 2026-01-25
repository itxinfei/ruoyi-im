package com.ruoyi.im.service;

import com.ruoyi.im.dto.document.ImDocumentCollaboratorRequest;
import com.ruoyi.im.vo.document.ImDocumentCollaboratorVO;
import com.ruoyi.im.vo.document.ImDocumentOperationLogVO;

import java.util.List;

/**
 * 文档协作服务接口
 *
 * @author ruoyi
 */
public interface ImDocumentCollaborationService {

    // ==================== 协作者管理 ====================

    /**
     * 添加协作者
     *
     * @param request 添加请求
     * @param userId  操作用户ID
     */
    void addCollaborators(ImDocumentCollaboratorRequest request, Long userId);

    /**
     * 移除协作者
     *
     * @param documentId 文档ID
     * @param targetUserId 目标用户ID
     * @param userId   操作用户ID
     */
    void removeCollaborator(Long documentId, Long targetUserId, Long userId);

    /**
     * 更新协作者权限
     *
     * @param documentId 文档ID
     * @param targetUserId 目标用户ID
     * @param permission  权限
     * @param userId      操作用户ID
     */
    void updatePermission(Long documentId, Long targetUserId, String permission, Long userId);

    /**
     * 获取文档协作者列表
     *
     * @param documentId 文档ID
     * @return 协作者列表
     */
    List<ImDocumentCollaboratorVO> getCollaborators(Long documentId);

    /**
     * 获取用户可协作的文档列表
     *
     * @param userId 用户ID
     * @return 文档ID列表
     */
    List<Long> getCollaborativeDocuments(Long userId);

    // ==================== 在线状态管理 ====================

    /**
     * 用户加入文档编辑
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     */
    void joinDocument(Long documentId, Long userId);

    /**
     * 用户离开文档编辑
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     */
    void leaveDocument(Long documentId, Long userId);

    /**
     * 更新用户光标位置
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     * @param position   光标位置
     * @param selection  选择范围
     */
    void updateCursor(Long documentId, Long userId, String position, String selection);

    /**
     * 获取文档当前在线编辑者
     *
     * @param documentId 文档ID
     * @return 在线协作者列表
     */
    List<ImDocumentCollaboratorVO> getOnlineEditors(Long documentId);

    /**
     * 心跳更新（保持在线状态）
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     */
    void heartbeat(Long documentId, Long userId);

    // ==================== 操作日志 ====================

    /**
     * 记录操作日志
     *
     * @param documentId     文档ID
     * @param userId         用户ID
     * @param operationType  操作类型
     * @param position       操作位置
     * @param content        操作内容
     * @param beforeVersion  操作前版本号
     * @param afterVersion   操作后版本号
     */
    void logOperation(Long documentId, Long userId, String operationType,
                     Integer position, String content, Integer beforeVersion, Integer afterVersion);

    /**
     * 获取文档操作日志
     *
     * @param documentId 文档ID
     * @param limit      限制数量
     * @return 操作日志列表
     */
    List<ImDocumentOperationLogVO> getOperationLogs(Long documentId, Integer limit);

    /**
     * 清理过期操作日志
     *
     * @param documentId    文档ID
     * @param keepDays      保留天数
     */
    void cleanOldLogs(Long documentId, Integer keepDays);
}
