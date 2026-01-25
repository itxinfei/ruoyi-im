package com.ruoyi.im.service;

import com.ruoyi.im.dto.cloud.ImCloudFileMoveRequest;
import com.ruoyi.im.dto.cloud.ImCloudFileShareRequest;
import com.ruoyi.im.dto.cloud.ImCloudFolderCreateRequest;
import com.ruoyi.im.vo.cloud.ImCloudFileShareVO;
import com.ruoyi.im.vo.cloud.ImCloudFileVO;
import com.ruoyi.im.vo.cloud.ImCloudFolderVO;
import com.ruoyi.im.vo.cloud.ImCloudStorageQuotaVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 企业云盘服务接口
 *
 * @author ruoyi
 */
public interface ImCloudDriveService {

    // ==================== 文件夹管理 ====================

    /**
     * 创建文件夹
     *
     * @param request 创建请求
     * @param userId  操作用户ID
     * @return 文件夹ID
     */
    Long createFolder(ImCloudFolderCreateRequest request, Long userId);

    /**
     * 重命名文件夹
     *
     * @param folderId  文件夹ID
     * @param newName   新名称
     * @param userId    操作用户ID
     */
    void renameFolder(Long folderId, String newName, Long userId);

    /**
     * 删除文件夹（移入回收站）
     *
     * @param folderId 文件夹ID
     * @param userId   操作用户ID
     */
    void deleteFolder(Long folderId, Long userId);

    /**
     * 永久删除文件夹
     *
     * @param folderId 文件夹ID
     * @param userId   操作用户ID
     */
    void permanentlyDeleteFolder(Long folderId, Long userId);

    /**
     * 恢复文件夹
     *
     * @param folderId 文件夹ID
     * @param userId   操作用户ID
     */
    void restoreFolder(Long folderId, Long userId);

    /**
     * 获取文件夹列表
     *
     * @param parentId  父文件夹ID
     * @param ownerType 所有者类型
     * @param userId    用户ID
     * @return 文件夹列表
     */
    List<ImCloudFolderVO> getFolderList(Long parentId, String ownerType, Long userId);

    /**
     * 获取文件夹路径
     *
     * @param folderId 文件夹ID
     * @return 路径文件夹列表
     */
    List<ImCloudFolderVO> getFolderPath(Long folderId);

    // ==================== 文件管理 ====================

    /**
     * 上传文件
     *
     * @param folderId 文件夹ID
     * @param file     文件
     * @param userId   用户ID
     * @return 文件VO
     */
    ImCloudFileVO uploadFile(Long folderId, MultipartFile file, Long userId);

    /**
     * 重命名文件
     *
     * @param fileId  文件ID
     * @param newName 新名称
     * @param userId  操作用户ID
     */
    void renameFile(Long fileId, String newName, Long userId);

    /**
     * 删除文件（移入回收站）
     *
     * @param fileId 文件ID
     * @param userId 操作用户ID
     */
    void deleteFile(Long fileId, Long userId);

    /**
     * 永久删除文件
     *
     * @param fileId 文件ID
     * @param userId 操作用户ID
     */
    void permanentlyDeleteFile(Long fileId, Long userId);

    /**
     * 恢复文件
     *
     * @param fileId 文件ID
     * @param userId 操作用户ID
     */
    void restoreFile(Long fileId, Long userId);

    /**
     * 移动文件
     *
     * @param request 移动请求
     * @param userId  操作用户ID
     */
    void moveFiles(ImCloudFileMoveRequest request, Long userId);

    /**
     * 获取文件夹内的文件列表
     *
     * @param folderId 文件夹ID
     * @param userId   用户ID
     * @return 文件列表
     */
    List<ImCloudFileVO> getFileList(Long folderId, Long userId);

    /**
     * 搜索文件
     *
     * @param keyword  关键词
     * @param fileType 文件类型
     * @param userId   用户ID
     * @return 文件列表
     */
    List<ImCloudFileVO> searchFiles(String keyword, String fileType, Long userId);

    /**
     * 获取最近上传的文件
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 文件列表
     */
    List<ImCloudFileVO> getRecentFiles(Long userId, Integer limit);

    /**
     * 获取回收站文件列表
     *
     * @param userId 用户ID
     * @return 文件列表
     */
    List<ImCloudFileVO> getRecycleBin(Long userId);

    // ==================== 文件分享 ====================

    /**
     * 创建分享链接
     *
     * @param request 分享请求
     * @param userId  操作用户ID
     * @return 分享VO
     */
    ImCloudFileShareVO createShare(ImCloudFileShareRequest request, Long userId);

    /**
     * 取消分享
     *
     * @param shareId 分享ID
     * @param userId  操作用户ID
     */
    void cancelShare(Long shareId, Long userId);

    /**
     * 获取分享列表
     *
     * @param userId 用户ID
     * @return 分享列表
     */
    List<ImCloudFileShareVO> getShareList(Long userId);

    /**
     * 通过分享码访问分享内容
     *
     * @param shareCode   分享码
     * @param accessPassword 访问密码
     * @return 分享VO
     */
    ImCloudFileShareVO accessShare(String shareCode, String accessPassword);

    // ==================== 存储配额 ====================

    /**
     * 获取存储配额信息
     *
     * @param userId 用户ID
     * @return 存储配额VO
     */
    ImCloudStorageQuotaVO getStorageQuota(Long userId);
}
