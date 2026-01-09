package com.ruoyi.im.service;

import com.ruoyi.im.dto.document.ImDocumentCommentRequest;
import com.ruoyi.im.dto.document.ImDocumentCreateRequest;
import com.ruoyi.im.dto.document.ImDocumentShareRequest;
import com.ruoyi.im.dto.document.ImDocumentUpdateRequest;
import com.ruoyi.im.vo.document.ImDocumentCommentVO;
import com.ruoyi.im.vo.document.ImDocumentVO;

import java.util.List;

/**
 * 文档服务接口
 *
 * @author ruoyi
 */
public interface ImDocumentService {

    /**
     * 创建文档
     *
     * @param request 创建请求
     * @param userId  用户ID
     * @return 文档ID
     */
    Long createDocument(ImDocumentCreateRequest request, Long userId);

    /**
     * 更新文档
     *
     * @param request 更新请求
     * @param userId  用户ID
     * @return 是否成功
     */
    Boolean updateDocument(ImDocumentUpdateRequest request, Long userId);

    /**
     * 删除文档（移到回收站）
     *
     * @param documentId 文档ID
     * @param userId    用户ID
     * @return 是否成功
     */
    Boolean deleteDocument(Long documentId, Long userId);

    /**
     * 永久删除文档
     *
     * @param documentId 文档ID
     * @param userId    用户ID
     * @return 是否成功
     */
    Boolean permanentlyDeleteDocument(Long documentId, Long userId);

    /**
     * 恢复文档
     *
     * @param documentId 文档ID
     * @param userId    用户ID
     * @return 是否成功
     */
    Boolean restoreDocument(Long documentId, Long userId);

    /**
     * 获取文档详情
     *
     * @param documentId 文档ID
     * @param userId    用户ID
     * @return 文档VO
     */
    ImDocumentVO getDocument(Long documentId, Long userId);

    /**
     * 获取用户的文档列表
     *
     * @param userId 用户ID
     * @param type   类型（all=全部, my=我的, shared=共享, starred=收藏, trash=回收站）
     * @return 文档列表
     */
    List<ImDocumentVO> getUserDocuments(Long userId, String type);

    /**
     * 搜索文档
     *
     * @param userId  用户ID
     * @param keyword 关键词
     * @return 文档列表
     */
    List<ImDocumentVO> searchDocuments(Long userId, String keyword);

    /**
     * 收藏/取消收藏文档
     *
     * @param documentId 文档ID
     * @param userId    用户ID
     * @param starred   是否收藏
     * @return 是否成功
     */
    Boolean toggleStar(Long documentId, Long userId, Boolean starred);

    /**
     * 分享文档
     *
     * @param request 分享请求
     * @param userId  用户ID
     * @return 是否成功
     */
    Boolean shareDocument(ImDocumentShareRequest request, Long userId);

    /**
     * 取消分享
     *
     * @param documentId 文档ID
     * @param targetUserId 目标用户ID
     * @param userId    当前用户ID
     * @return 是否成功
     */
    Boolean unshareDocument(Long documentId, Long targetUserId, Long userId);

    /**
     * 添加评论
     *
     * @param request 评论请求
     * @param userId  用户ID
     * @return 评论ID
     */
    Long addComment(ImDocumentCommentRequest request, Long userId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     * @return 是否成功
     */
    Boolean deleteComment(Long commentId, Long userId);

    /**
     * 获取文档评论列表
     *
     * @param documentId 文档ID
     * @param userId    用户ID
     * @return 评论列表
     */
    List<ImDocumentCommentVO> getDocumentComments(Long documentId, Long userId);

    /**
     * 获取文档版本历史
     *
     * @param documentId 文档ID
     * @param userId    用户ID
     * @return 版本列表
     */
    List<com.ruoyi.im.vo.document.ImDocumentVersionVO> getDocumentVersions(Long documentId, Long userId);

    /**
     * 恢复到指定版本
     *
     * @param documentId 文档ID
     * @param versionId  版本ID
     * @param userId     用户ID
     * @return 是否成功
     */
    Boolean restoreVersion(Long documentId, Long versionId, Long userId);
}
