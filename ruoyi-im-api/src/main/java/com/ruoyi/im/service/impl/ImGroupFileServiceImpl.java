package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.domain.ImGroupFile;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.mapper.ImGroupFileMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImGroupFileService;
import com.ruoyi.im.util.BusinessExceptionHelper;

import java.util.stream.Collectors;
import com.ruoyi.im.dto.group.ImGroupFileQueryRequest;
import com.ruoyi.im.dto.group.ImGroupFileUpdateRequest;
import com.ruoyi.im.dto.group.ImGroupFileUploadRequest;
import com.ruoyi.im.vo.group.ImGroupFileStatisticsVO;
import com.ruoyi.im.vo.group.ImGroupFileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 群组文件服务实现
 *
 * @author ruoyi
 */
@Service
public class ImGroupFileServiceImpl implements ImGroupFileService {

    /** 文件已删除状态 */
    private static final String STATUS_DELETED = "DELETED";
    /** 文件正常状态 */
    private static final int STATUS_NORMAL = 1;
    /** 文件禁用状态 */
    private static final int STATUS_DISABLED = 0;
    /** 群主角色 */
    private static final String ROLE_OWNER = "OWNER";
    /** 管理员角色 */
    private static final String ROLE_ADMIN = "ADMIN";
    /** 所有人权限 */
    private static final String PERMISSION_ALL = "ALL";
    /** 文件大小单位：KB */
    private static final long SIZE_KB = 1024;
    /** 文件大小单位：MB */
    private static final long SIZE_MB = 1024 * 1024;
    /** 文件大小单位：GB */
    private static final long SIZE_GB = 1024 * 1024 * 1024;

    @Autowired
    private ImGroupFileMapper groupFileMapper;

    @Autowired
    private ImGroupMemberMapper groupMemberMapper;

    @Autowired
    private ImFileAssetMapper fileAssetMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Override
    @Transactional
    public Long uploadFile(ImGroupFileUploadRequest request, Long userId) {
        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(request.getGroupId(), userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotGroupMember();
        }

        // 验证文件资产是否存在
        ImFileAsset fileAsset = fileAssetMapper.selectById(request.getFileId());
        if (fileAsset == null || STATUS_DELETED.equals(fileAsset.getStatus())) {
            BusinessExceptionHelper.throwFileNotFound();
        }

        // 验证文件是否属于当前用户
        if (!fileAsset.getUploaderId().equals(userId)) {
            BusinessExceptionHelper.throwOnlyUploadOwnFile();
        }

        // 创建群组文件记录
        ImGroupFile groupFile = new ImGroupFile();
        groupFile.setGroupId(request.getGroupId());
        groupFile.setFileId(request.getFileId());
        groupFile.setFileName(request.getFileName() != null ? request.getFileName() : fileAsset.getFileName());
        groupFile.setFileType(fileAsset.getFileType());
        groupFile.setFileSize(fileAsset.getFileSize());
        groupFile.setCategory(request.getCategory());
        groupFile.setPermission(request.getPermission());
        groupFile.setUploaderId(userId);
        groupFile.setDownloadCount(0);
        groupFile.setStatus(STATUS_NORMAL);

        // 获取用户名
        ImUser user = userMapper.selectImUserById(userId);
        if (user != null) {
            groupFile.setUploaderName(user.getNickname());
        }

        groupFileMapper.insert(groupFile);
        return groupFile.getId();
    }

    @Override
    public IPage<ImGroupFileVO> getFileList(ImGroupFileQueryRequest request, Long userId) {
        // 验证用户是否为群组成员
        if (request.getGroupId() != null) {
            ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(request.getGroupId(), userId);
            if (member == null) {
                BusinessExceptionHelper.throwNotGroupMember();
            }
        }

        // 分页查询
        Page<ImGroupFile> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ImGroupFile> groupFilePage = groupFileMapper.selectFilePage(page, request);

        // 转换为VO
        Page<ImGroupFileVO> voPage = new Page<>(groupFilePage.getCurrent(), groupFilePage.getSize(), groupFilePage.getTotal());
        List<ImGroupFileVO> voList = groupFilePage.getRecords().stream().map(gf -> {
            ImGroupFileVO vo = new ImGroupFileVO();
            BeanUtils.copyProperties(gf, vo);
            vo.setFileSizeFormat(formatFileSize(gf.getFileSize()));

            // 判断权限
            ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(gf.getGroupId(), userId);
            if (member != null) {
                String role = member.getRole();
                boolean isOwner = ROLE_OWNER.equals(role);
                boolean isAdmin = ROLE_ADMIN.equals(role);
                boolean isUploader = gf.getUploaderId().equals(userId);

                vo.setCanDelete(isOwner || isAdmin || isUploader);
                vo.setCanDownload(PERMISSION_ALL.equals(gf.getPermission()) || isOwner || isAdmin || isUploader);
            } else {
                vo.setCanDelete(false);
                vo.setCanDownload(false);
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ImGroupFileStatisticsVO getStatistics(Long groupId, Long userId) {
        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotGroupMember();
        }

        ImGroupFileStatisticsVO statistics = groupFileMapper.selectStatisticsByGroupId(groupId);
        if (statistics == null) {
            statistics = new ImGroupFileStatisticsVO();
        }
        if (statistics.getTotalSize() != null) {
            statistics.setTotalSizeFormat(formatFileSize(statistics.getTotalSize()));
        }
        return statistics;
    }

    @Override
    public List<String> getCategories(Long groupId, Long userId) {
        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotGroupMember();
        }

        return groupFileMapper.selectCategoriesByGroupId(groupId);
    }

    @Override
    @Transactional
    public void updateFile(Long groupFileId, ImGroupFileUpdateRequest request, Long userId) {
        ImGroupFile groupFile = groupFileMapper.selectById(groupFileId);
        if (groupFile == null || groupFile.getStatus() == STATUS_DISABLED) {
            BusinessExceptionHelper.throwFileNotFound();
        }

        // 验证权限：只有上传者、群主、管理员可以编辑
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupFile.getGroupId(), userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotGroupMember();
        }

        String role = member.getRole();
        boolean isOwner = ROLE_OWNER.equals(role);
        boolean isAdmin = ROLE_ADMIN.equals(role);
        boolean isUploader = groupFile.getUploaderId().equals(userId);

        if (!isOwner && !isAdmin && !isUploader) {
            BusinessExceptionHelper.throwOnlyOwnerAdminOrUploaderCanEditFile();
        }

        // 更新文件信息
        if (request.getFileName() != null) {
            groupFile.setFileName(request.getFileName());
        }
        if (request.getCategory() != null) {
            groupFile.setCategory(request.getCategory());
        }
        if (request.getPermission() != null && (isOwner || isAdmin)) {
            groupFile.setPermission(request.getPermission());
        }

        groupFileMapper.updateById(groupFile);
    }

    @Override
    @Transactional
    public void deleteFile(Long groupFileId, Long userId) {
        ImGroupFile groupFile = groupFileMapper.selectById(groupFileId);
        if (groupFile == null || groupFile.getStatus() == STATUS_DISABLED) {
            BusinessExceptionHelper.throwFileNotFound();
        }

        // 验证权限：只有上传者、群主、管理员可以删除
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupFile.getGroupId(), userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotGroupMember();
        }

        String role = member.getRole();
        boolean isOwner = ROLE_OWNER.equals(role);
        boolean isAdmin = ROLE_ADMIN.equals(role);
        boolean isUploader = groupFile.getUploaderId().equals(userId);

        if (!isOwner && !isAdmin && !isUploader) {
            BusinessExceptionHelper.throwOnlyOwnerAdminOrUploaderCanDeleteFile();
        }

        // 软删除
        groupFile.setStatus(STATUS_DISABLED);
        groupFileMapper.updateById(groupFile);
    }

    @Override
    @Transactional
    public String downloadFile(Long groupFileId, Long userId) {
        ImGroupFile groupFile = groupFileMapper.selectById(groupFileId);
        if (groupFile == null || groupFile.getStatus() == STATUS_DISABLED) {
            BusinessExceptionHelper.throwFileNotFound();
        }

        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupFile.getGroupId(), userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotGroupMember();
        }

        // 验证下载权限
        String role = member.getRole();
        boolean isOwner = ROLE_OWNER.equals(role);
        boolean isAdmin = ROLE_ADMIN.equals(role);
        boolean isUploader = groupFile.getUploaderId().equals(userId);

        if (!PERMISSION_ALL.equals(groupFile.getPermission()) && !isOwner && !isAdmin && !isUploader) {
            BusinessExceptionHelper.throwNoPermissionToDownload();
        }

        // 获取文件URL
        ImFileAsset fileAsset = fileAssetMapper.selectById(groupFile.getFileId());
        if (fileAsset == null) {
            throw new BusinessException("文件不存在");
        }

        // 更新下载次数
        groupFileMapper.incrementDownloadCount(groupFileId);

        // 返回文件URL - 从配置的前缀和文件路径拼接
        return fileAsset.getFilePath();
    }

    @Override
    @Transactional
    public void batchDeleteFiles(List<Long> groupFileIds, Long userId) {
        for (Long groupFileId : groupFileIds) {
            try {
                deleteFile(groupFileId, userId);
            } catch (BusinessException e) {
                // 跳过无权限删除的文件
                continue;
            }
        }
    }

    @Override
    @Transactional
    public void moveFile(Long groupFileId, String category, Long userId) {
        ImGroupFile groupFile = groupFileMapper.selectById(groupFileId);
        if (groupFile == null || groupFile.getStatus() == STATUS_DISABLED) {
            BusinessExceptionHelper.throwFileNotFound();
        }

        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupFile.getGroupId(), userId);
        if (member == null) {
            BusinessExceptionHelper.throwNotGroupMember();
        }

        // 验证权限：只有上传者、群主、管理员可以移动文件
        String role = member.getRole();
        boolean isOwner = ROLE_OWNER.equals(role);
        boolean isAdmin = ROLE_ADMIN.equals(role);
        boolean isUploader = groupFile.getUploaderId().equals(userId);

        if (!isOwner && !isAdmin && !isUploader) {
            BusinessExceptionHelper.throwOnlyOwnerAdminOrUploaderCanEditFile();
        }

        groupFile.setCategory(category);
        groupFileMapper.updateById(groupFile);
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    private String formatFileSize(Long size) {
        if (size == null) {
            return "0 B";
        }
        if (size < SIZE_KB) {
            return size + " B";
        } else if (size < SIZE_MB) {
            return String.format("%.2f KB", size / (double) SIZE_KB);
        } else if (size < SIZE_GB) {
            return String.format("%.2f MB", size / (double) SIZE_MB);
        } else {
            return String.format("%.2f GB", size / (double) SIZE_GB);
        }
    }
}
