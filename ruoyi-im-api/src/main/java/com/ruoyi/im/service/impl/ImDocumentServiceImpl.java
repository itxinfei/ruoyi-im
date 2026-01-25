package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.domain.ImDocument;
import com.ruoyi.im.domain.ImDocumentComment;
import com.ruoyi.im.domain.ImDocumentShare;
import com.ruoyi.im.domain.ImDocumentVersion;
import com.ruoyi.im.dto.document.ImDocumentCommentRequest;
import com.ruoyi.im.dto.document.ImDocumentCreateRequest;
import com.ruoyi.im.dto.document.ImDocumentShareRequest;
import com.ruoyi.im.dto.document.ImDocumentUpdateRequest;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImDocumentCommentMapper;
import com.ruoyi.im.mapper.ImDocumentMapper;
import com.ruoyi.im.mapper.ImDocumentShareMapper;
import com.ruoyi.im.mapper.ImDocumentVersionMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImDocumentService;
import com.ruoyi.im.vo.document.ImDocumentCommentVO;
import com.ruoyi.im.vo.document.ImDocumentVersionVO;
import com.ruoyi.im.vo.document.ImDocumentVO;
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
 * 文档服务实现
 *
 * @author ruoyi
 */
@Service
public class ImDocumentServiceImpl implements ImDocumentService {

    private static final Logger log = LoggerFactory.getLogger(ImDocumentServiceImpl.class);

    private final ImDocumentMapper documentMapper;
    private final ImDocumentShareMapper documentShareMapper;
    private final ImDocumentCommentMapper documentCommentMapper;
    private final ImDocumentVersionMapper documentVersionMapper;
    private final ImUserMapper imUserMapper;

    /**
     * 构造器注入依赖
     */
    public ImDocumentServiceImpl(ImDocumentMapper documentMapper,
                                  ImDocumentShareMapper documentShareMapper,
                                  ImDocumentCommentMapper documentCommentMapper,
                                  ImDocumentVersionMapper documentVersionMapper,
                                  ImUserMapper imUserMapper) {
        this.documentMapper = documentMapper;
        this.documentShareMapper = documentShareMapper;
        this.documentCommentMapper = documentCommentMapper;
        this.documentVersionMapper = documentVersionMapper;
        this.imUserMapper = imUserMapper;
    }

    @Override
    @Transactional
    public Long createDocument(ImDocumentCreateRequest request, Long userId) {
        ImDocument document = new ImDocument();
        document.setTitle(request.getTitle());
        document.setDocType(request.getDocType());
        document.setContent(request.getContent() != null ? request.getContent() : "");
        document.setStatus("draft");
        document.setOwnerId(userId);
        document.setFolderId(request.getFolderId());
        document.setParentId(request.getParentId());
        document.setIsStarred(false);
        document.setIsDeleted(false);
        document.setLastModifiedBy(userId);
        document.setLastModifiedTime(LocalDateTime.now());
        document.setCreateTime(LocalDateTime.now());
        document.setVersion(1);

        documentMapper.insert(document);

        // 创建初始版本
        createDocumentVersion(document.getId(), document.getContent(), document.getTitle(),
            userId, "创建文档", 0L);

        log.info("创建文档成功: documentId={}, title={}", document.getId(), document.getTitle());
        return document.getId();
    }

    @Override
    @Transactional
    public Boolean updateDocument(ImDocumentUpdateRequest request, Long userId) {
        ImDocument document = documentMapper.selectById(request.getId());
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查权限（只有所有者或被授权用户可以编辑）
        if (!canEditDocument(document, userId)) {
            throw new BusinessException("无权限编辑此文档");
        }

        // 更新文档内容
        if (request.getContent() != null) {
            document.setContent(request.getContent());
        }
        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }

        document.setLastModifiedBy(userId);
        document.setLastModifiedTime(LocalDateTime.now());

        // 如果不是自动保存，创建新版本
        Boolean autoSave = request.getAutoSave() != null ? request.getAutoSave() : false;
        if (!autoSave) {
            document.setVersion(document.getVersion() + 1);
            createDocumentVersion(document.getId(), document.getContent(), document.getTitle(),
                userId, request.getChangeDescription(), calculateContentSize(document.getContent()));
        }

        documentMapper.updateById(document);

        log.info("更新文档成功: documentId={}, autoSave={}", document.getId(), autoSave);
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteDocument(Long documentId, Long userId) {
        ImDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查权限
        if (!document.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限删除此文档");
        }

        // 移到回收站
        document.setIsDeleted(true);
        document.setDeletedTime(LocalDateTime.now());
        document.setAutoDeleteDays(30);
        documentMapper.updateById(document);

        log.info("文档移到回收站: documentId={}", documentId);
        return true;
    }

    @Override
    @Transactional
    public Boolean permanentlyDeleteDocument(Long documentId, Long userId) {
        ImDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查权限
        if (!document.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限删除此文档");
        }

        // 删除相关数据
        documentShareMapper.delete(new LambdaQueryWrapper<ImDocumentShare>()
            .eq(ImDocumentShare::getDocumentId, documentId));
        documentCommentMapper.delete(new LambdaQueryWrapper<ImDocumentComment>()
            .eq(ImDocumentComment::getDocumentId, documentId));
        documentVersionMapper.delete(new LambdaQueryWrapper<ImDocumentVersion>()
            .eq(ImDocumentVersion::getDocumentId, documentId));

        // 删除文档
        documentMapper.deleteById(documentId);

        log.info("永久删除文档: documentId={}", documentId);
        return true;
    }

    @Override
    @Transactional
    public Boolean restoreDocument(Long documentId, Long userId) {
        ImDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查权限
        if (!document.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限恢复此文档");
        }

        // 恢复文档
        document.setIsDeleted(false);
        document.setDeletedTime(null);
        document.setAutoDeleteDays(null);
        documentMapper.updateById(document);

        log.info("恢复文档: documentId={}", documentId);
        return true;
    }

    @Override
    public ImDocumentVO getDocument(Long documentId, Long userId) {
        ImDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查权限
        if (!canViewDocument(document, userId)) {
            throw new BusinessException("无权限查看此文档");
        }

        ImDocumentVO vo = new ImDocumentVO();
        BeanUtils.copyProperties(document, vo);

        // 设置预览
        if (document.getContent() != null && document.getContent().length() > 100) {
            vo.setPreview(document.getContent().substring(0, 100) + "...");
        } else {
            vo.setPreview(document.getContent());
        }

        // 获取用户权限
        vo.setUserPermission(getUserPermission(document, userId));

        return vo;
    }

    @Override
    public List<ImDocumentVO> getUserDocuments(Long userId, String type) {
        List<ImDocument> documents = new ArrayList<>();

        switch (type) {
            case "all":
                // 我的文档
                documents.addAll(documentMapper.selectList(
                    new LambdaQueryWrapper<ImDocument>()
                        .eq(ImDocument::getOwnerId, userId)
                        .eq(ImDocument::getIsDeleted, false)
                ));
                // 共享文档
                documents.addAll(documentMapper.selectSharedDocuments(userId));
                break;
            case "my":
                documents = documentMapper.selectList(
                    new LambdaQueryWrapper<ImDocument>()
                        .eq(ImDocument::getOwnerId, userId)
                        .eq(ImDocument::getIsDeleted, false)
                );
                break;
            case "shared":
                documents = documentMapper.selectSharedDocuments(userId);
                break;
            case "starred":
                documents = documentMapper.selectStarredDocuments(userId);
                break;
            case "trash":
                documents = documentMapper.selectDeletedDocuments(userId);
                break;
            default:
                documents = documentMapper.selectList(
                    new LambdaQueryWrapper<ImDocument>()
                        .eq(ImDocument::getOwnerId, userId)
                        .eq(ImDocument::getIsDeleted, false)
                );
        }

        return documents.stream()
            .map(doc -> {
                ImDocumentVO vo = new ImDocumentVO();
                BeanUtils.copyProperties(doc, vo);
                return vo;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<ImDocumentVO> searchDocuments(Long userId, String keyword) {
        List<ImDocument> documents = documentMapper.searchDocuments(userId, keyword);
        return documents.stream()
            .map(doc -> {
                ImDocumentVO vo = new ImDocumentVO();
                BeanUtils.copyProperties(doc, vo);
                return vo;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean toggleStar(Long documentId, Long userId, Boolean starred) {
        ImDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 只能收藏自己的文档或共享文档
        if (!canViewDocument(document, userId)) {
            throw new BusinessException("无权限收藏此文档");
        }

        document.setIsStarred(starred);
        documentMapper.updateById(document);

        log.info("文档收藏状态更新: documentId={}, starred={}", documentId, starred);
        return true;
    }

    @Override
    @Transactional
    public Boolean shareDocument(ImDocumentShareRequest request, Long userId) {
        ImDocument document = documentMapper.selectById(request.getDocumentId());
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查权限（只有所有者可以分享）
        if (!document.getOwnerId().equals(userId)) {
            throw new BusinessException("只有文档所有者可以分享文档");
        }

        LocalDateTime expireTime = null;
        if (request.getExpireDays() != null) {
            expireTime = LocalDateTime.now().plusDays(request.getExpireDays());
        }

        // 创建分享记录
        for (Long targetUserId : request.getUserIds()) {
            // 检查是否已存在
            Long count = documentShareMapper.selectCount(
                new LambdaQueryWrapper<ImDocumentShare>()
                    .eq(ImDocumentShare::getDocumentId, request.getDocumentId())
                    .eq(ImDocumentShare::getSharedToUserId, targetUserId)
            );

            if (count == 0) {
                ImDocumentShare share = new ImDocumentShare();
                share.setDocumentId(request.getDocumentId());
                share.setSharedToUserId(targetUserId);
                share.setSharedByUserId(userId);
                share.setPermission(request.getPermission());
                share.setIsExpired(false);
                share.setExpireTime(expireTime);
                share.setCreateTime(LocalDateTime.now());
                documentShareMapper.insert(share);
            }
        }

        log.info("文档分享成功: documentId={}, userCount={}", request.getDocumentId(), request.getUserIds().size());
        return true;
    }

    @Override
    @Transactional
    public Boolean unshareDocument(Long documentId, Long targetUserId, Long userId) {
        // 检查权限
        Long count = documentShareMapper.selectCount(
            new LambdaQueryWrapper<ImDocumentShare>()
                .eq(ImDocumentShare::getDocumentId, documentId)
                .eq(ImDocumentShare::getSharedByUserId, userId)
        );

        if (count == 0) {
            throw new BusinessException("无权限取消分享");
        }

        documentShareMapper.delete(new LambdaQueryWrapper<ImDocumentShare>()
            .eq(ImDocumentShare::getDocumentId, documentId)
            .eq(ImDocumentShare::getSharedToUserId, targetUserId));

        log.info("取消文档分享: documentId={}, targetUserId={}", documentId, targetUserId);
        return true;
    }

    @Override
    @Transactional
    public Long addComment(ImDocumentCommentRequest request, Long userId) {
        ImDocumentComment comment = new ImDocumentComment();
        comment.setDocumentId(request.getDocumentId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setSelectedText(request.getSelectedText());
        comment.setSelectionOffset(request.getSelectionOffset());
        comment.setIsResolved(false);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        documentCommentMapper.insert(comment);

        log.info("添加文档评论: commentId={}, documentId={}", comment.getId(), request.getDocumentId());
        return comment.getId();
    }

    @Override
    @Transactional
    public Boolean deleteComment(Long commentId, Long userId) {
        ImDocumentComment comment = documentCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除此评论");
        }

        documentCommentMapper.deleteById(commentId);

        log.info("删除文档评论: commentId={}", commentId);
        return true;
    }

    @Override
    public List<ImDocumentCommentVO> getDocumentComments(Long documentId, Long userId) {
        List<ImDocumentComment> comments = documentCommentMapper.selectList(
            new LambdaQueryWrapper<ImDocumentComment>()
                .eq(ImDocumentComment::getDocumentId, documentId)
                .isNull(ImDocumentComment::getParentId)
                .orderByDesc(ImDocumentComment::getCreateTime)
        );

        // 批量获取评论用户ID
        java.util.Set<Long> userIds = comments.stream()
            .map(ImDocumentComment::getUserId)
            .collect(Collectors.toSet());

        // 获取回复的评论
        List<ImDocumentComment> allReplies = documentCommentMapper.selectList(
            new LambdaQueryWrapper<ImDocumentComment>()
                .in(ImDocumentComment::getParentId,
                    comments.stream().map(ImDocumentComment::getId).collect(Collectors.toList()))
        );
        allReplies.forEach(reply -> userIds.add(reply.getUserId()));

        // 批量查询用户信息
        java.util.Map<Long, ImUser> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<ImUser> users = imUserMapper.selectImUserListByIds(new ArrayList<>(userIds));
            users.forEach(user -> userMap.put(user.getId(), user));
        }

        java.util.Map<Long, List<ImDocumentComment>> repliesMap = allReplies.stream()
            .collect(Collectors.groupingBy(ImDocumentComment::getParentId));

        return comments.stream()
            .map(comment -> {
                ImDocumentCommentVO vo = new ImDocumentCommentVO();
                BeanUtils.copyProperties(comment, vo);
                ImUser user = userMap.get(comment.getUserId());
                vo.setUserName(user != null ? user.getNickname() : "用户" + comment.getUserId());
                vo.setUserAvatar(user != null ? user.getAvatar() : null);
                vo.setCanDelete(comment.getUserId().equals(userId));

                // 转换回复为VO
                List<ImDocumentComment> replies = repliesMap.getOrDefault(comment.getId(), new ArrayList<>());
                List<ImDocumentCommentVO.ReplyVO> replyVOs = replies.stream()
                    .map(reply -> {
                        ImDocumentCommentVO.ReplyVO replyVO = new ImDocumentCommentVO.ReplyVO();
                        replyVO.setId(reply.getId());
                        ImUser replyUser = userMap.get(reply.getUserId());
                        replyVO.setUserName(replyUser != null ? replyUser.getNickname() : "用户" + reply.getUserId());
                        replyVO.setContent(reply.getContent());
                        replyVO.setCreateTime(reply.getCreateTime());
                        return replyVO;
                    })
                    .collect(Collectors.toList());
                vo.setReplies(replyVOs);

                return vo;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<ImDocumentVersionVO> getDocumentVersions(Long documentId, Long userId) {
        List<ImDocumentVersion> versions = documentVersionMapper.selectVersionsByDocumentId(documentId);

        return versions.stream()
            .map(version -> {
                ImDocumentVersionVO vo = new ImDocumentVersionVO();
                BeanUtils.copyProperties(version, vo);
                vo.setCanRestore(true);
                return vo;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean restoreVersion(Long documentId, Long versionId, Long userId) {
        ImDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查权限
        if (!canEditDocument(document, userId)) {
            throw new BusinessException("无权限恢复此文档");
        }

        ImDocumentVersion version = documentVersionMapper.selectById(versionId);
        if (version == null || !version.getDocumentId().equals(documentId)) {
            throw new BusinessException("版本不存在");
        }

        // 恢复内容
        document.setContent(version.getContent());
        document.setVersion(document.getVersion() + 1);
        document.setLastModifiedBy(userId);
        document.setLastModifiedTime(LocalDateTime.now());
        documentMapper.updateById(document);

        // 创建新版本记录恢复操作
        createDocumentVersion(documentId, version.getContent(), version.getTitleSnapshot(),
            userId, "恢复到版本" + version.getVersion(), version.getFileSize());

        log.info("恢复文档版本: documentId={}, versionId={}", documentId, versionId);
        return true;
    }

    /**
     * 创建文档版本记录
     */
    private void createDocumentVersion(Long documentId, String content, String titleSnapshot,
                                      Long userId, String description, Long fileSize) {
        Integer latestVersion = documentVersionMapper.selectLatestVersion(documentId);
        int newVersion = (latestVersion != null ? latestVersion : 0) + 1;

        // 获取用户信息
        ImUser user = imUserMapper.selectImUserById(userId);
        String userName = user != null ? user.getNickname() : "用户" + userId;

        ImDocumentVersion version = new ImDocumentVersion();
        version.setDocumentId(documentId);
        version.setVersion(newVersion);
        version.setContent(content);
        version.setTitleSnapshot(titleSnapshot);
        version.setModifiedBy(userId);
        version.setModifiedByName(userName);
        version.setChangeDescription(description);
        version.setFileSize(fileSize != null ? fileSize : calculateContentSize(content));
        version.setCreateTime(LocalDateTime.now());

        documentVersionMapper.insert(version);
    }

    /**
     * 检查是否可以查看文档
     */
    private boolean canViewDocument(ImDocument document, Long userId) {
        // 所有者可以查看
        if (document.getOwnerId().equals(userId)) {
            return true;
        }

        // 检查是否被分享
        Long count = documentShareMapper.selectCount(
            new LambdaQueryWrapper<ImDocumentShare>()
                .eq(ImDocumentShare::getDocumentId, document.getId())
                .eq(ImDocumentShare::getSharedToUserId, userId)
                .eq(ImDocumentShare::getIsExpired, false)
        );

        return count > 0;
    }

    /**
     * 检查是否可以编辑文档
     */
    private boolean canEditDocument(ImDocument document, Long userId) {
        // 所有者可以编辑
        if (document.getOwnerId().equals(userId)) {
            return true;
        }

        // 检查是否有编辑权限
        Long count = documentShareMapper.selectCount(
            new LambdaQueryWrapper<ImDocumentShare>()
                .eq(ImDocumentShare::getDocumentId, document.getId())
                .eq(ImDocumentShare::getSharedToUserId, userId)
                .eq(ImDocumentShare::getPermission, "edit")
                .eq(ImDocumentShare::getIsExpired, false)
        );

        return count > 0;
    }

    /**
     * 获取用户对文档的权限
     */
    private String getUserPermission(ImDocument document, Long userId) {
        if (document.getOwnerId().equals(userId)) {
            return "admin";
        }

        ImDocumentShare share = documentShareMapper.selectOne(
            new LambdaQueryWrapper<ImDocumentShare>()
                .eq(ImDocumentShare::getDocumentId, document.getId())
                .eq(ImDocumentShare::getSharedToUserId, userId)
                .eq(ImDocumentShare::getIsExpired, false)
        );

        return share != null ? share.getPermission() : "view";
    }

    /**
     * 计算内容大小
     */
    private Long calculateContentSize(String content) {
        if (content == null) {
            return 0L;
        }
        return (long) content.getBytes().length;
    }
}
