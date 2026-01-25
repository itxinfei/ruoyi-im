package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDocumentOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档操作日志Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImDocumentOperationLogMapper extends BaseMapper<ImDocumentOperationLog> {

    /**
     * 查询文档操作日志
     *
     * @param documentId 文档ID
     * @param limit      限制数量
     * @return 操作日志列表
     */
    List<ImDocumentOperationLog> selectByDocumentId(@Param("documentId") Long documentId,
                                                   @Param("limit") Integer limit);

    /**
     * 删除过期日志
     *
     * @param beforeTime 时间点
     * @return 删除行数
     */
    int deleteOldLogs(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查询用户在某文档的操作次数
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     * @return 操作次数
     */
    int countByDocumentAndUser(@Param("documentId") Long documentId,
                              @Param("userId") Long userId);
}
