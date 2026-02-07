package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.constants.StatusConstants;
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

import java.util.stream.Collectors;
import com.ruoyi.im.dto.group.ImGroupFileQueryRequest;
import com.ruoyi.im.dto.group.ImGroupFileUpdateRequest;
import com.ruoyi.im.dto.group.ImGroupFileUploadRequest;
import com.ruoyi.im.vo.group.ImGroupFileStatisticsVO;
import com.ruoyi.im.vo.group.ImGroupFileVO;
import com.ruoyi.im.util.BeanConvertUtil;
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
            throw new BusinessException("您不是该群组成员");
        }

        // 验证文件资产是否存在
        ImFileAsset fileAsset = fileAssetMapper.selectById(request.getFileId());
        if (fileAsset == null || "DELETED".equals(fileAsset.getStatus())) {
            throw new BusinessException("文件不存在");
        }

        // 验证文件是否属于当前用户
        if (!fileAsset.getUploaderId().equals(userId)) {
            throw new BusinessException("只能上传自己的文件");
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
        groupFile.setStatus(1);

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
                throw new BusinessException("您不是该群组成员");
            }
        }

        // 分页查询
        Page<ImGroupFile> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ImGroupFile> groupFilePage = groupFileMapper.selectFilePage(page, request);

        // 转换为VO
        Page<ImGroupFileVO> voPage = new Page<>(groupFilePage.getCurrent(), groupFilePage.getSize(), groupFilePage.getTotal());
        List<ImGroupFileVO> voList = groupFilePage.getRecords().stream().map(gf -> {
            ImGroupFileVO vo = new ImGroupFileVO();
            BeanConvertUtil.copyProperties(gf, vo);
            vo.setFileSizeFormat(formatFileSize(gf.getFileSize()));

            // 判断权限
            ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(gf.getGroupId(), userId);
            if (member != null) {
                String role = member.getRole();
                boolean isOwner = StatusConstants.GroupMemberRole.OWNER.equals(role);
                boolean isAdmin = StatusConstants.GroupMemberRole.ADMIN.equals(role);
                boolean isUploader = gf.getUploaderId().equals(userId);

                vo.setCanDelete(isOwner || isAdmin || isUploader);
                vo.setCanDownload("ALL".equals(gf.getPermission()) || isOwner || isAdmin || isUploader);
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
            throw new BusinessException("您不是该群组成员");
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
            throw new BusinessException("您不是该群组成员");
        }

        return groupFileMapper.selectCategoriesByGroupId(groupId);
    }

    @Override
    @Transactional
    public void updateFile(Long groupFileId, ImGroupFileUpdateRequest request, Long userId) {
        ImGroupFile groupFile = groupFileMapper.selectById(groupFileId);
        if (groupFile == null || groupFile.getStatus() == 0) {
            throw new BusinessException("文件不存在");
        }

        // 验证权限：只有上传者、群主、管理员可以编辑
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupFile.getGroupId(), userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String role = member.getRole();
        boolean isOwner = "OWNER".equals(role);
        boolean isAdmin = "ADMIN".equals(role);
        boolean isUploader = groupFile.getUploaderId().equals(userId);

        if (!isOwner && !isAdmin && !isUploader) {
            throw new BusinessException("只有群主、管理员或上传者可以编辑文件信息");
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
        if (groupFile == null || groupFile.getStatus() == 0) {
            throw new BusinessException("文件不存在");
        }

        // 验证权限：只有上传者、群主、管理员可以删除
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupFile.getGroupId(), userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String role = member.getRole();
        boolean isOwner = "OWNER".equals(role);
        boolean isAdmin = "ADMIN".equals(role);
        boolean isUploader = groupFile.getUploaderId().equals(userId);

        if (!isOwner && !isAdmin && !isUploader) {
            throw new BusinessException("只有群主、管理员或上传者可以删除文件");
        }

        // 软删除
        groupFile.setStatus(0);
        groupFileMapper.updateById(groupFile);
    }

    @Override
    @Transactional
    public String downloadFile(Long groupFileId, Long userId) {
        ImGroupFile groupFile = groupFileMapper.selectById(groupFileId);
        if (groupFile == null || groupFile.getStatus() == 0) {
            throw new BusinessException("文件不存在");
        }

        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupFile.getGroupId(), userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        // 验证下载权限
        String role = member.getRole();
        boolean isOwner = "OWNER".equals(role);
        boolean isAdmin = "ADMIN".equals(role);
        boolean isUploader = groupFile.getUploaderId().equals(userId);

        if (!"ALL".equals(groupFile.getPermission()) && !isOwner && !isAdmin && !isUploader) {
            throw new BusinessException("您没有权限下载此文件");
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
        if (groupFile == null || groupFile.getStatus() == 0) {
            throw new BusinessException("文件不存在");
        }

        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupFile.getGroupId(), userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        // 任何成员都可以移动文件
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
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
