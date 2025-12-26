package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImFileAsset;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 文件资产Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImFileAssetService extends IService<ImFileAsset> {

    /**
     * 上传文件
     * 
     * @param file 上传的文件
     * @param uploaderId 上传者ID
     * @param watermark 是否添加水印
     * @return 文件资产信息
     */
    ImFileAsset uploadFile(MultipartFile file, Long uploaderId, boolean watermark);

    /**
     * 批量上传文件
     * 
     * @param files 上传的文件列表
     * @param uploaderId 上传者ID
     * @param watermark 是否添加水印
     * @return 文件资产信息列表
     */
    List<ImFileAsset> uploadFiles(List<MultipartFile> files, Long uploaderId, boolean watermark);

    /**
     * 根据MD5查询文件（秒传功能）
     * 
     * @param md5 文件MD5值
     * @return 文件资产信息
     */
    ImFileAsset getFileByMd5(String md5);

    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean deleteFile(Long fileId, Long operatorId);

    /**
     * 批量删除文件
     * 
     * @param fileIds 文件ID列表
     * @param operatorId 操作者ID
     * @return 删除数量
     */
    int deleteFileBatch(List<Long> fileIds, Long operatorId);

    /**
     * 查询用户上传的文件列表
     * 
     * @param uploaderId 上传者ID
     * @param fileType 文件类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 文件列表
     */
    List<ImFileAsset> selectUserFiles(Long uploaderId, String fileType, Date startTime, Date endTime);

    /**
     * 搜索文件
     * 
     * @param keyword 搜索关键词
     * @param uploaderId 上传者ID（可选）
     * @param fileType 文件类型（可选）
     * @return 文件列表
     */
    List<ImFileAsset> searchFiles(String keyword, Long uploaderId, String fileType);

    /**
     * 获取文件详细信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    ImFileAsset getFileDetail(Long fileId);

    /**
     * 统计用户文件数量
     * 
     * @param uploaderId 上传者ID
     * @param fileType 文件类型（可选）
     * @return 文件数量
     */
    int countUserFiles(Long uploaderId, String fileType);

    /**
     * 统计用户文件总大小
     * 
     * @param uploaderId 上传者ID
     * @param fileType 文件类型（可选）
     * @return 文件总大小（字节）
     */
    long sumUserFileSize(Long uploaderId, String fileType);

    /**
     * 查询大文件列表
     * 
     * @param minSize 最小文件大小（字节）
     * @param limit 限制数量
     * @return 大文件列表
     */
    List<ImFileAsset> selectLargeFiles(long minSize, int limit);

    /**
     * 根据MIME类型查询文件
     * 
     * @param mimeType MIME类型
     * @param limit 限制数量
     * @return 文件列表
     */
    List<ImFileAsset> selectFilesByMimeType(String mimeType, int limit);

    /**
     * 清理过期文件
     * 
     * @param days 过期天数
     * @return 清理数量
     */
    int cleanExpiredFiles(int days);

    /**
     * 获取文件统计信息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> getFileStatistics(Date startTime, Date endTime);

    /**
     * 批量查询文件信息
     * 
     * @param fileIds 文件ID列表
     * @return 文件列表
     */
    List<ImFileAsset> selectFilesByIds(List<Long> fileIds);

    /**
     * 生成文件下载URL
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 下载URL
     */
    String generateDownloadUrl(Long fileId, Long userId);

    /**
     * 生成文件预览URL
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 预览URL
     */
    String generatePreviewUrl(Long fileId, Long userId);

    /**
     * 检查文件是否可预览
     * 
     * @param fileId 文件ID
     * @return 是否可预览
     */
    boolean isFilePreviewable(Long fileId);

    /**
     * 删除用户上传的所有文件
     * 
     * @param uploaderId 上传者ID
     * @return 删除数量
     */
    int deleteUserAllFiles(Long uploaderId);

    /**
     * 验证文件访问权限
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean validateFileAccess(Long fileId, Long userId);

    /**
     * 更新文件水印状态
     * 
     * @param fileId 文件ID
     * @param watermark 是否有水印
     * @return 是否成功
     */
    boolean updateFileWatermark(Long fileId, boolean watermark);
}