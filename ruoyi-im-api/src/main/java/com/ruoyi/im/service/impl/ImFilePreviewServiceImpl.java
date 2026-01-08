package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.service.ImFilePreviewService;
import com.ruoyi.im.vo.file.ImFilePreviewInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * 文件预览服务实现
 *
 * @author ruoyi
 */
@Service
public class ImFilePreviewServiceImpl implements ImFilePreviewService {

    @Autowired
    private ImFileAssetMapper fileAssetMapper;

    @Value("${im.file.upload.path:uploads/}")
    private String uploadPath;

    @Value("${im.file.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    @Value("${im.file.preview.thumbnail-dir:thumbnails/}")
    private String thumbnailDir;

    /** 支持预览的图片格式 */
    private static final Set<String> IMAGE_FORMATS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    ));

    /** 支持预览的文档格式 */
    private static final Set<String> DOCUMENT_FORMATS = new HashSet<>(Arrays.asList(
            "pdf", "txt", "md", "json", "xml"
    ));

    /** 支持预览的视频格式 */
    private static final Set<String> VIDEO_FORMATS = new HashSet<>(Arrays.asList(
            "mp4", "webm", "ogg"
    ));

    /** 支持预览的音频格式 */
    private static final Set<String> AUDIO_FORMATS = new HashSet<>(Arrays.asList(
            "mp3", "wav", "ogg", "flac", "aac"
    ));

    @Override
    public ImFilePreviewInfoVO getPreviewInfo(Long fileId, Long userId) {
        ImFileAsset fileAsset = fileAssetMapper.selectById(fileId);
        if (fileAsset == null) {
            throw new BusinessException("文件不存在");
        }

        ImFilePreviewInfoVO vo = new ImFilePreviewInfoVO();
        vo.setFileId(fileAsset.getId());
        vo.setFileName(fileAsset.getFileName());
        vo.setFileType(fileAsset.getFileType());
        vo.setFileExtension(fileAsset.getFileExtension());
        vo.setFileSize(fileAsset.getFileSize());

        String extension = fileAsset.getFileExtension() != null ? fileAsset.getFileExtension().toLowerCase() : "";

        // 确定预览类型
        if (IMAGE_FORMATS.contains(extension)) {
            vo.setPreviewType("image");
            vo.setCanPreview(true);
            vo.setSupportedFormats(Arrays.asList("original", "thumbnail"));

            // 获取图片尺寸
            if (fileAsset.getFilePath() != null) {
                getImageDimensions(fileAsset.getFilePath(), vo);
            }
        } else if (DOCUMENT_FORMATS.contains(extension)) {
            vo.setPreviewType("pdf");
            vo.setCanPreview(true);
            vo.setSupportedFormats(Arrays.asList("original"));
        } else if (VIDEO_FORMATS.contains(extension)) {
            vo.setPreviewType("video");
            vo.setCanPreview(true);
            vo.setSupportedFormats(Arrays.asList("original"));
        } else if (AUDIO_FORMATS.contains(extension)) {
            vo.setPreviewType("audio");
            vo.setCanPreview(true);
            vo.setSupportedFormats(Arrays.asList("original"));
        } else {
            // Office文档等暂不支持直接预览
            vo.setPreviewType("office");
            vo.setCanPreview(false);
            vo.setSupportedFormats(Collections.emptyList());
        }

        // 设置预览URL
        vo.setPreviewUrl(fileAsset.getFileUrl());
        vo.setDownloadUrl(fileAsset.getFileUrl());

        // 生成缩略图（仅图片）
        if ("image".equals(vo.getPreviewType())) {
            String thumbnailUrl = generateThumbnail(fileId, 200, 200, userId);
            vo.setThumbnailUrl(thumbnailUrl);
        }

        return vo;
    }

    @Override
    public String generateThumbnail(Long fileId, Integer width, Integer height, Long userId) {
        ImFileAsset fileAsset = fileAssetMapper.selectById(fileId);
        if (fileAsset == null) {
            throw new BusinessException("文件不存在");
        }

        if (fileAsset.getFilePath() == null) {
            throw new BusinessException("文件路径不存在");
        }

        try {
            // 读取原图
            File originalFile = new File(fileAsset.getFilePath());
            if (!originalFile.exists()) {
                throw new BusinessException("文件不存在");
            }

            BufferedImage originalImage = ImageIO.read(originalFile);
            if (originalImage == null) {
                throw new BusinessException("无法读取图片");
            }

            // 计算缩略图尺寸（保持宽高比）
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            int targetWidth = width != null ? width : 200;
            int targetHeight = height != null ? height : 200;

            double widthRatio = (double) targetWidth / originalWidth;
            double heightRatio = (double) targetHeight / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);

            int thumbnailWidth = (int) (originalWidth * ratio);
            int thumbnailHeight = (int) (originalHeight * ratio);

            // 生成缩略图
            BufferedImage thumbnailImage = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumbnailImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
            g.dispose();

            // 保存缩略图
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String thumbnailPathDir = uploadPath + thumbnailDir + dateDir + "/";
            Path thumbnailPath = Paths.get(thumbnailPathDir);
            if (!Files.exists(thumbnailPath)) {
                Files.createDirectories(thumbnailPath);
            }

            String thumbnailFileName = "thumb_" + fileAsset.getId() + "_" + width + "x" + height + ".jpg";
            String thumbnailFilePath = thumbnailPathDir + thumbnailFileName;
            File thumbnailFile = new File(thumbnailFilePath);
            ImageIO.write(thumbnailImage, "jpg", thumbnailFile);

            // 返回缩略图URL
            return urlPrefix + thumbnailDir + dateDir + "/" + thumbnailFileName;

        } catch (IOException e) {
            throw new BusinessException("生成缩略图失败: " + e.getMessage());
        }
    }

    @Override
    public String getPreviewUrl(Long fileId, String format, Long userId) {
        ImFileAsset fileAsset = fileAssetMapper.selectById(fileId);
        if (fileAsset == null) {
            throw new BusinessException("文件不存在");
        }

        switch (format) {
            case "thumbnail":
                return generateThumbnail(fileId, 200, 200, userId);
            case "image":
            case "pdf":
            case "html":
                // 对于PDF/Office等，需要转换后返回URL
                // 这里简化处理，直接返回原文件URL
                return fileAsset.getFileUrl();
            default:
                return fileAsset.getFileUrl();
        }
    }

    @Override
    public Boolean isPreviewSupported(String fileType) {
        if (fileType == null) {
            return false;
        }

        String extension = fileType.toLowerCase();
        return IMAGE_FORMATS.contains(extension) ||
                DOCUMENT_FORMATS.contains(extension) ||
                VIDEO_FORMATS.contains(extension) ||
                AUDIO_FORMATS.contains(extension);
    }

    /**
     * 获取图片尺寸
     *
     * @param filePath 文件路径
     * @param vo 预览信息VO
     */
    private void getImageDimensions(String filePath, ImFilePreviewInfoVO vo) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }

            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                vo.setWidth(image.getWidth());
                vo.setHeight(image.getHeight());
            }
        } catch (IOException e) {
            // 忽略错误
        }
    }
}
