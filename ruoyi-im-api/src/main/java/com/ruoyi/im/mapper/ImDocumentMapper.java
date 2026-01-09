package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImDocumentMapper extends BaseMapper<ImDocument> {

    /**
     * 查询用户的文档列表
     *
     * @param userId 用户ID
     * @param includeShared 是否包含共享文档
     * @return 文档列表
     */
    List<ImDocument> selectUserDocuments(@Param("userId") Long userId,
                                        @Param("includeShared") Boolean includeShared);

    /**
     * 查询共享给用户的文档列表
     *
     * @param userId 用户ID
     * @return 文档列表
     */
    List<ImDocument> selectSharedDocuments(@Param("userId") Long userId);

    /**
     * 查询用户的收藏文档
     *
     * @param userId 用户ID
     * @return 文档列表
     */
    List<ImDocument> selectStarredDocuments(@Param("userId") Long userId);

    /**
     * 查询回收站文档
     *
     * @param userId 用户ID
     * @return 文档列表
     */
    List<ImDocument> selectDeletedDocuments(@Param("userId") Long userId);

    /**
     * 搜索文档
     *
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 文档列表
     */
    List<ImDocument> searchDocuments(@Param("userId") Long userId,
                                     @Param("keyword") String keyword);
}
