package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImFileChunkDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分片文件详情Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImFileChunkDetailMapper extends BaseMapper<ImFileChunkDetail> {

    /**
     * 根据上传ID查询所有分片
     *
     * @param uploadId 上传ID
     * @return 分片详情列表
     */
    List<ImFileChunkDetail> selectByUploadId(@Param("uploadId") String uploadId);

    /**
     * 根据上传ID查询已完成的分片
     *
     * @param uploadId 上传ID
     * @return 已完成的分片列表
     */
    List<ImFileChunkDetail> selectCompletedByUploadId(@Param("uploadId") String uploadId);

    /**
     * 根据上传ID和分片序号查询
     *
     * @param uploadId 上传ID
     * @param chunkNumber 分片序号
     * @return 分片详情
     */
    ImFileChunkDetail selectByUploadIdAndChunkNumber(@Param("uploadId") String uploadId,
                                                      @Param("chunkNumber") Integer chunkNumber);

    /**
     * 批量插入分片记录
     *
     * @param chunkDetails 分片详情列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<ImFileChunkDetail> chunkDetails);

    /**
     * 更新分片状态
     *
     * @param uploadId 上传ID
     * @param chunkNumber 分片序号
     * @param status 状态
     * @param chunkPath 分片路径
     * @return 影响行数
     */
    int updateChunkStatus(@Param("uploadId") String uploadId,
                          @Param("chunkNumber") Integer chunkNumber,
                          @Param("status") String status,
                          @Param("chunkPath") String chunkPath);

    /**
     * 根据上传ID删除所有分片记录
     *
     * @param uploadId 上传ID
     * @return 影响行数
     */
    int deleteByUploadId(@Param("uploadId") String uploadId);
}
