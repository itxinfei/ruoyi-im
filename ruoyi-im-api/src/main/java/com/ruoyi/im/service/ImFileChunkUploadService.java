package com.ruoyi.im.service;

import com.ruoyi.im.dto.file.ImFileChunkMergeRequest;
import com.ruoyi.im.dto.file.ImFileChunkUploadInitRequest;
import com.ruoyi.im.dto.file.ImFileChunkUploadRequest;
import com.ruoyi.im.vo.file.ImFileChunkUploadInitVO;
import com.ruoyi.im.vo.file.ImFileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分片上传服务接口
 *
 * @author ruoyi
 */
public interface ImFileChunkUploadService {

    /**
     * 初始化分片上传
     * 检查文件是否已存在（秒传），创建上传任务和分片记录
     *
     * @param request 初始化请求
     * @param userId 用户ID
     * @return 初始化结果
     */
    ImFileChunkUploadInitVO initChunkUpload(ImFileChunkUploadInitRequest request, Long userId);

    /**
     * 上传分片
     * 保存单个分片文件
     *
     * @param uploadId 上传ID
     * @param chunkNumber 分片序号
     * @param file 分片文件
     * @param userId 用户ID
     * @return 上传结果
     */
    Boolean uploadChunk(String uploadId, Integer chunkNumber, MultipartFile file, Long userId);

    /**
     * 合并分片
     * 将所有分片合并成完整文件
     *
     * @param request 合并请求
     * @param userId 用户ID
     * @return 文件信息
     */
    ImFileVO mergeChunks(ImFileChunkMergeRequest request, Long userId);

    /**
     * 取消分片上传
     * 删除上传记录和临时分片文件
     *
     * @param uploadId 上传ID
     * @param userId 用户ID
     */
    void cancelChunkUpload(String uploadId, Long userId);

    /**
     * 暂停分片上传
     *
     * @param uploadId 上传ID
     * @param userId 用户ID
     */
    void pauseChunkUpload(String uploadId, Long userId);

    /**
     * 恢复分片上传
     *
     * @param uploadId 上传ID
     * @param userId 用户ID
     * @return 初始化结果
     */
    ImFileChunkUploadInitVO resumeChunkUpload(String uploadId, Long userId);

    /**
     * 获取上传进度
     *
     * @param uploadId 上传ID
     * @param userId 用户ID
     * @return 进度百分比（0-100）
     */
    Integer getUploadProgress(String uploadId, Long userId);
}
