package com.ruoyi.im.service;

import com.ruoyi.im.dto.file.ImFileUploadRequest;
import com.ruoyi.im.vo.file.ImFileStatisticsVO;
import com.ruoyi.im.vo.file.ImFileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.domain.ImFileAsset;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文件服务接口
 *
 * @author ruoyi
 */
public interface ImFileService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param userId 用户ID
     * @return 文件信息
     */
    ImFileVO uploadFile(MultipartFile file, Long userId);

    /**
     * 批量上传文件
     *
     * @param files 文件列表
     * @param userId 用户ID
     * @return 文件信息列表
     */
    List<ImFileVO> uploadFiles(List<MultipartFile> files, Long userId);

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @param userId 用户ID
     */
    void downloadFile(Long fileId, Long userId);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @param userId 用户ID
     */
    void deleteFile(Long fileId, Long userId);

    /**
     * 批量删除文件
     * @param ids ID列表
     */
    void deleteFiles(List<Long> ids);

    /**
     * 获取文件列表
     *
     * @param userId 用户ID
     * @param fileType 文件类型
     * @return 文件列表
     */
    List<ImFileVO> getFileList(Long userId, String fileType);

    /**
     * 管理端获取文件列表（分页）
     */
    IPage<ImFileAsset> getAdminFileList(Integer pageNum, Integer pageSize, String fileName, 
                                       String fileType, Long uploaderId, LocalDateTime startTime, 
                                       LocalDateTime endTime);

    /**
     * 获取文件详情
     *
     * @param fileId 文件ID
     * @return 文件详情
     */
    ImFileVO getFileById(Long fileId);

    /**
     * 获取存储统计
     *
     * @return 存储统计信息
     */
    ImFileStatisticsVO getStorageStatistics();

    /**
     * 按文件类型统计
     */
    List<Map<String, Object>> getFileStatisticsByType();

    /**
     * 按上传者统计
     */
    List<Map<String, Object>> getFileStatisticsByUploader();
}
