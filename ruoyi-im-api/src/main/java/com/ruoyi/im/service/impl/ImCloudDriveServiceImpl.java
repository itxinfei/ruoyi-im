package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImCloudFile;
import com.ruoyi.im.domain.ImCloudFileShare;
import com.ruoyi.im.domain.ImCloudFileVersion;
import com.ruoyi.im.domain.ImCloudFolder;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.cloud.ImCloudFileMoveRequest;
import com.ruoyi.im.dto.cloud.ImCloudFileShareRequest;
import com.ruoyi.im.dto.cloud.ImCloudFolderCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImCloudFileMapper;
import com.ruoyi.im.mapper.ImCloudFileShareMapper;
import com.ruoyi.im.mapper.ImCloudFileVersionMapper;
import com.ruoyi.im.mapper.ImCloudFolderMapper;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImCloudDriveService;
import com.ruoyi.im.util.FileUtils;
import com.ruoyi.im.vo.cloud.ImCloudFileShareVO;
import com.ruoyi.im.vo.cloud.ImCloudFileVO;
import com.ruoyi.im.vo.cloud.ImCloudFolderVO;
import com.ruoyi.im.vo.cloud.ImCloudStorageQuotaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.stream.Collectors;

/**
 * 企业云盘服务实现
 *
 * @author ruoyi
 */
@Service
public class ImCloudDriveServiceImpl implements ImCloudDriveService {

    private static final Logger logger = LoggerFactory.getLogger(ImCloudDriveServiceImpl.class);

    /**
     * 个人存储默认配额 5GB
     */
    private static final long PERSONAL_QUOTA = 5L * 1024 * 1024 * 1024;

    /**
     * 部门存储默认配额 50GB
     */
    private static final long DEPARTMENT_QUOTA = 50L * 1024 * 1024 * 1024;

    /**
     * 公司存储默认配额 500GB
     */
    private static final long COMPANY_QUOTA = 500L * 1024 * 1024 * 1024;

    @Autowired
    private ImCloudFolderMapper cloudFolderMapper;

    @Autowired
    private ImCloudFileMapper cloudFileMapper;

    @Autowired
    private ImCloudFileShareMapper cloudFileShareMapper;

    @Autowired
    private ImCloudFileVersionMapper cloudFileVersionMapper;

    @Autowired
    private ImFileAssetMapper fileAssetMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(ImCloudFolderCreateRequest request, Long userId) {
        // 检查父文件夹是否存在
        if (request.getParentId() != 0) {
            ImCloudFolder parentFolder = cloudFolderMapper.selectById(request.getParentId());
            if (parentFolder == null) {
                throw new BusinessException("父文件夹不存在");
            }
        }

        // 检查同名文件夹
        ImCloudFolder existFolder = cloudFolderMapper.selectByNameAndOwner(
                request.getParentId(), request.getFolderName(), userId);
        if (existFolder != null) {
            throw new BusinessException("同名文件夹已存在");
        }

        ImCloudFolder folder = new ImCloudFolder();
        folder.setFolderName(request.getFolderName());
        folder.setParentId(request.getParentId());
        folder.setOwnerId(userId);
        folder.setOwnerType(request.getOwnerType());
        folder.setDepartmentId(request.getDepartmentId());
        folder.setAccessPermission(request.getAccessPermission() != null ? request.getAccessPermission() : "PRIVATE");
        folder.setIsDeleted(false);
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());

        // 计算路径和层级
        if (request.getParentId() == 0) {
            folder.setFolderPath("/");
            folder.setLevel(0);
        } else {
            ImCloudFolder parent = cloudFolderMapper.selectById(request.getParentId());
            folder.setFolderPath(parent.getFolderPath() + request.getFolderName() + "/");
            folder.setLevel(parent.getLevel() + 1);
        }

        folder.setSortOrder(0);
        cloudFolderMapper.insert(folder);

        logger.info("创建文件夹成功: folderId={}, userId={}", folder.getId(), userId);
        return folder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFolder(Long folderId, String newName, Long userId) {
        ImCloudFolder folder = cloudFolderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }

        if (!folder.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限操作此文件夹");
        }

        folder.setFolderName(newName);
        folder.setUpdateTime(LocalDateTime.now());
        cloudFolderMapper.updateById(folder);

        logger.info("重命名文件夹成功: folderId={}, newName={}", folderId, newName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long folderId, Long userId) {
        ImCloudFolder folder = cloudFolderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }

        if (!folder.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限操作此文件夹");
        }

        folder.setIsDeleted(true);
        folder.setDeleteTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());
        cloudFolderMapper.updateById(folder);

        // 同时删除子文件夹和文件（软删除）
        deleteChildFolders(folderId);
        deleteFilesInFolder(folderId);

        logger.info("删除文件夹成功: folderId={}, userId={}", folderId, userId);
    }

    /**
     * 递归删除子文件夹
     */
    private void deleteChildFolders(Long parentId) {
        List<ImCloudFolder> children = cloudFolderMapper.selectList(
                new LambdaQueryWrapper<ImCloudFolder>()
                        .eq(ImCloudFolder::getParentId, parentId)
                        .eq(ImCloudFolder::getIsDeleted, false)
        );

        for (ImCloudFolder child : children) {
            child.setIsDeleted(true);
            child.setDeleteTime(LocalDateTime.now());
            cloudFolderMapper.updateById(child);
            deleteChildFolders(child.getId());
        }
    }

    /**
     * 删除文件夹内的文件
     */
    private void deleteFilesInFolder(Long folderId) {
        List<ImCloudFile> files = cloudFileMapper.selectList(
                new LambdaQueryWrapper<ImCloudFile>()
                        .eq(ImCloudFile::getFolderId, folderId)
                        .eq(ImCloudFile::getIsDeleted, false)
        );

        for (ImCloudFile file : files) {
            file.setIsDeleted(true);
            file.setDeleteTime(LocalDateTime.now());
            cloudFileMapper.updateById(file);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteFolder(Long folderId, Long userId) {
        ImCloudFolder folder = cloudFolderMapper.selectById(folderId);
        if (folder == null || !folder.getIsDeleted()) {
            throw new BusinessException("文件夹不存在或未在回收站");
        }

        if (!folder.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限操作此文件夹");
        }

        cloudFolderMapper.deleteById(folderId);
        logger.info("永久删除文件夹成功: folderId={}", folderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFolder(Long folderId, Long userId) {
        ImCloudFolder folder = cloudFolderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }

        if (!folder.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限操作此文件夹");
        }

        folder.setIsDeleted(false);
        folder.setDeleteTime(null);
        folder.setUpdateTime(LocalDateTime.now());
        cloudFolderMapper.updateById(folder);

        // 恢复子文件夹和文件
        restoreChildFolders(folderId);
        restoreFilesInFolder(folderId);

        logger.info("恢复文件夹成功: folderId={}", folderId);
    }

    private void restoreChildFolders(Long parentId) {
        List<ImCloudFolder> children = cloudFolderMapper.selectList(
                new LambdaQueryWrapper<ImCloudFolder>()
                        .eq(ImCloudFolder::getParentId, parentId)
                        .eq(ImCloudFolder::getIsDeleted, true)
        );

        for (ImCloudFolder child : children) {
            child.setIsDeleted(false);
            child.setDeleteTime(null);
            cloudFolderMapper.updateById(child);
            restoreChildFolders(child.getId());
        }
    }

    private void restoreFilesInFolder(Long folderId) {
        List<ImCloudFile> files = cloudFileMapper.selectList(
                new LambdaQueryWrapper<ImCloudFile>()
                        .eq(ImCloudFile::getFolderId, folderId)
                        .eq(ImCloudFile::getIsDeleted, true)
        );

        for (ImCloudFile file : files) {
            file.setIsDeleted(false);
            file.setDeleteTime(null);
            cloudFileMapper.updateById(file);
        }
    }

    @Override
    public List<ImCloudFolderVO> getFolderList(Long parentId, String ownerType, Long userId) {
        List<ImCloudFolder> folders = cloudFolderMapper.selectUserFolders(userId, parentId, ownerType);

        return folders.stream().map(folder -> {
            ImCloudFolderVO vo = new ImCloudFolderVO();
            BeanUtils.copyProperties(folder, vo);

            // 获取所有者信息
            ImUser owner = userMapper.selectById(folder.getOwnerId());
            if (owner != null) {
                vo.setOwnerName(owner.getNickname());
            }

            // 统计子文件夹和文件数量
            vo.setSubFolderCount(cloudFolderMapper.countSubFolders(folder.getId()));
            vo.setFileCount(cloudFolderMapper.countFilesInFolder(folder.getId()));

            // 权限判断
            vo.setCanEdit(folder.getOwnerId().equals(userId));
            vo.setCanDelete(folder.getOwnerId().equals(userId));

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ImCloudFolderVO> getFolderPath(Long folderId) {
        List<ImCloudFolder> folders = cloudFolderMapper.selectFolderPath(folderId);
        return folders.stream().map(folder -> {
            ImCloudFolderVO vo = new ImCloudFolderVO();
            BeanUtils.copyProperties(folder, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImCloudFileVO uploadFile(Long folderId, MultipartFile file, Long userId) {
        // 检查文件夹是否存在
        if (folderId != 0) {
            ImCloudFolder folder = cloudFolderMapper.selectById(folderId);
            if (folder == null) {
                throw new BusinessException("文件夹不存在");
            }
        }

        // 检查存储配额
        Long usedSize = cloudFileMapper.sumUserFileSize(userId);
        Long totalQuota = PERSONAL_QUOTA;
        if (usedSize + file.getSize() > totalQuota) {
            throw new BusinessException("存储空间不足");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = FileUtils.getFileExtension(originalFilename);
        String fileType = FileUtils.getFileType(fileExtension);

        // 保存文件
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        String datePath = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = "cloud/" + datePath + "/" + fileName;
        String filePath = uploadPath + relativePath;

        File targetFile = new File(filePath);
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            file.transferTo(targetFile);
        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }

        // 创建文件资产记录
        ImFileAsset fileAsset = new ImFileAsset();
        fileAsset.setFileName(originalFilename);
        fileAsset.setFilePath(relativePath);
        fileAsset.setFileSize(file.getSize());
        fileAsset.setFileType(fileType);
        fileAsset.setFileExt(fileExtension);
        fileAsset.setUploaderId(userId);
        fileAsset.setDownloadCount(0);
        fileAsset.setStatus("ACTIVE");
        fileAsset.setCreateTime(LocalDateTime.now());
        fileAssetMapper.insert(fileAsset);

        // 创建云盘文件记录
        ImCloudFile cloudFile = new ImCloudFile();
        cloudFile.setFileAssetId(fileAsset.getId());
        cloudFile.setFolderId(folderId);
        cloudFile.setFileName(originalFilename);
        cloudFile.setFileSize(file.getSize());
        cloudFile.setFileType(fileType);
        cloudFile.setFileExt(fileExtension);
        cloudFile.setMimeType(file.getContentType());
        cloudFile.setUploaderId(userId);
        cloudFile.setDownloadCount(0);
        cloudFile.setPreviewCount(0);
        cloudFile.setAccessPermission("PRIVATE");
        cloudFile.setIsDeleted(false);
        cloudFile.setCreateTime(LocalDateTime.now());
        cloudFile.setUpdateTime(LocalDateTime.now());
        cloudFileMapper.insert(cloudFile);

        // 获取上传者信息
        ImUser uploader = userMapper.selectById(userId);
        if (uploader != null) {
            cloudFile.setUploaderName(uploader.getNickname());
            cloudFileMapper.updateById(cloudFile);
        }

        logger.info("上传文件成功: fileId={}, userId={}", cloudFile.getId(), userId);
        return convertToVO(cloudFile, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFile(Long fileId, String newName, Long userId) {
        ImCloudFile cloudFile = cloudFileMapper.selectById(fileId);
        if (cloudFile == null) {
            throw new BusinessException("文件不存在");
        }

        if (!cloudFile.getUploaderId().equals(userId)) {
            throw new BusinessException("无权限操作此文件");
        }

        cloudFile.setFileName(newName);
        cloudFile.setUpdateTime(LocalDateTime.now());
        cloudFileMapper.updateById(cloudFile);

        logger.info("重命名文件成功: fileId={}, newName={}", fileId, newName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId, Long userId) {
        ImCloudFile cloudFile = cloudFileMapper.selectById(fileId);
        if (cloudFile == null) {
            throw new BusinessException("文件不存在");
        }

        if (!cloudFile.getUploaderId().equals(userId)) {
            throw new BusinessException("无权限操作此文件");
        }

        cloudFile.setIsDeleted(true);
        cloudFile.setDeleteTime(LocalDateTime.now());
        cloudFile.setUpdateTime(LocalDateTime.now());
        cloudFileMapper.updateById(cloudFile);

        logger.info("删除文件成功: fileId={}, userId={}", fileId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteFile(Long fileId, Long userId) {
        ImCloudFile cloudFile = cloudFileMapper.selectById(fileId);
        if (cloudFile == null || !cloudFile.getIsDeleted()) {
            throw new BusinessException("文件不存在或未在回收站");
        }

        if (!cloudFile.getUploaderId().equals(userId)) {
            throw new BusinessException("无权限操作此文件");
        }

        // 删除物理文件
        if (cloudFile.getFileAssetId() != null) {
            ImFileAsset fileAsset = fileAssetMapper.selectById(cloudFile.getFileAssetId());
            if (fileAsset != null) {
                // TODO: 删除物理文件
                fileAssetMapper.deleteById(fileAsset.getId());
            }
        }

        cloudFileMapper.deleteById(fileId);
        logger.info("永久删除文件成功: fileId={}", fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFile(Long fileId, Long userId) {
        ImCloudFile cloudFile = cloudFileMapper.selectById(fileId);
        if (cloudFile == null) {
            throw new BusinessException("文件不存在");
        }

        if (!cloudFile.getUploaderId().equals(userId)) {
            throw new BusinessException("无权限操作此文件");
        }

        cloudFile.setIsDeleted(false);
        cloudFile.setDeleteTime(null);
        cloudFile.setUpdateTime(LocalDateTime.now());
        cloudFileMapper.updateById(cloudFile);

        logger.info("恢复文件成功: fileId={}", fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveFiles(ImCloudFileMoveRequest request, Long userId) {
        // 检查目标文件夹
        if (request.getTargetFolderId() != 0) {
            ImCloudFolder targetFolder = cloudFolderMapper.selectById(request.getTargetFolderId());
            if (targetFolder == null) {
                throw new BusinessException("目标文件夹不存在");
            }
        }

        cloudFileMapper.batchUpdateFolder(request.getFileIds(), request.getTargetFolderId());
        logger.info("移动文件成功: fileCount={}, targetFolderId={}",
                request.getFileIds().size(), request.getTargetFolderId());
    }

    @Override
    public List<ImCloudFileVO> getFileList(Long folderId, Long userId) {
        List<ImCloudFile> files = cloudFileMapper.selectByFolderId(folderId, userId);
        return files.stream().map(f -> convertToVO(f, userId)).collect(Collectors.toList());
    }

    @Override
    public List<ImCloudFileVO> searchFiles(String keyword, String fileType, Long userId) {
        List<ImCloudFile> files = cloudFileMapper.searchFiles(userId, keyword, fileType);
        return files.stream().map(f -> convertToVO(f, userId)).collect(Collectors.toList());
    }

    @Override
    public List<ImCloudFileVO> getRecentFiles(Long userId, Integer limit) {
        int queryLimit = limit != null && limit > 0 ? limit : 10;
        List<ImCloudFile> files = cloudFileMapper.selectRecentFiles(userId, queryLimit);
        return files.stream().map(f -> convertToVO(f, userId)).collect(Collectors.toList());
    }

    @Override
    public List<ImCloudFileVO> getRecycleBin(Long userId) {
        List<ImCloudFile> files = cloudFileMapper.selectList(
                new LambdaQueryWrapper<ImCloudFile>()
                        .eq(ImCloudFile::getUploaderId, userId)
                        .eq(ImCloudFile::getIsDeleted, true)
                        .orderByDesc(ImCloudFile::getDeleteTime)
        );

        return files.stream().map(f -> convertToVO(f, userId)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImCloudFileShareVO createShare(ImCloudFileShareRequest request, Long userId) {
        // 生成唯一分享码
        String shareCode = generateShareCode();

        ImCloudFileShare share = new ImCloudFileShare();
        share.setShareType(request.getShareType());
        share.setResourceId(request.getResourceId());
        share.setSharerId(userId);
        share.setShareCode(shareCode);
        share.setShareLink(generateShareLink(shareCode));
        share.setAccessPassword(request.getAccessPassword());
        share.setExpireDays(request.getExpireDays() != null ? request.getExpireDays() : 0);
        share.setAllowDownload(request.getAllowDownload() != null ? request.getAllowDownload() : true);
        share.setAllowPreview(request.getAllowPreview() != null ? request.getAllowPreview() : true);
        share.setViewCount(0);
        share.setDownloadCount(0);
        share.setStatus("ACTIVE");
        share.setCreateTime(LocalDateTime.now());
        share.setUpdateTime(LocalDateTime.now());

        // 设置过期时间
        if (request.getExpireDays() != null && request.getExpireDays() > 0) {
            share.setExpireTime(LocalDateTime.now().plusDays(request.getExpireDays()));
        }

        cloudFileShareMapper.insert(share);

        ImCloudFileShareVO vo = new ImCloudFileShareVO();
        BeanUtils.copyProperties(share, vo);
        vo.setHasPassword(share.getAccessPassword() != null && !share.getAccessPassword().isEmpty());

        // 获取分享者信息
        ImUser sharer = userMapper.selectById(userId);
        if (sharer != null) {
            vo.setSharerName(sharer.getNickname());
        }

        // 获取资源名称
        if ("FILE".equals(request.getShareType())) {
            ImCloudFile file = cloudFileMapper.selectById(request.getResourceId());
            if (file != null) {
                vo.setResourceName(file.getFileName());
            }
        } else {
            ImCloudFolder folder = cloudFolderMapper.selectById(request.getResourceId());
            if (folder != null) {
                vo.setResourceName(folder.getFolderName());
            }
        }

        logger.info("创建分享成功: shareId={}, shareCode={}", share.getId(), shareCode);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelShare(Long shareId, Long userId) {
        ImCloudFileShare share = cloudFileShareMapper.selectById(shareId);
        if (share == null) {
            throw new BusinessException("分享不存在");
        }

        if (!share.getSharerId().equals(userId)) {
            throw new BusinessException("无权限操作此分享");
        }

        share.setStatus("CANCELLED");
        share.setUpdateTime(LocalDateTime.now());
        cloudFileShareMapper.updateById(share);

        logger.info("取消分享成功: shareId={}", shareId);
    }

    @Override
    public List<ImCloudFileShareVO> getShareList(Long userId) {
        List<ImCloudFileShare> shares = cloudFileShareMapper.selectBySharerId(userId);

        return shares.stream().map(share -> {
            ImCloudFileShareVO vo = new ImCloudFileShareVO();
            BeanUtils.copyProperties(share, vo);
            vo.setHasPassword(share.getAccessPassword() != null && !share.getAccessPassword().isEmpty());
            vo.setIsExpired(share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now()));

            // 获取资源名称
            if ("FILE".equals(share.getShareType())) {
                ImCloudFile file = cloudFileMapper.selectById(share.getResourceId());
                if (file != null) {
                    vo.setResourceName(file.getFileName());
                }
            } else {
                ImCloudFolder folder = cloudFolderMapper.selectById(share.getResourceId());
                if (folder != null) {
                    vo.setResourceName(folder.getFolderName());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImCloudFileShareVO accessShare(String shareCode, String accessPassword) {
        ImCloudFileShare share = cloudFileShareMapper.selectByShareCode(shareCode);
        if (share == null) {
            throw new BusinessException("分享不存在或已失效");
        }

        if ("CANCELLED".equals(share.getStatus())) {
            throw new BusinessException("分享已取消");
        }

        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("分享已过期");
        }

        // 验证密码
        if (share.getAccessPassword() != null && !share.getAccessPassword().isEmpty()) {
            if (!share.getAccessPassword().equals(accessPassword)) {
                throw new BusinessException("密码错误");
            }
        }

        // 增加访问次数
        cloudFileShareMapper.incrementViewCount(share.getId());

        ImCloudFileShareVO vo = new ImCloudFileShareVO();
        BeanUtils.copyProperties(share, vo);
        vo.setHasPassword(share.getAccessPassword() != null && !share.getAccessPassword().isEmpty());

        return vo;
    }

    @Override
    public ImCloudStorageQuotaVO getStorageQuota(Long userId) {
        ImCloudStorageQuotaVO vo = new ImCloudStorageQuotaVO();

        Long totalCapacity = PERSONAL_QUOTA;
        Long usedCapacity = cloudFileMapper.sumUserFileSize(userId);
        if (usedCapacity == null) {
            usedCapacity = 0L;
        }

        Integer fileCount = cloudFileMapper.countUserFiles(userId);
        Integer folderCount = cloudFolderMapper.selectCount(
                new LambdaQueryWrapper<ImCloudFolder>()
                        .eq(ImCloudFolder::getOwnerId, userId)
                        .eq(ImCloudFolder::getIsDeleted, false)
        ).intValue();

        Long availableCapacity = totalCapacity - usedCapacity;
        Double usageRate = (double) usedCapacity / totalCapacity * 100;

        vo.setTotalCapacity(totalCapacity);
        vo.setUsedCapacity(usedCapacity);
        vo.setAvailableCapacity(availableCapacity);
        vo.setUsageRate(usageRate);
        vo.setFileCount(fileCount != null ? fileCount : 0);
        vo.setFolderCount(folderCount);

        // 格式化显示
        vo.setTotalCapacityFormat(formatFileSize(totalCapacity));
        vo.setUsedCapacityFormat(formatFileSize(usedCapacity));
        vo.setAvailableCapacityFormat(formatFileSize(availableCapacity));

        return vo;
    }

    /**
     * 转换为VO
     */
    private ImCloudFileVO convertToVO(ImCloudFile cloudFile, Long currentUserId) {
        ImCloudFileVO vo = new ImCloudFileVO();
        BeanUtils.copyProperties(cloudFile, vo);

        // 格式化文件大小
        vo.setFileSizeFormat(formatFileSize(cloudFile.getFileSize()));

        // 设置文件URL
        if (cloudFile.getFileAssetId() != null) {
            ImFileAsset asset = fileAssetMapper.selectById(cloudFile.getFileAssetId());
            if (asset != null) {
                vo.setFileUrl(urlPrefix + asset.getFilePath());
            }
        }

        // 获取上传者信息
        ImUser uploader = userMapper.selectById(cloudFile.getUploaderId());
        if (uploader != null) {
            vo.setUploaderName(uploader.getNickname());
            vo.setUploaderAvatar(uploader.getAvatar());
        }

        // 权限判断
        vo.setCanEdit(cloudFile.getUploaderId().equals(currentUserId));
        vo.setCanDelete(cloudFile.getUploaderId().equals(currentUserId));
        vo.setCanDownload(true);
        vo.setCanShare(cloudFile.getUploaderId().equals(currentUserId));

        // 获取版本号
        ImCloudFileVersion latestVersion = cloudFileVersionMapper.selectLatestVersion(cloudFile.getId());
        if (latestVersion != null) {
            vo.setLatestVersion(latestVersion.getVersionNumber());
        } else {
            vo.setLatestVersion(1);
        }

        return vo;
    }

    /**
     * 生成分享码
     */
    private String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 生成分享链接
     */
    private String generateShareLink(String shareCode) {
        // 返回相对路径，前端拼接域名
        return "/share/" + shareCode;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();

        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }
}
