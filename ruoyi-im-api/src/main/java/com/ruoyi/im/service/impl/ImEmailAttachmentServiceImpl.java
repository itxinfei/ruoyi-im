package com.ruoyi.im.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.config.FileUploadConfig;
import com.ruoyi.im.domain.ImEmail;
import com.ruoyi.im.domain.ImEmailAttachment;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImEmailAttachmentMapper;
import com.ruoyi.im.mapper.ImEmailMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImEmailAttachmentService;
import com.ruoyi.im.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 邮件附件服务实现
 *
 * @author ruoyi
 */
@Service
public class ImEmailAttachmentServiceImpl implements ImEmailAttachmentService {

    private static final Logger log = LoggerFactory.getLogger(ImEmailAttachmentServiceImpl.class);

    @Autowired
    private ImEmailAttachmentMapper attachmentMapper;

    @Autowired
    private ImEmailMapper emailMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Resource
    private FileUploadConfig fileUploadConfig;

    /** 邮件附件存储子目录 */
    private static final String EMAIL_SUBDIR = "email";

    /** 最大附件大小 10MB */
    private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024;

    /** 允许的文件类型 */
    private static final List<String> ALLOWED_EXTENSIONS = new ArrayList<>();

    static {
        // 文档类
        ALLOWED_EXTENSIONS.add("doc");
        ALLOWED_EXTENSIONS.add("docx");
        ALLOWED_EXTENSIONS.add("xls");
        ALLOWED_EXTENSIONS.add("xlsx");
        ALLOWED_EXTENSIONS.add("ppt");
        ALLOWED_EXTENSIONS.add("pptx");
        ALLOWED_EXTENSIONS.add("pdf");
        ALLOWED_EXTENSIONS.add("txt");
        ALLOWED_EXTENSIONS.add("csv");
        // 图片类
        ALLOWED_EXTENSIONS.add("jpg");
        ALLOWED_EXTENSIONS.add("jpeg");
        ALLOWED_EXTENSIONS.add("png");
        ALLOWED_EXTENSIONS.add("gif");
        ALLOWED_EXTENSIONS.add("bmp");
        // 压缩类
        ALLOWED_EXTENSIONS.add("zip");
        ALLOWED_EXTENSIONS.add("rar");
        ALLOWED_EXTENSIONS.add("7z");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImEmailAttachment uploadAttachment(MultipartFile file, Long uploaderId) {
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 文件大小校验
        if (file.getSize() > MAX_ATTACHMENT_SIZE) {
            throw new BusinessException("文件大小不能超过 10MB");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = FileUtils.getFileExtension(originalFilename).toLowerCase();

        // 文件类型校验
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new BusinessException("不支持的文件类型");
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;

        // 构建存储路径：email/年/月/日/文件名
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = EMAIL_SUBDIR + "/" + datePath + "/" + fileName;
        String absolutePath = fileUploadConfig.getAbsoluteUploadPath() + relativePath;

        // 确保目录存在
        File targetFile = new File(absolutePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 保存文件
        try {
            file.transferTo(targetFile);
        } catch (Exception e) {
            log.error("附件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("附件上传失败: " + e.getMessage());
        }

        // 创建附件记录（此时 emailId 为空，待发送邮件后关联）
        ImEmailAttachment attachment = new ImEmailAttachment();
        attachment.setFileName(originalFilename);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(file.getContentType());
        attachment.setFilePath(relativePath);
        attachment.setFileUrl(fileUploadConfig.buildFileUrl(relativePath));
        attachment.setUploadTime(LocalDateTime.now());
        attachment.setUploaderId(uploaderId);

        attachmentMapper.insertAttachment(attachment);

        log.info("附件上传成功: attachmentId={}, fileName={}, size={}",
                attachment.getId(), originalFilename, file.getSize());

        return attachment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttachments(Long emailId, List<ImEmailAttachment> attachments) {
        if (emailId == null || attachments == null || attachments.isEmpty()) {
            return;
        }

        // 更新附件的 emailId
        for (ImEmailAttachment attachment : attachments) {
            attachment.setEmailId(emailId);
            attachmentMapper.insertAttachment(attachment);
        }

        // 更新邮件的附件数量
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email != null) {
            email.setAttachmentCount(attachments.size());
            emailMapper.updateEmail(email);
        }

        log.info("保存邮件附件成功: emailId={}, count={}", emailId, attachments.size());
    }

    @Override
    public List<ImEmailAttachment> getEmailAttachments(Long emailId) {
        if (emailId == null) {
            return new ArrayList<>();
        }
        return attachmentMapper.selectAttachmentsByEmailId(emailId);
    }

    @Override
    public ImEmailAttachment getAttachmentById(Long attachmentId) {
        if (attachmentId == null) {
            throw new BusinessException("附件ID不能为空");
        }
        ImEmailAttachment attachment = attachmentMapper.selectAttachmentById(attachmentId);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }
        return attachment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long attachmentId, Long userId) {
        ImEmailAttachment attachment = getAttachmentById(attachmentId);

        // 验证权限：只有上传者可以删除
        if (!attachment.getUploaderId().equals(userId)) {
            throw new BusinessException("无权限删除此附件");
        }

        // 删除物理文件
        String absolutePath = fileUploadConfig.getAbsoluteUploadPath() + attachment.getFilePath();
        File file = new File(absolutePath);
        if (file.exists()) {
            file.delete();
        }

        // 删除数据库记录
        attachmentMapper.deleteAttachmentById(attachmentId);

        // 如果附件已关联邮件，更新邮件的附件数量
        if (attachment.getEmailId() != null) {
            ImEmail email = emailMapper.selectEmailById(attachment.getEmailId());
            if (email != null && email.getAttachmentCount() != null && email.getAttachmentCount() > 0) {
                email.setAttachmentCount(email.getAttachmentCount() - 1);
                emailMapper.updateEmail(email);
            }
        }

        log.info("删除附件成功: attachmentId={}, userId={}", attachmentId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachmentsByEmailId(Long emailId) {
        List<ImEmailAttachment> attachments = attachmentMapper.selectAttachmentsByEmailId(emailId);

        for (ImEmailAttachment attachment : attachments) {
            // 删除物理文件
            String absolutePath = fileUploadConfig.getAbsoluteUploadPath() + attachment.getFilePath();
            File file = new File(absolutePath);
            if (file.exists()) {
                file.delete();
            }
        }

        // 删除数据库记录
        attachmentMapper.deleteAttachmentsByEmailId(emailId);

        log.info("删除邮件附件成功: emailId={}, count={}", emailId, attachments.size());
    }
}
