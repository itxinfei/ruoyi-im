package com.ruoyi.im.service.impl;

import com.ruoyi.im.config.FileUploadConfig;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.service.ImFileService;
import com.ruoyi.im.util.FileUtils;
import com.ruoyi.im.vo.file.ImFileStatisticsVO;
import com.ruoyi.im.vo.file.ImFileVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.im.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Map;

/**
 * 文件服务实现
 *
 * @author ruoyi
 */
@Service
public class ImFileServiceImpl implements ImFileService {

    private static final Logger logger = LoggerFactory.getLogger(ImFileServiceImpl.class);

    private final ImFileAssetMapper imFileAssetMapper;
    private final FileUploadConfig fileUploadConfig;

    public ImFileServiceImpl(ImFileAssetMapper imFileAssetMapper,
                             FileUploadConfig fileUploadConfig) {
        this.imFileAssetMapper = imFileAssetMapper;
        this.fileUploadConfig = fileUploadConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            deleteFile(id, null);
        }
    }

    @Override
    public IPage<ImFileAsset> getAdminFileList(Integer pageNum, Integer pageSize, String fileName, 
                                              String fileType, Long uploaderId, LocalDateTime startTime, 
                                              LocalDateTime endTime) {
        LambdaQueryWrapper<ImFileAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImFileAsset::getStatus, "ACTIVE");

        if (fileName != null && !fileName.isEmpty()) {
            wrapper.like(ImFileAsset::getFileName, fileName);
        }
        if (fileType != null && !fileType.isEmpty()) {
            wrapper.eq(ImFileAsset::getFileType, fileType);
        }
        if (uploaderId != null) {
            wrapper.eq(ImFileAsset::getUploaderId, uploaderId);
        }
        if (startTime != null) {
            wrapper.ge(ImFileAsset::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(ImFileAsset::getCreateTime, endTime);
        }

        wrapper.orderByDesc(ImFileAsset::getCreateTime);
        return imFileAssetMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<Map<String, Object>> getFileStatisticsByType() {
        QueryWrapper<ImFileAsset> wrapper = new QueryWrapper<>();
        wrapper.select("file_type", "COUNT(*) as count", "SUM(file_size) as totalSize");
        wrapper.eq("status", "ACTIVE");
        wrapper.groupBy("file_type");
        return imFileAssetMapper.selectMaps(wrapper);
    }

    @Override
    public List<Map<String, Object>> getFileStatisticsByUploader() {
        QueryWrapper<ImFileAsset> wrapper = new QueryWrapper<>();
        wrapper.select("uploader_id", "COUNT(*) as count", "SUM(file_size) as totalSize");
        wrapper.eq("status", "ACTIVE");
        wrapper.groupBy("uploader_id");
        wrapper.orderByDesc("COUNT(*)");
        wrapper.last("LIMIT 10");
        return imFileAssetMapper.selectMaps(wrapper);
    }

    @Override
    public ImFileVO uploadFile(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = FileUtils.getFileExtension(originalFilename);
        String fileType = FileUtils.getFileType(fileExtension);

        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + fileName;
        String filePath = fileUploadConfig.getAbsoluteUploadPath() + relativePath;

        File targetFile = new File(filePath);
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }

        ImFileAsset fileAsset = new ImFileAsset();
        fileAsset.setFileName(fileName);
        fileAsset.setFilePath(relativePath);
        fileAsset.setFileSize(file.getSize());
        fileAsset.setFileType(fileType);
        fileAsset.setFileExt(fileExtension);
        fileAsset.setUploaderId(userId);
        fileAsset.setCreateTime(LocalDateTime.now());
        fileAsset.setDownloadCount(0);
        fileAsset.setStatus("ACTIVE");

        imFileAssetMapper.insert(fileAsset);

        return convertToVO(fileAsset);
    }

    @Override
    public List<ImFileVO> uploadFiles(List<MultipartFile> files, Long userId) {
        List<ImFileVO> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(uploadFile(file, userId));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void downloadFile(Long fileId, Long userId) {
        ImFileAsset fileAsset = imFileAssetMapper.selectById(fileId);
        if (fileAsset == null) {
            throw new BusinessException("文件不存在");
        }

        fileAsset.setDownloadCount(fileAsset.getDownloadCount() + 1);
        imFileAssetMapper.updateById(fileAsset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId, Long userId) {
        ImFileAsset fileAsset = imFileAssetMapper.selectById(fileId);
        if (fileAsset == null) {
            throw new BusinessException("文件不存在");
        }

        fileAsset.setStatus("DELETED");
        imFileAssetMapper.updateById(fileAsset);
    }

    @Override
    public List<ImFileVO> getFileList(Long userId, String fileType) {
        List<ImFileAsset> list = imFileAssetMapper.selectList(null);
        List<ImFileVO> result = new ArrayList<>();
        for (ImFileAsset asset : list) {
            if ("ACTIVE".equals(asset.getStatus())) {
                if (fileType == null || fileType.isEmpty() || fileType.equals(asset.getFileType())) {
                    result.add(convertToVO(asset));
                }
            }
        }
        return result;
    }

    @Override
    public ImFileVO getFileById(Long fileId) {
        ImFileAsset fileAsset = imFileAssetMapper.selectById(fileId);
        if (fileAsset == null) {
            throw new BusinessException("文件不存在");
        }
        return convertToVO(fileAsset);
    }

    @Override
    public ImFileStatisticsVO getStorageStatistics() {
        ImFileStatisticsVO vo = new ImFileStatisticsVO();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30);

        Long totalFiles = imFileAssetMapper.countTotalFiles(startDate, today);
        Long todayUploads = imFileAssetMapper.countTodayUploads(today);
        Long totalStorage = imFileAssetMapper.countTotalStorage();
        Long totalDownloads = imFileAssetMapper.countTotalDownloads(startDate, today);

        vo.setTotalFiles(totalFiles.intValue());
        vo.setTodayUploads(todayUploads.intValue());
        vo.setTotalStorageSize(totalStorage);
        vo.setTotalDownloads(totalDownloads.intValue());

        return vo;
    }

    private ImFileVO convertToVO(ImFileAsset asset) {
        ImFileVO vo = new ImFileVO();
        BeanConvertUtil.copyProperties(asset, vo);
        vo.setFileId(asset.getId());
        vo.setFileExtension(asset.getFileExt());
        vo.setFileUrl(fileUploadConfig.buildFileUrl(asset.getFilePath()));
        vo.setUploadTime(asset.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        vo.setDeleted("DELETED".equals(asset.getStatus()));
        return vo;
    }
}
