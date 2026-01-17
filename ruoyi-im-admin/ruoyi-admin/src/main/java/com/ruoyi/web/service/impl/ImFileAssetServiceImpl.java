package com.ruoyi.web.service.impl;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.web.domain.ImFileAsset;
import com.ruoyi.web.mapper.ImFileAssetMapper;
import com.ruoyi.web.service.ImFileAssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM文件资源Service实现（Admin模块专用）
 *
 * @author ruoyi
 * @date 2025-01-17
 */
@Service
public class ImFileAssetServiceImpl implements ImFileAssetService {

    private static final Logger logger = LoggerFactory.getLogger(ImFileAssetServiceImpl.class);

    /** 文件大小限制：100MB */
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    @Autowired
    private ImFileAssetMapper fileAssetMapper;

    @Autowired
    private RuoYiConfig ruoYiConfig;

    @Override
    public List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset) {
        try {
            List<ImFileAsset> list = fileAssetMapper.selectImFileAssetList(imFileAsset);
            if (list == null) {
                return new ArrayList<>();
            }
            return list;
        } catch (Exception e) {
            logger.error("查询文件列表异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public ImFileAsset selectImFileAssetById(Long id) {
        if (id == null || id <= 0) {
            logger.warn("查询文件详情失败：文件ID为空");
            return null;
        }
        try {
            return fileAssetMapper.selectImFileAssetById(id);
        } catch (Exception e) {
            logger.error("查询文件详情异常，文件ID：{}", id, e);
            return null;
        }
    }

    @Override
    public int insertImFileAsset(ImFileAsset imFileAsset) {
        if (imFileAsset == null) {
            logger.warn("新增文件失败：文件对象为空");
            return 0;
        }
        try {
            // 设置默认值
            if (StringUtils.isEmpty(imFileAsset.getStatus())) {
                imFileAsset.setStatus(ImFileAsset.STATUS_ACTIVE);
            }
            if (imFileAsset.getDownloadCount() == null) {
                imFileAsset.setDownloadCount(0);
            }
            return fileAssetMapper.insertImFileAsset(imFileAsset);
        } catch (Exception e) {
            logger.error("新增文件异常", e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImFileAsset uploadFile(MultipartFile file, Long uploaderId) throws Exception {
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 获取文件扩展名
        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex + 1);
        }

        // 上传文件到服务器
        String uploadPath;
        try {
            uploadPath = FileUploadUtils.upload(ruoYiConfig.getProfile(), file);
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw new Exception("文件上传失败: " + e.getMessage());
        }

        // 创建文件记录
        ImFileAsset fileAsset = new ImFileAsset();
        fileAsset.setFileName(originalFilename);
        fileAsset.setOriginalName(originalFilename);
        fileAsset.setFileType(getFileTypeByExtension(fileExtension));
        fileAsset.setFileSize(file.getSize());
        fileAsset.setFileExtension(fileExtension);
        fileAsset.setFilePath(uploadPath);
        fileAsset.setFileUrl("/profile/" + uploadPath);
        fileAsset.setMimeType(file.getContentType());
        fileAsset.setUploaderId(uploaderId != null ? uploaderId : 1L);
        fileAsset.setDownloadCount(0);
        fileAsset.setStatus(ImFileAsset.STATUS_ACTIVE);

        try {
            fileAssetMapper.insertImFileAsset(fileAsset);
            logger.info("文件上传成功，文件ID：{}，文件名：{}", fileAsset.getId(), originalFilename);
            return fileAsset;
        } catch (Exception e) {
            logger.error("保存文件记录失败", e);
            throw new Exception("保存文件记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateImFileAsset(ImFileAsset imFileAsset) {
        if (imFileAsset == null || imFileAsset.getId() == null) {
            logger.warn("修改文件失败：文件对象或ID为空");
            return 0;
        }

        try {
            // 检查文件是否存在
            ImFileAsset existingFile = fileAssetMapper.selectImFileAssetById(imFileAsset.getId());
            if (existingFile == null) {
                logger.warn("修改文件失败：文件不存在，ID：{}", imFileAsset.getId());
                return 0;
            }

            return fileAssetMapper.updateImFileAsset(imFileAsset);
        } catch (Exception e) {
            logger.error("修改文件异常", e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImFileAssetById(Long id) {
        if (id == null || id <= 0) {
            logger.warn("删除文件失败：文件ID为空");
            return 0;
        }

        try {
            return fileAssetMapper.deleteImFileAssetById(id);
        } catch (Exception e) {
            logger.error("删除文件异常", e);
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImFileAssetByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            logger.warn("批量删除文件失败：文件ID数组为空");
            return 0;
        }

        try {
            return fileAssetMapper.deleteImFileAssetByIds(ids);
        } catch (Exception e) {
            logger.error("批量删除文件异常", e);
            return 0;
        }
    }

    @Override
    public int countTotalFiles(ImFileAsset imFileAsset) {
        return fileAssetMapper.countTotalFiles(imFileAsset);
    }

    @Override
    public int countTodayUploads(ImFileAsset imFileAsset) {
        return fileAssetMapper.countTodayUploads(imFileAsset);
    }

    @Override
    public Long countTotalStorage() {
        return fileAssetMapper.countTotalStorage();
    }

    @Override
    public int cleanInvalidFiles() {
        return fileAssetMapper.cleanInvalidFiles();
    }

    @Override
    public Map<String, Object> getFileStatistics() {
        try {
            Map<String, Object> result = fileAssetMapper.getFileStatistics();
            if (result == null) {
                result = new HashMap<>();
                result.put("totalCount", 0);
                result.put("imageCount", 0);
                result.put("videoCount", 0);
                result.put("docCount", 0);
                result.put("audioCount", 0);
                result.put("otherCount", 0);
                result.put("totalStorage", 0L);
                result.put("todayUploads", 0);
            }
            return result;
        } catch (Exception e) {
            logger.error("获取文件统计异常", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("totalCount", 0);
            errorResult.put("imageCount", 0);
            errorResult.put("videoCount", 0);
            errorResult.put("docCount", 0);
            errorResult.put("audioCount", 0);
            errorResult.put("otherCount", 0);
            errorResult.put("totalStorage", 0L);
            errorResult.put("todayUploads", 0);
            return errorResult;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int incrementDownloadCount(Long id) {
        if (id == null || id <= 0) {
            logger.warn("增加下载次数失败：文件ID为空");
            return 0;
        }

        try {
            return fileAssetMapper.incrementDownloadCount(id);
        } catch (Exception e) {
            logger.error("增加下载次数异常", e);
            return 0;
        }
    }

    @Override
    public String getFileTypeByExtension(String extension) {
        if (StringUtils.isEmpty(extension)) {
            return ImFileAsset.TYPE_OTHER;
        }

        extension = extension.toLowerCase();

        // 图片类型
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp|svg")) {
            return ImFileAsset.TYPE_IMAGE;
        }
        // 视频类型
        else if (extension.matches("mp4|avi|mov|wmv|flv|mkv|rmvb|3gp")) {
            return ImFileAsset.TYPE_VIDEO;
        }
        // 音频类型
        else if (extension.matches("mp3|wav|flac|aac|ogg|wma|mid")) {
            return ImFileAsset.TYPE_AUDIO;
        }
        // 文档类型
        else if (extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf|odt")) {
            return ImFileAsset.TYPE_DOCUMENT;
        }
        // 其他类型
        else {
            return ImFileAsset.TYPE_OTHER;
        }
    }
}
