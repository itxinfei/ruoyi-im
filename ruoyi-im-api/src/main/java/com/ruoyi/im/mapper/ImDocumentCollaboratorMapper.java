package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDocumentCollaborator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档协作者Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImDocumentCollaboratorMapper extends BaseMapper<ImDocumentCollaborator> {

    /**
     * 查询文档协作者列表
     *
     * @param documentId 文档ID
     * @return 协作者列表
     */
    List<ImDocumentCollaborator> selectByDocumentId(@Param("documentId") Long documentId);

    /**
     * 查询用户可协作的文档ID列表
     *
     * @param userId 用户ID
     * @return 文档ID列表
     */
    List<Long> selectCollaborativeDocumentIds(@Param("userId") Long userId);

    /**
     * 查询协作者
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     * @return 协作者信息
     */
    ImDocumentCollaborator selectByDocumentAndUser(@Param("documentId") Long documentId,
                                                   @Param("userId") Long userId);

    /**
     * 更新在线状态
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     * @param status     状态
     * @param activeTime 活跃时间
     * @return 更新行数
     */
    int updateOnlineStatus(@Param("documentId") Long documentId,
                          @Param("userId") Long userId,
                          @Param("status") String status,
                          @Param("activeTime") LocalDateTime activeTime);

    /**
     * 更新光标位置
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     * @param cursor     光标位置
     * @param selection  选择范围
     * @return 更新行数
     */
    int updateCursor(@Param("documentId") Long documentId,
                    @Param("userId") Long userId,
                    @Param("cursor") String cursor,
                    @Param("selection") String selection);

    /**
     * 查询在线编辑者
     *
     * @param documentId 文档ID
     * @param timeout    超时时间（分钟）
     * @return 在线编辑者列表
     */
    List<ImDocumentCollaborator> selectOnlineEditors(@Param("documentId") Long documentId,
                                                    @Param("timeout") Integer timeout);
}
