package com.ruoyi.im.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.im.domain.ImEmail;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImEmailMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImEmailAttachmentService;
import com.ruoyi.im.service.ImEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 邮件服务实现
 *
 * @author ruoyi
 */
@Service
public class ImEmailServiceImpl implements ImEmailService {

    private static final Logger log = LoggerFactory.getLogger(ImEmailServiceImpl.class);

    private final ImEmailMapper emailMapper;
    private final ImUserMapper userMapper;
    private final ImEmailAttachmentService attachmentService;

    public ImEmailServiceImpl(ImEmailMapper emailMapper,
                              ImUserMapper userMapper,
                              ImEmailAttachmentService attachmentService) {
        this.emailMapper = emailMapper;
        this.userMapper = userMapper;
        this.attachmentService = attachmentService;
    }

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
        return sendEmail(toIds, null, null, subject, content, null, senderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendEmail(List<Long> toIds, List<Long> ccIds, List<Long> bccIds, String subject, String content, Long senderId) {
        return sendEmail(toIds, ccIds, bccIds, subject, content, null, senderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendEmail(List<Long> toIds, List<Long> ccIds, List<Long> bccIds, String subject, String content, List<Long> attachmentIds, Long senderId) {
        if (toIds == null || toIds.isEmpty()) {
            throw new BusinessException("接收者不能为空");
        }

        // 获取发送者信息
        String senderName = String.valueOf(senderId);
        String senderEmail = senderId + "@im.com";

        List<ImEmail> emails = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 计算附件数量
        int attachmentCount = (attachmentIds != null && !attachmentIds.isEmpty()) ? attachmentIds.size() : 0;

        // 转换抄送密送列表为JSON字符串
        String ccIdsJson = (ccIds != null && !ccIds.isEmpty()) ? JSON.toJSONString(ccIds) : null;
        String bccIdsJson = (bccIds != null && !bccIds.isEmpty()) ? JSON.toJSONString(bccIds) : null;

        // 为每个接收者创建邮件记录（收件人）
        for (Long toId : toIds) {
            ImEmail email = new ImEmail();
            email.setSenderId(senderId);
            email.setSenderName(senderName);
            email.setSenderEmail(senderEmail);
            email.setReceiverId(toId);
            email.setCcIds(ccIdsJson);
            email.setBccIds(bccIdsJson);
            email.setSubject(subject);
            email.setHtmlContent(content);
            email.setTextContent(stripHtml(content));
            email.setIsRead(false);
            email.setIsDeleted(false);
            email.setIsStarred(false);
            email.setFolder("INBOX");
            email.setAttachmentCount(attachmentCount);
            email.setSendTime(now);
            email.setReceiveTime(now);
            emails.add(email);
        }

        // 为每个抄送人创建邮件记录
        if (ccIds != null && !ccIds.isEmpty()) {
            for (Long ccId : ccIds) {
                // 抄送人不重复接收
                if (toIds.contains(ccId)) {
                    continue;
                }
                ImEmail email = new ImEmail();
                email.setSenderId(senderId);
                email.setSenderName(senderName);
                email.setSenderEmail(senderEmail);
                email.setReceiverId(ccId);
                email.setCcIds(ccIdsJson);
                email.setBccIds(bccIdsJson);
                email.setSubject(subject);
                email.setHtmlContent(content);
                email.setTextContent(stripHtml(content));
                email.setIsRead(false);
                email.setIsDeleted(false);
                email.setIsStarred(false);
                email.setFolder("INBOX");
                email.setAttachmentCount(attachmentCount);
                email.setSendTime(now);
                email.setReceiveTime(now);
                emails.add(email);
            }
        }

        // 插入所有接收者和抄送者的邮件
        for (ImEmail email : emails) {
            emailMapper.insertEmail(email);
        }

        // 为发送者创建"已发送"记录
        ImEmail sentEmail = new ImEmail();
        sentEmail.setSenderId(senderId);
        sentEmail.setSenderName(senderName);
        sentEmail.setSenderEmail(senderEmail);
        sentEmail.setReceiverId(toIds.get(0)); // 主收件人
        sentEmail.setCcIds(ccIdsJson);
        sentEmail.setBccIds(bccIdsJson);
        sentEmail.setSubject(subject);
        sentEmail.setHtmlContent(content);
        sentEmail.setTextContent(stripHtml(content));
        sentEmail.setIsRead(true);
        sentEmail.setIsDeleted(false);
        sentEmail.setIsStarred(false);
        sentEmail.setFolder("SENT");
        sentEmail.setAttachmentCount(attachmentCount);
        sentEmail.setSendTime(now);
        sentEmail.setReceiveTime(now);
        emailMapper.insertEmail(sentEmail);

        // 保存附件关联（将附件关联到所有邮件）
        if (attachmentIds != null && !attachmentIds.isEmpty()) {
            // 为每个收件人和抄送人的邮件关联附件
            for (ImEmail email : emails) {
                attachmentService.saveEmailAttachmentsByIds(email.getId(), attachmentIds);
            }
            // 为发送者的已发送邮件也关联附件
            attachmentService.saveEmailAttachmentsByIds(sentEmail.getId(), attachmentIds);
        }

        log.info("发送邮件: senderId={}, toIds={}, ccIds={}, bccIds={}, attachmentCount={}, totalCount={}",
                senderId, toIds, ccIds, bccIds, attachmentCount, emails.size() + 1);

        return emails.isEmpty() ? sentEmail.getId() : emails.get(0).getId();
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
    public void markAsUnread(Long emailId, Long userId) {
        ImEmail email = emailMapper.selectEmailById(emailId);
        if (email == null) {
            throw new BusinessException("邮件不存在");
        }
        if (!email.getReceiverId().equals(userId)) {
            throw new BusinessException("无权限操作此邮件");
        }

        email.setIsRead(false);
        emailMapper.updateEmail(email);

        log.info("标记未读: emailId={}, userId={}", emailId, userId);
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
    public int batchMarkAsUnread(List<Long> emailIds, Long userId) {
        if (emailIds == null || emailIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long emailId : emailIds) {
            try {
                markAsUnread(emailId, userId);
                successCount++;
            } catch (Exception e) {
                log.warn("批量标记未读失败: emailId={}, error={}", emailId, e.getMessage());
            }
        }

        log.info("批量标记未读: userId={}, successCount={}", userId, successCount);
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
    @Transactional(rollbackFor = Exception.class)
    public int batchMoveToFolder(List<Long> emailIds, String folder, Long userId) {
        if (emailIds == null || emailIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long emailId : emailIds) {
            try {
                moveToFolder(emailId, folder, userId);
                successCount++;
            } catch (Exception e) {
                log.warn("批量移动失败: emailId={}, folder={}, error={}", emailId, folder, e.getMessage());
            }
        }

        log.info("批量移动: userId={}, folder={}, successCount={}", userId, folder, successCount);
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
        String[] folders = {"INBOX", "SENT", "DRAFTS", "TRASH"};
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

        // 单独查询星标邮件
        int starredCount = emailMapper.countStarredByUserId(userId);
        int starredUnreadCount = emailMapper.countUnreadByUserId(userId); // 星标邮件未读数单独查询
        java.util.Map<String, Object> starredStats = new java.util.HashMap<>();
        starredStats.put("total", starredCount);
        starredStats.put("unread", 0); // 星标邮件的未读暂不计入
        stats.put("STARRED", starredStats);

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
