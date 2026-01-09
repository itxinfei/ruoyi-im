package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDocumentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档版本Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImDocumentVersionMapper extends BaseMapper<ImDocumentVersion> {

    /**
     * 查询文档的版本历史
     *
     * @param documentId 文档ID
     * @return 版本列表
     */
    List<ImDocumentVersion> selectVersionsByDocumentId(@Param("documentId") Long documentId);

    /**
     * 查询文档的最新版本号
     *
     * @param documentId 文档ID
     * @return 版本号
     */
    Integer selectLatestVersion(@Param("documentId") Long documentId);
}
