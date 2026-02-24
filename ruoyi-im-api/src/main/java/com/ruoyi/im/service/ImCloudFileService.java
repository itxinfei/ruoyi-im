package com.ruoyi.im.service;

import com.ruoyi.im.dto.cloud.ImCloudFileMoveRequest;
import com.ruoyi.im.vo.cloud.ImCloudFileVO;
import com.ruoyi.im.vo.cloud.ImCloudStorageQuotaVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 云盘文件服务接口
 */
public interface ImCloudFileService {

    ImCloudFileVO uploadFile(Long folderId, MultipartFile file, Long userId);

    void renameFile(Long fileId, String newName, Long userId);

    void deleteFile(Long fileId, Long userId);

    void permanentlyDeleteFile(Long fileId, Long userId);

    void restoreFile(Long fileId, Long userId);

    void moveFiles(ImCloudFileMoveRequest request, Long userId);

    List<ImCloudFileVO> getFileList(Long folderId, Long userId);

    List<ImCloudFileVO> searchFiles(String keyword, String fileType, Long userId);

    List<ImCloudFileVO> getRecentFiles(Long userId, Integer limit);

    List<ImCloudFileVO> getRecycleBin(Long userId);

    ImCloudStorageQuotaVO getStorageQuota(Long userId);
}
