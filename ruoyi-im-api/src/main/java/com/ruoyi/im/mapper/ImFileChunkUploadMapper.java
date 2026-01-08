package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImFileChunkUpload;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分片上传记录Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImFileChunkUploadMapper extends BaseMapper<ImFileChunkUpload> {

    /**
     * 根据上传ID查询记录
     *
     * @param uploadId 上传ID
     * @return 分片上传记录
     */
    ImFileChunkUpload selectByUploadId(@Param("uploadId") String uploadId);

    /**
     * 根据用户ID查询进行中的上传任务
     *
     * @param userId 用户ID
     * @return 上传任务列表
     */
    List<ImFileChunkUpload> selectUploadingByUserId(@Param("userId") Long userId);

    /**
     * 根据文件MD5查询已完成的上传（用于秒传）
     *
     * @param fileMd5 文件MD5
     * @return 已完成的上传记录
     */
    ImFileChunkUpload selectCompletedByMd5(@Param("fileMd5") String fileMd5);

    /**
     * 更新已上传分片数
     *
     * @param uploadId 上传ID
     * @param uploadedChunks 已上传分片数
     * @return 影响行数
     */
    int updateUploadedChunks(@Param("uploadId") String uploadId,
                             @Param("uploadedChunks") Integer uploadedChunks);

    /**
     * 更新上传状态
     *
     * @param uploadId 上传ID
     * @param status 状态
     * @param filePath 文件路径
     * @param fileUrl 文件URL
     * @return 影响行数
     */
    int updateStatus(@Param("uploadId") String uploadId,
                     @Param("status") String status,
                     @Param("filePath") String filePath,
                     @Param("fileUrl") String fileUrl);

    /**
     * 删除过期的上传记录
     *
     * @param expireTime 过期时间
     * @return 影响行数
     */
    int deleteExpired(@Param("expireTime") LocalDateTime expireTime);
}
