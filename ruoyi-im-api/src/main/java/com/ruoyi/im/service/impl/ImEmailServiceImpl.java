package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImEmail;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImEmailMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件服务实现
 *
 * @author ruoyi
 */
@Service
public class ImEmailServiceImpl implements ImEmailService {

    private static final Logger log = LoggerFactory.getLogger(ImEmailServiceImpl.class);

    @Autowired
    private ImEmailMapper emailMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Override
    public List<ImEmail> getEmailList(Long userId, String folder) {
        return emailMapper.selectEmailsByUserIdAndFolder(userId, folder);
    }

    @Override
    public ImEmail getEmailDetail(Long emailId, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        // 验证权限：只有发送者或接收者可以查看
        if (!email.getSenderId().equals(userId) && !email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限查看此邮件");
        }
        return email;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendEmail(List<Long> toIds, String subject, String content, Long senderId) {
        if (toIds == null || toIds.isEmpty()) {
            throw new BusinessException("接收者不能为空");
        }

        // 获取发送者信息
        String senderName = String.valueOf(senderId);
        String senderEmail = senderId + "@im.com";

        List<ImEmail> emails = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long toId : toIds) {
            ImEmail email = new ImEmail();
            email.setSenderId(senderId);
            email.setSenderName(senderName);
            email.setSenderEmail(senderEmail);
            email.setReceiverId(toId);
            email.setSubject(subject);
            email.setHtmlContent(content);
            email.setTextContent(stripHtml(content));
            email.setIsRead(false);
            email.setIsDeleted(false);
            email.setIsStarred(false);
            email.setFolder("INBOX");
            email.setAttachmentCount(0);
            email.setSendTime(now);
            email.setReceiveTime(now);
            emails.add(email);
        }

        // 为每个接收者创建邮件记录
        for (ImEmail email : emails) {
            emailMapper.insertEmail(email);
        }

        log.info("发送邮件: senderId={}, toIds={}, count={}", senderId, toIds, emails.size());

        return emails.isEmpty() ? null : emails.get(0).getId();
    }

    @Override
    public Long saveDraft(String subject, String content, Long senderId) {
        ImEmail email = new ImEmail();
        email.setSenderId(senderId);
        email.setReceiverId(senderId); // 草稿的接收者设为自己
        email.setSubject(subject);
        email.setHtmlContent(content);
        email.setTextContent(stripHtml(content));
        email.setIsRead(true);
        email.setIsDeleted(false);
        email.setIsStarred(false);
        email.setFolder("DRAFTS");
        email.setAttachmentCount(0);

        emailMapper.insertEmail(email);

        log.info("保存草稿: senderId={}, emailId={}", senderId, email.getId());

        return email.getId();
    }

    @Override
    public void markAsRead(Long emailId, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        if (!email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作此邮件");
        }

        email.setIsRead(true);
        emailMapper.updateEmail(email);

        log.info("标记已读: emailId={}, userId={}", emailId, userId);
    }

    @Override
    public void markAsStarred(Long emailId, Long userId, boolean starred) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        // 验证权限：只有发送者或接收者可以操作
        if (!email.getSenderId().equals(userId) && !email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作此邮件");
        }

        email.setIsStarred(starred);
        emailMapper.updateEmail(email);

        log.info("标记星标: emailId={}, userId={}, starred={}", emailId, userId, starred);
    }

    @Override
    public void moveToTrash(Long emailId, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        // 验证权限：只有发送者或接收者可以操作
        if (!email.getSenderId().equals(userId) && !email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作此邮件");
        }

        email.setIsDeleted(true);
        email.setFolder("TRASH");
        emailMapper.updateEmail(email);

        log.info("移至垃圾箱: emailId={}, userId={}", emailId, userId);
    }

    @Override
    public void permanentlyDelete(Long emailId, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        // 验证权限：只有发送者或接收者可以删除
        if (!email.getSenderId().equals(userId) && !email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限删除此邮件");
        }

        emailMapper.deleteEmailById(emailId);

        log.info("永久删除: emailId={}, userId={}", emailId, userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return emailMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long replyEmail(Long originalEmailId, String content, Long senderId) {
        // 获取原邮件
        ImEmail originalEmail = getEmailDetail(originalEmailId, senderId);

        // 回复给原邮件发送者
        Long replyToId = originalEmail.getSenderId();
        String replySubject = originalEmail.getSubject();
        if (!replySubject.startsWith("Re:")) {
            replySubject = "Re: " + replySubject;
        }

        // 构建回复内容（包含原邮件引用）
        String replyContent = buildReplyContent(content, originalEmail);

        return sendEmail(java.util.Collections.singletonList(replyToId), replySubject, replyContent, senderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long forwardEmail(Long originalEmailId, java.util.List<Long> toIds, String content, Long senderId) {
        // 获取原邮件
        ImEmail originalEmail = getEmailDetail(originalEmailId, senderId);

        // 构建转发主题
        String forwardSubject = originalEmail.getSubject();
        if (!forwardSubject.startsWith("Fwd:")) {
            forwardSubject = "Fwd: " + forwardSubject;
        }

        // 构建转发内容（包含原邮件引用）
        String forwardContent = buildForwardContent(content, originalEmail);

        return sendEmail(toIds, forwardSubject, forwardContent, senderId);
    }

    @Override
    public void moveToFolder(Long emailId, String folder, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        // 验证权限
        if (!email.getSenderId().equals(userId) && !email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作此邮件");
        }

        email.setFolder(folder);
        emailMapper.updateEmail(email);

        log.info("移动邮件: emailId={}, folder={}, userId={}", emailId, folder, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkAsRead(java.util.List<Long> emailIds, Long userId) {
        if (emailIds == null || emailIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long emailId : emailIds) {
            try {
                markAsRead(emailId, userId);
                successCount++;
            } catch (Exception e) {
                log.warn("批量标记已读失败: emailId={}, error={}", emailId, e.getMessage());
            }
        }

        log.info("批量标记已读: userId={}, successCount={}", userId, successCount);
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMoveToTrash(java.util.List<Long> emailIds, Long userId) {
        if (emailIds == null || emailIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long emailId : emailIds) {
            try {
                moveToTrash(emailId, userId);
                successCount++;
            } catch (Exception e) {
                log.warn("批量删除失败: emailId={}, error={}", emailId, e.getMessage());
            }
        }

        log.info("批量删除: userId={}, successCount={}", userId, successCount);
        return successCount;
    }

    @Override
    public java.util.List<ImEmail> searchEmails(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return emailMapper.searchEmailsByKeyword(userId, keyword.trim());
    }

    @Override
    public java.util.Map<String, java.util.Map<String, Object>> getFolderStats(Long userId) {
        java.util.Map<String, java.util.Map<String, Object>> stats = new java.util.HashMap<>();

        // 从数据库获取统计数据
        java.util.List<java.util.Map<String, Object>> folderCounts = emailMapper.countByFolder(userId);

        // 初始化所有文件夹
        String[] folders = {"INBOX", "SENT", "DRAFTS", "STARRED", "TRASH"};
        for (String folder : folders) {
            java.util.Map<String, Object> folderStats = new java.util.HashMap<>();
            folderStats.put("total", 0);
            folderStats.put("unread", 0);
            stats.put(folder, folderStats);
        }

        // 填充实际数据
        for (java.util.Map<String, Object> row : folderCounts) {
            String folder = (String) row.get("folder");
            Integer total = ((Number) row.getOrDefault("total", 0)).intValue();
            Integer unread = ((Number) row.getOrDefault("unread", 0)).intValue();

            java.util.Map<String, Object> folderStats = stats.get(folder);
            if (folderStats != null) {
                folderStats.put("total", total);
                folderStats.put("unread", unread);
            }
        }

        log.info("获取文件夹统计: userId={}, stats={}", userId, stats);
        return stats;
    }

    /**
     * 构建回复邮件内容（包含原邮件引用）
     */
    private String buildReplyContent(String replyContent, ImEmail originalEmail) {
        StringBuilder sb = new StringBuilder();
        sb.append(replyContent);
        sb.append("<br/><br/>");
        sb.append("<div style=\"border-left: 2px solid #ccc; padding-left: 10px; color: #666;\">");
        sb.append("<p>在 ").append(originalEmail.getSendTime()).append("，");
        sb.append(originalEmail.getSenderName()).append(" 写道：</p>");
        sb.append("<blockquote>");
        sb.append(originalEmail.getHtmlContent());
        sb.append("</blockquote>");
        sb.append("</div>");
        return sb.toString();
    }

    /**
     * 构建转发邮件内容（包含原邮件引用）
     */
    private String buildForwardContent(String forwardContent, ImEmail originalEmail) {
        StringBuilder sb = new StringBuilder();
        sb.append(forwardContent);
        sb.append("<br/><br/>");
        sb.append("<div style=\"border: 1px solid #ccc; padding: 10px; background-color: #f5f5f5;\">");
        sb.append("<p>---------- 转发的邮件 ----------</p>");
        sb.append("<p><strong>发件人：</strong>").append(originalEmail.getSenderName()).append("</p>");
        sb.append("<p><strong>日期：</strong>").append(originalEmail.getSendTime()).append("</p>");
        sb.append("<p><strong>主题：</strong>").append(originalEmail.getSubject()).append("</p>");
        sb.append("<p><strong>收件人：</strong>").append(originalEmail.getReceiverId()).append("</p>");
        sb.append("<br/>");
        sb.append(originalEmail.getHtmlContent());
        sb.append("</div>");
        return sb.toString();
    }

    /**
     * 去除HTML标签，获取纯文本
     */
    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").trim();
    }
}
