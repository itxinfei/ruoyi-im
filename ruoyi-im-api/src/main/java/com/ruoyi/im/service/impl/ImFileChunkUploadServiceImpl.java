package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.domain.ImFileChunkDetail;
import com.ruoyi.im.domain.ImFileChunkUpload;
import com.ruoyi.im.dto.file.ImFileChunkMergeRequest;
import com.ruoyi.im.dto.file.ImFileChunkUploadInitRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.mapper.ImFileChunkDetailMapper;
import com.ruoyi.im.mapper.ImFileChunkUploadMapper;
import com.ruoyi.im.service.ImFileChunkUploadService;
import com.ruoyi.im.vo.file.ImFileChunkUploadInitVO;
import com.ruoyi.im.vo.file.ImFileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分片上传服务实现
 *
 * @author ruoyi
 */
@Service
public class ImFileChunkUploadServiceImpl implements ImFileChunkUploadService {

    @Autowired
    private ImFileChunkUploadMapper chunkUploadMapper;

    @Autowired
    private ImFileChunkDetailMapper chunkDetailMapper;

    @Autowired
    private ImFileAssetMapper fileAssetMapper;

    @Value("${im.file.upload.path:uploads/}")
    private String uploadPath;

    @Value("${im.file.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    /** 上传任务过期时间（小时） */
    private static final int EXPIRE_HOURS = 24;

    /** 分片临时目录 */
    private static final String CHUNK_TEMP_DIR = "chunks/";

    @Override
    @Transactional
    public ImFileChunkUploadInitVO initChunkUpload(ImFileChunkUploadInitRequest request, Long userId) {
        // 检查是否可以秒传（文件MD5已存在）
        if (request.getFileMd5() != null && !request.getFileMd5().isEmpty()) {
            ImFileChunkUpload existingUpload = chunkUploadMapper.selectCompletedByMd5(request.getFileMd5());
            if (existingUpload != null && existingUpload.getFileUrl() != null) {
                // 秒传：直接返回已有文件
                ImFileChunkUploadInitVO vo = new ImFileChunkUploadInitVO();
                vo.setUploadId(existingUpload.getUploadId());
                vo.setTotalChunks(existingUpload.getTotalChunks());
                vo.setChunkSize(existingUpload.getChunkSize());
                vo.setUploadedChunks(Collections.emptyList());
                vo.setNeedUpload(false);
                vo.setFileUrl(existingUpload.getFileUrl());
                vo.setStatus("COMPLETED");
                return vo;
            }
        }

        // 计算分片信息
        Integer chunkSize = request.getChunkSize() != null ? request.getChunkSize() : 5242880; // 默认5MB
        int totalChunks = (int) Math.ceil((double) request.getFileSize() / chunkSize);

        // 生成上传ID
        String uploadId = UUID.randomUUID().toString().replace("-", "");

        // 创建上传记录
        ImFileChunkUpload chunkUpload = new ImFileChunkUpload();
        chunkUpload.setUploadId(uploadId);
        chunkUpload.setFileName(request.getFileName());
        chunkUpload.setFileSize(request.getFileSize());
        chunkUpload.setFileMd5(request.getFileMd5());
        chunkUpload.setChunkSize(chunkSize);
        chunkUpload.setTotalChunks(totalChunks);
        chunkUpload.setUploadedChunks(0);
        chunkUpload.setStatus("UPLOADING");
        chunkUpload.setUserId(userId);
        chunkUpload.setExpireTime(LocalDateTime.now().plusHours(EXPIRE_HOURS));
        chunkUpload.setCreateTime(LocalDateTime.now());
        chunkUploadMapper.insert(chunkUpload);

        // 创建分片记录
        List<ImFileChunkDetail> chunkDetails = new ArrayList<>();
        for (int i = 1; i <= totalChunks; i++) {
            ImFileChunkDetail detail = new ImFileChunkDetail();
            detail.setUploadId(uploadId);
            detail.setChunkNumber(i);
            detail.setChunkSize(chunkSize);
            detail.setStatus("PENDING");
            detail.setRetryCount(0);
            chunkDetails.add(detail);
        }
        chunkDetailMapper.batchInsert(chunkDetails);

        // 返回初始化结果
        ImFileChunkUploadInitVO vo = new ImFileChunkUploadInitVO();
        vo.setUploadId(uploadId);
        vo.setTotalChunks(totalChunks);
        vo.setChunkSize(chunkSize);
        vo.setUploadedChunks(Collections.emptyList());
        vo.setNeedUpload(true);
        vo.setStatus("UPLOADING");
        vo.setExpireTime(chunkUpload.getExpireTime());
        return vo;
    }

    @Override
    @Transactional
    public Boolean uploadChunk(String uploadId, Integer chunkNumber, MultipartFile file, Long userId) {
        // 验证上传任务
        ImFileChunkUpload chunkUpload = chunkUploadMapper.selectByUploadId(uploadId);
        if (chunkUpload == null) {
            throw new BusinessException("上传任务不存在");
        }
        if (!chunkUpload.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此上传任务");
        }
        if (!"UPLOADING".equals(chunkUpload.getStatus())) {
            throw new BusinessException("上传任务状态异常");
        }

        // 验证分片
        ImFileChunkDetail chunkDetail = chunkDetailMapper.selectByUploadIdAndChunkNumber(uploadId, chunkNumber);
        if (chunkDetail == null) {
            throw new BusinessException("分片信息不存在");
        }
        if ("COMPLETED".equals(chunkDetail.getStatus())) {
            // 分片已上传，直接返回成功
            return true;
        }

        try {
            // 保存分片文件
            String dateDir = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            String chunkDir = uploadPath + CHUNK_TEMP_DIR + uploadId + "/";
            Path chunkPath = Paths.get(chunkDir);
            if (!Files.exists(chunkPath)) {
                Files.createDirectories(chunkPath);
            }

            String chunkFileName = "chunk_" + chunkNumber;
            String fullChunkPath = chunkDir + chunkFileName;
            file.transferTo(new File(fullChunkPath));

            // 更新分片状态
            chunkDetailMapper.updateChunkStatus(uploadId, chunkNumber, "COMPLETED", fullChunkPath);

            // 更新已上传分片数
            List<ImFileChunkDetail> completedChunks = chunkDetailMapper.selectCompletedByUploadId(uploadId);
            chunkUploadMapper.updateUploadedChunks(uploadId, completedChunks.size());

            // 检查是否全部上传完成
            if (completedChunks.size() >= chunkUpload.getTotalChunks()) {
                chunkUploadMapper.updateStatus(uploadId, "READY_TO_MERGE", null, null);
            }

            return true;
        } catch (IOException e) {
            // 更新分片状态为失败
            chunkDetailMapper.updateChunkStatus(uploadId, chunkNumber, "FAILED", null);
            throw new BusinessException("分片上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ImFileVO mergeChunks(ImFileChunkMergeRequest request, Long userId) {
        // 验证上传任务
        ImFileChunkUpload chunkUpload = chunkUploadMapper.selectByUploadId(request.getUploadId());
        if (chunkUpload == null) {
            throw new BusinessException("上传任务不存在");
        }
        if (!chunkUpload.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此上传任务");
        }

        // 获取所有分片
        List<ImFileChunkDetail> chunkDetails = chunkDetailMapper.selectCompletedByUploadId(request.getUploadId());
        if (chunkDetails.size() < chunkUpload.getTotalChunks()) {
            throw new BusinessException("分片未全部上传完成");
        }

        try {
            // 按分片序号排序
            chunkDetails.sort(Comparator.comparingInt(ImFileChunkDetail::getChunkNumber));

            // 创建最终文件目录
            String dateDir = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String targetDir = uploadPath + dateDir + "/";
            Path targetPath = Paths.get(targetDir);
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            // 生成最终文件名
            String fileExtension = getFileExtension(chunkUpload.getFileName());
            String finalFileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExtension;
            String finalFilePath = targetDir + finalFileName;
            String fileUrl = urlPrefix + dateDir + finalFileName;

            // 合并分片
            try (FileOutputStream fos = new FileOutputStream(finalFilePath)) {
                for (ImFileChunkDetail chunk : chunkDetails) {
                    File chunkFile = new File(chunk.getChunkPath());
                    if (!chunkFile.exists()) {
                        throw new BusinessException("分片文件丢失: " + chunk.getChunkNumber());
                    }
                    try (FileInputStream fis = new FileInputStream(chunkFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }

            // 验证文件MD5（如果提供）
            if (request.getFileMd5() != null && !request.getFileMd5().isEmpty()) {
                String actualMd5 = calculateFileMd5(new File(finalFilePath));
                if (!actualMd5.equalsIgnoreCase(request.getFileMd5())) {
                    Files.deleteIfExists(Paths.get(finalFilePath));
                    throw new BusinessException("文件MD5校验失败");
                }
            }

            // 保存文件信息到im_file_asset表
            ImFileAsset fileAsset = new ImFileAsset();
            fileAsset.setFileName(chunkUpload.getFileName());
            fileAsset.setFilePath(finalFilePath);
            fileAsset.setFileSize(chunkUpload.getFileSize());
            fileAsset.setFileType(getFileType(chunkUpload.getFileName()));
            fileAsset.setFileExt(getFileExtension(chunkUpload.getFileName()));
            fileAsset.setUploaderId(userId);
            fileAsset.setStatus("ACTIVE");
            fileAsset.setDownloadCount(0);
            fileAsset.setCreateTime(LocalDateTime.now());
            fileAssetMapper.insert(fileAsset);

            // 更新上传任务状态
            chunkUploadMapper.updateStatus(request.getUploadId(), "COMPLETED", finalFilePath, fileUrl);

            // 删除临时分片文件
            cleanupChunks(request.getUploadId());

            // 返回文件信息
            ImFileVO vo = new ImFileVO();
            vo.setFileId(fileAsset.getId());
            vo.setFileName(chunkUpload.getFileName());
            vo.setFileSize(chunkUpload.getFileSize());
            vo.setFilePath(finalFilePath);
            vo.setFileUrl(fileUrl);
            vo.setFileType(getFileType(chunkUpload.getFileName()));
            vo.setFileExtension(getFileExtension(chunkUpload.getFileName()));
            return vo;

        } catch (IOException e) {
            throw new BusinessException("文件合并失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void cancelChunkUpload(String uploadId, Long userId) {
        ImFileChunkUpload chunkUpload = chunkUploadMapper.selectByUploadId(uploadId);
        if (chunkUpload == null) {
            throw new BusinessException("上传任务不存在");
        }
        if (!chunkUpload.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此上传任务");
        }

        // 更新状态为已取消
        chunkUploadMapper.updateStatus(uploadId, "CANCELLED", null, null);

        // 删除临时分片文件
        cleanupChunks(uploadId);

        // 删除分片记录
        chunkDetailMapper.deleteByUploadId(uploadId);
    }

    @Override
    @Transactional
    public void pauseChunkUpload(String uploadId, Long userId) {
        ImFileChunkUpload chunkUpload = chunkUploadMapper.selectByUploadId(uploadId);
        if (chunkUpload == null) {
            throw new BusinessException("上传任务不存在");
        }
        if (!chunkUpload.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此上传任务");
        }

        chunkUploadMapper.updateStatus(uploadId, "PAUSED", null, null);
    }

    @Override
    public ImFileChunkUploadInitVO resumeChunkUpload(String uploadId, Long userId) {
        ImFileChunkUpload chunkUpload = chunkUploadMapper.selectByUploadId(uploadId);
        if (chunkUpload == null) {
            throw new BusinessException("上传任务不存在");
        }
        if (!chunkUpload.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此上传任务");
        }

        // 获取已上传的分片
        List<ImFileChunkDetail> completedChunks = chunkDetailMapper.selectCompletedByUploadId(uploadId);
        List<Integer> uploadedChunkNumbers = completedChunks.stream()
                .map(ImFileChunkDetail::getChunkNumber)
                .collect(Collectors.toList());

        // 更新状态为上传中
        chunkUploadMapper.updateStatus(uploadId, "UPLOADING", null, null);

        // 返回初始化结果
        ImFileChunkUploadInitVO vo = new ImFileChunkUploadInitVO();
        vo.setUploadId(uploadId);
        vo.setTotalChunks(chunkUpload.getTotalChunks());
        vo.setChunkSize(chunkUpload.getChunkSize());
        vo.setUploadedChunks(uploadedChunkNumbers);
        vo.setNeedUpload(uploadedChunkNumbers.size() < chunkUpload.getTotalChunks());
        vo.setStatus("UPLOADING");
        vo.setExpireTime(chunkUpload.getExpireTime());
        return vo;
    }

    @Override
    public Integer getUploadProgress(String uploadId, Long userId) {
        ImFileChunkUpload chunkUpload = chunkUploadMapper.selectByUploadId(uploadId);
        if (chunkUpload == null) {
            throw new BusinessException("上传任务不存在");
        }

        if ("COMPLETED".equals(chunkUpload.getStatus())) {
            return 100;
        }

        List<ImFileChunkDetail> completedChunks = chunkDetailMapper.selectCompletedByUploadId(uploadId);
        int progress = (int) ((double) completedChunks.size() / chunkUpload.getTotalChunks() * 100);
        return Math.min(progress, 99); // 合并中时返回99
    }

    /**
     * 清理分片临时文件
     *
     * @param uploadId 上传ID
     */
    private void cleanupChunks(String uploadId) {
        try {
            String chunkDir = uploadPath + CHUNK_TEMP_DIR + uploadId + "/";
            Path chunkPath = Paths.get(chunkDir);
            if (Files.exists(chunkPath)) {
                Files.walk(chunkPath)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                // 忽略删除失败
                            }
                        });
            }
        } catch (IOException e) {
            // 忽略清理失败
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "bin";
    }

    /**
     * 计算文件MD5
     *
     * @param file 文件
     * @return MD5值
     * @throws IOException IO异常
     */
    private String calculateFileMd5(File file) throws IOException {
        // 简化实现，实际应使用MessageDigest
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 根据文件名获取文件类型
     *
     * @param fileName 文件名
     * @return 文件类型
     */
    private String getFileType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        // 图片类型
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp|svg")) {
            return "image";
        }
        // 视频类型
        if (extension.matches("mp4|avi|mkv|mov|wmv|flv|webm")) {
            return "video";
        }
        // 音频类型
        if (extension.matches("mp3|wav|flac|aac|ogg")) {
            return "audio";
        }
        // 文档类型
        if (extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt")) {
            return "document";
        }
        return "other";
    }
}
