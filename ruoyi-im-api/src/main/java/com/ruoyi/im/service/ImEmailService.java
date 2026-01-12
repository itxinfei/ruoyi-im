package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImEmail;

import java.util.List;

/**
 * 邮件服务接口
 *
 * @author ruoyi
 */
public interface ImEmailService {

    /**
     * 获取收件箱邮件列表
     *
     * @param userId 用户ID
     * @param folder 文件夹 INBOX/SENT/DRAFTS/SPAM/TRASH
     * @return 邮件列表
     */
    List<ImEmail> getEmailList(Long userId, String folder);

    /**
     * 获取邮件详情
     *
     * @param emailId 邮件ID
     * @param userId 用户ID
     * @return 邮件详情
     */
    ImEmail getEmailDetail(Long emailId, Long userId);

    /**
     * 发送邮件
     *
     * @param toIds 接收者ID列表
     * @param subject 主题
     * @param content 内容
     * @param senderId 发送者ID
     * @return 邮件ID
     */
    Long sendEmail(List<Long> toIds, String subject, String content, Long senderId);

    /**
     * 保存草稿
     *
     * @param subject 主题
     * @param content 内容
     * @param senderId 发送者ID
     * @return 邮件ID
     */
    Long saveDraft(String subject, String content, Long senderId);

    /**
     * 标记已读
     *
     * @param emailId 邮件ID
     * @param userId 用户ID
     */
    void markAsRead(Long emailId, Long userId);

    /**
     * 标记星标
     *
     * @param emailId 邮件ID
     * @param userId 用户ID
     * @param starred 是否星标
     */
    void markAsStarred(Long emailId, Long userId, boolean starred);

    /**
     * 移至垃圾箱
     *
     * @param emailId 邮件ID
     * @param userId 用户ID
     */
    void moveToTrash(Long emailId, Long userId);

    /**
     * 永久删除
     *
     * @param emailId 邮件ID
     * @param userId 用户ID
     */
    void permanentlyDelete(Long emailId, Long userId);

    /**
     * 获取未读邮件数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int getUnreadCount(Long userId);
}
