package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImEmailAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 邮件附件服务接口
 *
 * @author ruoyi
 */
public interface ImEmailAttachmentService {

    /**
     * 上传附件
     *
     * @param file 文件
     * @param uploaderId 上传者ID
     * @return 附件信息
     */
    ImEmailAttachment uploadAttachment(MultipartFile file, Long uploaderId);

    /**
     * 保存附件记录到数据库
     *
     * @param emailId 邮件ID
     * @param attachments 附件列表
     */
    void saveAttachments(Long emailId, List<ImEmailAttachment> attachments);

    /**
     * 保存邮件附件关联（通过附件ID列表）
     *
     * @param emailId 邮件ID
     * @param attachmentIds 附件ID列表
     */
    void saveEmailAttachmentsByIds(Long emailId, List<Long> attachmentIds);

    /**
     * 获取邮件的所有附件
     *
     * @param emailId 邮件ID
     * @return 附件列表
     */
    List<ImEmailAttachment> getEmailAttachments(Long emailId);

    /**
     * 获取附件详情
     *
     * @param attachmentId 附件ID
     * @return 附件信息
     */
    ImEmailAttachment getAttachmentById(Long attachmentId);

    /**
     * 删除附件
     *
     * @param attachmentId 附件ID
     * @param userId 操作用户ID
     */
    void deleteAttachment(Long attachmentId, Long userId);

    /**
     * 批量删除附件（删除邮件时调用）
     *
     * @param emailId 邮件ID
     */
    void deleteAttachmentsByEmailId(Long emailId);
}
